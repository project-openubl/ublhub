use actix_web::http::StatusCode;
use actix_web::{delete, get, post, put, web, HttpResponse, Responder};

use openubl_api::db::Transactional;
use openubl_entity as entity;

use crate::dto::{CredentialsDto, NewCredentialsDto};
use crate::server::Error;
use crate::AppState;

#[utoipa::path(responses((status = 200, description = "List credentials")))]
#[get("/credentials")]
pub async fn list_credentials(state: web::Data<AppState>) -> Result<impl Responder, Error> {
    let result = state
        .system
        .list_credentials(Transactional::None)
        .await
        .map_err(Error::System)?
        .into_iter()
        .map(|e| CredentialsDto::from(e.credentials))
        .collect::<Vec<_>>();

    Ok(HttpResponse::Ok().json(result))
}

#[utoipa::path(responses((status = 200, description = "Create credentials")))]
#[post("/credentials")]
pub async fn create_credentials(
    state: web::Data<AppState>,
    json: web::Json<NewCredentialsDto>,
) -> Result<impl Responder, Error> {
    let supplier_ids = json.supplier_ids_applied_to.clone();
    let model = entity::credentials::Model::from(json.into_inner());

    let credentials_ctx = state
        .system
        .persist_credentials(&model, &supplier_ids, Transactional::None)
        .await
        .map_err(Error::System)?;
    Ok(HttpResponse::Ok().json(CredentialsDto::from(credentials_ctx.credentials)))
}

#[utoipa::path(responses((status = 200, description = "Get credential")))]
#[get("/credentials/{credentials_id}")]
pub async fn get_credentials(
    state: web::Data<AppState>,
    path: web::Path<i32>,
) -> Result<impl Responder, Error> {
    let credentials_id = path.into_inner();

    let credentials_ctx = state
        .system
        .find_credentials_by_id(credentials_id, Transactional::None)
        .await?
        .ok_or(Error::BadRequest {
            status: StatusCode::NOT_FOUND,
            msg: "Credentials not found".to_string(),
        })?;

    Ok(HttpResponse::Ok().json(CredentialsDto::from(credentials_ctx.credentials)))
}

#[utoipa::path(responses((status = 204, description = "Update credentials")))]
#[put("/credentials/{credentials_id}")]
pub async fn update_credentials(
    state: web::Data<AppState>,
    path: web::Path<i32>,
    json: web::Json<NewCredentialsDto>,
) -> Result<impl Responder, Error> {
    let credentials_id = path.into_inner();

    let credentials_ctx = state
        .system
        .find_credentials_by_id(credentials_id, Transactional::None)
        .await?
        .ok_or(Error::BadRequest {
            status: StatusCode::NOT_FOUND,
            msg: "Credentials not found".to_string(),
        })?;

    let model = entity::credentials::Model::from(json.into_inner());
    credentials_ctx.update(&model, Transactional::None).await?;
    Ok(HttpResponse::NoContent().finish())
}

#[utoipa::path(responses((status = 204, description = "Delete credentials")))]
#[delete("/credentials/{credentials_id}")]
pub async fn delete_credentials(
    state: web::Data<AppState>,
    path: web::Path<i32>,
) -> Result<impl Responder, Error> {
    let credentials_id = path.into_inner();

    let credentials_ctx = state
        .system
        .find_credentials_by_id(credentials_id, Transactional::None)
        .await?
        .ok_or(Error::BadRequest {
            status: StatusCode::NOT_FOUND,
            msg: "Credentials not found".to_string(),
        })?;

    credentials_ctx.delete(Transactional::None).await?;
    Ok(HttpResponse::NoContent().finish())
}
