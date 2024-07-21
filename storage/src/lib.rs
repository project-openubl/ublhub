use anyhow::anyhow;
use std::fs;
use std::fs::File;
use std::io::{Cursor, Read, Write};
use std::path::Path;
use std::str::FromStr;

use aws_config::meta::region::RegionProviderChain;
use aws_config::retry::RetryConfig;
use aws_config::BehaviorVersion;
use aws_sdk_s3::client::Client as S3Client;
use aws_sdk_s3::error::SdkError;
use aws_sdk_s3::operation::get_object::GetObjectError;
use aws_sdk_s3::operation::put_object::PutObjectError;
use aws_sdk_s3::primitives::{ByteStream, ByteStreamError};
use aws_smithy_runtime_api::client::orchestrator::HttpResponse;
use minio::s3::args::{GetObjectArgs, PutObjectArgs};
use minio::s3::client::Client as MinioClient;
use minio::s3::creds::StaticProvider;
use minio::s3::http::BaseUrl;
use zip::result::{ZipError, ZipResult};
use zip::write::FileOptions;
use zip::{ZipArchive, ZipWriter};

use crate::config::Storage;

pub mod config;

pub struct LocalDir {
    path: String,
}

pub struct Bucket {
    name: String,
}

pub enum StorageSystem {
    Local(LocalDir),
    Minio(Bucket, MinioClient),
    S3(Bucket, S3Client),
}

#[derive(Debug, thiserror::Error)]
pub enum StorageSystemErr {
    #[error(transparent)]
    Local(std::io::Error),

    #[error(transparent)]
    Minio(MinioError),

