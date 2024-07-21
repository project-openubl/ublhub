use actix_web::{get, web, Responder};

use crate::server::Error;
use crate::AppState;

#[utoipa::path(
    responses(
        (status = 200, description = "Liveness"),
    ),
)]
#[get("/health/live")]
pub async fn liveness(_: web::Data<AppState>) -> Result<impl Responder, Error> {
    Ok("Live")
}

#[utoipa::path(
    responses(
        (status = 200, description = "Readiness"),
    ),
)]
#[get("/health/read")]
pub async fn readiness(_: web::Data<AppState>) -> Result<impl Responder, Error> {
    Ok("Read")
}
