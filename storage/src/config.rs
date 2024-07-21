#[derive(clap::Subcommand, Debug)]
pub enum Storage {
    Local(LocalStorage),
    Minio(MinioStorage),
    S3(S3Storage),
}

#[derive(clap::Args, Debug)]
pub struct LocalStorage {
    #[arg(
        id = "storage-local-dir",
        long,
        env = "STORAGE_LOCAL_DIR",
        default_value = "storage"
    )]
    pub local_dir: String,
}

#[derive(clap::Args, Debug)]
pub struct MinioStorage {
    #[arg(id = "storage-minio-host", long, env = "STORAGE_MINIO_HOST")]
    pub host: String,

    #[arg(
        id = "storage-minio-bucket",
        long,
        env = "STORAGE_MINIO_BUCKET",
        default_value = "openubl"
    )]
    pub bucket: String,

    #[arg(
        id = "storage-minio-access-key",
        long,
        env = "STORAGE_MINIO_ACCESS_KEY"
    )]
    pub access_key: String,

    #[arg(
        id = "storage-minio-secret-key",
        long,
        env = "STORAGE_MINIO_SECRET_KEY"
    )]
    pub secret_key: String,
}

#[derive(clap::Args, Debug)]
pub struct S3Storage {
    #[arg(id = "storage-s3-region", long, env = "STORAGE_S3_REGION")]
    pub region: String,

    #[arg(
        id = "storage-s3-bucket",
        long,
        env = "STORAGE_S3_BUCKET",
        default_value = "openubl"
    )]
    pub bucket: String,

    #[arg(id = "storage-s3-access-key", long, env = "STORAGE_S3_ACCESS_KEY")]
    pub access_key: String,

    #[arg(id = "storage-s3-secret-key", long, env = "STORAGE_S3_SECRET_KEY")]
    pub secret_key: String,
}
