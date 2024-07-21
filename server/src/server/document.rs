use actix_multipart::form::tempfile::TempFile;
use actix_multipart::form::MultipartForm;
use actix_web::http::StatusCode;
use actix_web::{get, post, web, HttpResponse, Responder};
use anyhow::anyhow;

use openubl_api::db::{Paginated, Transactional};
use openubl_entity::document;
use xsender::prelude::{Credentials, FileSender, FromPath, UblFile, Urls};

use crate::dto::DocumentDto;
use crate::server::Error;
use crate::AppState;

#[utoipa::path(responses((status = 200, description = "List documents")))]
#[get("/documents")]
pub async fn list_documents(
    state: web::Data<AppState>,
    paginated: web::Query<Paginated>,
) -> Result<impl Responder, Error> {
    let result = state
        .system
        .list_documents(paginated.into_inner(), Transactional::None)
        .await
        .map_err(Error::System)?;

    Ok(HttpResponse::Ok()
        .append_header(("x-total", result.num_items))
        .json(
            result
                .items
                .into_iter()
                .map(|document_context| DocumentDto::from(document_context.document))
                .collect::<Vec<_>>(),
        ))
}

#[derive(Debug, MultipartForm)]
struct UploadForm {
    #[multipart(rename = "file")]
    files: Vec<TempFile>,
}

#[utoipa::path(responses((status = 200, description = "Upload a file")))]
#[post("/documents")]
pub async fn upload_file(
    state: web::Data<AppState>,
    MultipartForm(form): MultipartForm<UploadForm>,
) -> Result<impl Responder, Error> {
    let temp_file = form.files.first().ok_or(Error::BadRequest {
        status: StatusCode::BAD_REQUEST,
        msg: "No file found to be processed".to_string(),
    })?;

    // Read file
    let ubl_file = UblFile::from_path(temp_file.file.path())?;
    let file_metadata = ubl_file.metadata()?;
    let file_sha256 = ubl_file.sha256();

    // Verify file so we don't upload the same file twice
    let prev_file = state
        .system
        .find_document_by_ubl_params(
            &file_metadata.ruc,
            &file_metadata.document_type,
            &file_metadata.document_id,
            &file_sha256,
            Transactional::None,
        )
        .await?;

    match &prev_file {
        None => {
            // Upload to storage
            let file_path = temp_file.file.path().to_str().ok_or(Error::Any(anyhow!(
                "Could not extract the file path of the temp file"
            )))?;

            let file_id = state
                .storage
                .upload_ubl_xml(
                    &file_metadata.ruc,
                    &file_metadata.document_type,
                    &file_metadata.document_id,
                    &file_sha256,
                    file_path,
                )
                .await?;

            // Create file
            let document_model = document::Model {
                id: 0,
                file_id,
                supplier_id: file_metadata.ruc,
                identifier: file_metadata.document_id,
                r#type: file_metadata.document_type,
                voided_document_code: file_metadata.voided_line_document_type_code,
                digest_value: file_metadata.digest_value,
                sha256: file_sha256,
            };

            let document_ctx = state
                .system
                .persist_document(&document_model, Transactional::None)
                .await?;

            Ok(HttpResponse::Created().json(DocumentDto::from(document_ctx.document)))
        }
        Some(_) => Err(Error::BadRequest {
            status: StatusCode::CONFLICT,
            msg: "File already uploaded".to_string(),
        }),
    }
}

#[utoipa::path(responses((status = 200, description = "Get document's file")))]
#[get("/documents/{document_id}/download")]
pub async fn get_document_file(
    state: web::Data<AppState>,
    path: web::Path<i32>,
) -> Result<impl Responder, Error> {
    let document_id = path.into_inner();

    let document_ctx = state
        .system
        .find_document_by_id(document_id, Transactional::None)
        .await?
        .ok_or(Error::BadRequest {
            status: StatusCode::NOT_FOUND,
            msg: "Document not found".to_string(),
        })?;

    let xml_file = state
        .storage
        .download_ubl_xml(&document_ctx.document.file_id)
        .await?;

    Ok(HttpResponse::Ok()
        .append_header(("Content-Type", "application/xml"))
        .body(xml_file))
}

#[utoipa::path(responses((status = 200, description = "Get document's file")))]
#[post("/documents/{document_id}/send")]
pub async fn send_document(
    state: web::Data<AppState>,
    path: web::Path<i32>,
) -> Result<impl Responder, Error> {
    let document_id = path.into_inner();

    let document_ctx = state
        .system
        .find_document_by_id(document_id, Transactional::None)
        .await?
        .ok_or(Error::BadRequest {
            status: StatusCode::NOT_FOUND,
            msg: "Document not found".to_string(),
        })?;

    let xml_file = state
        .storage
        .download_ubl_xml(&document_ctx.document.file_id)
        .await?;

    let credentials_ctx = state
        .system
        .find_credentials_by_supplier_id(&document_ctx.document.supplier_id, Transactional::None)
        .await?
        .ok_or(Error::BadRequest {
            status: StatusCode::BAD_REQUEST,
            msg: "There is no credentials that can be used".to_string(),
        })?;

    let file_sender_client = FileSender {
        urls: Urls {
            invoice: credentials_ctx.credentials.url_invoice,
            perception_retention: credentials_ctx.credentials.url_perception_retention,
            despatch: credentials_ctx.credentials.url_despatch,
        },
        credentials: Credentials {
            username: credentials_ctx.credentials.username_sol,
            password: credentials_ctx.credentials.password_sol,
        },
    };

    let ubl_file = UblFile {
        file_content: xml_file,
    };
    let send_result = file_sender_client
        .send_file(&ubl_file)
        .await
        .map_err(|_err| Error::BadRequest {
            status: StatusCode::BAD_REQUEST,
            msg: "Error while sending the file".to_string(),
        })?;

    document_ctx
        .add_delivery(&send_result, Transactional::None)
        .await?;

    Ok(HttpResponse::Ok())
}