    #[error(transparent)]
    S3(#[from] S3Error),

    #[error(transparent)]
    Zip(ZipError),

    #[error(transparent)]
    Any(#[from] anyhow::Error),
}

#[derive(Debug, thiserror::Error)]
pub enum MinioError {
    #[error(transparent)]
    Sdk(minio::s3::error::Error),
    #[error(transparent)]
    HttpClient(reqwest::Error),
}

#[derive(Debug, thiserror::Error)]
pub enum S3Error {
    #[error(transparent)]
    Stream(ByteStreamError),

    #[error(transparent)]
    Get(SdkError<GetObjectError, HttpResponse>),

    #[error(transparent)]
    Put(SdkError<PutObjectError, HttpResponse>),
}

impl From<std::io::Error> for StorageSystemErr {
    fn from(e: std::io::Error) -> Self {
        Self::Local(e)
    }
}

impl From<minio::s3::error::Error> for StorageSystemErr {
    fn from(e: minio::s3::error::Error) -> Self {
        Self::Minio(MinioError::Sdk(e))
    }
}

impl From<reqwest::Error> for StorageSystemErr {
    fn from(e: reqwest::Error) -> Self {
        Self::Minio(MinioError::HttpClient(e))
    }
}

impl From<ByteStreamError> for StorageSystemErr {
    fn from(e: ByteStreamError) -> Self {
        Self::S3(S3Error::Stream(e))
    }
}

impl From<SdkError<GetObjectError, HttpResponse>> for StorageSystemErr {
    fn from(e: SdkError<GetObjectError, HttpResponse>) -> Self {
        Self::S3(S3Error::Get(e))
    }
}

impl From<SdkError<PutObjectError, HttpResponse>> for StorageSystemErr {
    fn from(e: SdkError<PutObjectError, HttpResponse>) -> Self {
        Self::S3(S3Error::Put(e))
    }
}

impl From<ZipError> for StorageSystemErr {
    fn from(e: ZipError) -> Self {
        Self::Zip(e)
    }
}

impl StorageSystem {
    pub async fn new(config: &Storage) -> anyhow::Result<Self> {
        match config {
            Storage::Local(config) => Ok(Self::Local(LocalDir {
                path: config.local_dir.clone(),
            })),
            Storage::Minio(config) => {
                let static_provider =
                    StaticProvider::new(&config.access_key, &config.secret_key, None);
                let client = MinioClient::new(
                    BaseUrl::from_str(&config.host)?,
                    Some(Box::new(static_provider)),
                    None,
                    None,
                )?;
                Ok(Self::Minio(
                    Bucket {
                        name: config.bucket.clone(),
                    },
                    client,
                ))
            }
            Storage::S3(config) => {
                let region_provider = RegionProviderChain::default_provider().or_else("us-east-1");
                let credentials_provider = aws_sdk_s3::config::Credentials::new(
                    &config.access_key,
                    &config.secret_key,
                    Some("test_session_token".to_string()),
                    None,
                    "",
                );
                let sdk_config = aws_config::defaults(BehaviorVersion::latest())
                    .region(region_provider)
                    .credentials_provider(credentials_provider)
                    .retry_config(RetryConfig::standard().with_max_attempts(3))
                    .load()
                    .await;

                let client = S3Client::new(&sdk_config);
                Ok(Self::S3(
                    Bucket {
                        name: config.bucket.clone(),
                    },
                    client,
                ))
            }
        }
    }

    /// Each file will be zipped before being uploaded to the Storage
    pub async fn upload_ubl_xml(
        &self,
        ruc: &str,
        document_type: &str,
        document_id: &str,
        file_sha246: &str,
        file_full_path: &str,
    ) -> Result<String, StorageSystemErr> {
        // Create zip
        let file_name_inside_zip = format!("{ruc}-{}.xml", document_id.to_uppercase());

        let zip_file = create_zip_from_path(file_full_path, &file_name_inside_zip)?;
        let zip_file_name = format!(
            "{}_{}.zip",
            document_id.to_uppercase(),
            file_sha246.chars().take(7).collect::<String>()
        );

        match self {
            StorageSystem::Local(directories) => {
                let object_name = Path::new(&directories.path)
                    .join(ruc)
                    .join(document_type)
                    .join(&zip_file_name);

                File::create(object_name)?.write_all(&zip_file)?;
                Ok(zip_file_name.clone())
            }
            StorageSystem::Minio(bucket, client) => {
                let object_name = format!("{ruc}/{document_type}/{zip_file_name}");

                let object_stream_size = zip_file.len();
                let mut object_stream = Cursor::new(zip_file);

                let mut object = PutObjectArgs::new(
                    &bucket.name,
                    &object_name,
                    &mut object_stream,
                    Some(object_stream_size),
                    None,
                )?;

                client.put_object(&mut object).await?;

                Ok(object_name)
            }
            StorageSystem::S3(buckets, client) => {
                let object_name = format!("{ruc}/{document_type}/{zip_file_name}");
                let object = ByteStream::from(zip_file);

                client
                    .put_object()
                    .bucket(&buckets.name)
                    .key(&object_name)
                    .body(object)
                    .send()
                    .await?;

                Ok(object_name)
            }
        }
    }

    /// Each file will be unzipped after retrieved from storage
    pub async fn download_ubl_xml(&self, file_id: &str) -> Result<String, StorageSystemErr> {
        let zip_file = match self {
            StorageSystem::Local(_) => fs::read(file_id)?,
            StorageSystem::Minio(bucket, client) => {
                let object = GetObjectArgs::new(&bucket.name, file_id)?;

                client.get_object(&object).await?.bytes().await?.to_vec()
            }
            StorageSystem::S3(buckets, client) => client
                .get_object()
                .bucket(&buckets.name)
                .key(file_id)
                .send()
                .await?
                .body
                .try_next()
                .await?
                .ok_or(StorageSystemErr::Any(anyhow!("Could not find response")))?
                .to_vec(),
        };

        let xml_file = extract_first_file_from_zip(&zip_file)?.ok_or(StorageSystemErr::Any(
            anyhow!("Could not extract first file from zip"),
        ))?;

        Ok(xml_file)
    }
}

pub fn create_zip_from_path(path: &str, file_name_inside_zip: &str) -> ZipResult<Vec<u8>> {
    let file_content = fs::read_to_string(path)?;
    create_zip_from_str(&file_content, file_name_inside_zip)
}

pub fn create_zip_from_str(content: &str, file_name_inside_zip: &str) -> ZipResult<Vec<u8>> {
    let mut data = Vec::new();

    {
        let buff = Cursor::new(&mut data);
        let mut zip = ZipWriter::new(buff);

        let file_options = FileOptions::default();
        zip.start_file(file_name_inside_zip, file_options)?;
        zip.write_all(content.as_bytes())?;
        zip.finish()?;
    }

    Ok(data)
}

pub fn extract_first_file_from_zip(zip_buf: &Vec<u8>) -> Result<Option<String>, std::io::Error> {
    let reader = Cursor::new(zip_buf);
    let mut archive = ZipArchive::new(reader)?;

    let mut result = None;

    for index in 0..archive.len() {
        let mut entry = archive.by_index(index)?;
        if entry.is_file() {
            let mut buffer = String::new();
            entry.read_to_string(&mut buffer)?;
            result = Some(buffer);
            break;
        }
    }

    Ok(result)
}
