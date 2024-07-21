use std::borrow::Cow;
use std::fmt::Display;

use actix_web::body::BoxBody;
use actix_web::http::StatusCode;
use actix_web::{HttpResponse, ResponseError};

use openubl_api::system;
use openubl_storage::StorageSystemErr;

pub mod credentials;
pub mod document;
pub mod health;

#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error(transparent)]
    System(system::error::Error),
    #[error(transparent)]
    Io(std::io::Error),
    #[error(transparent)]
    Storage(#[from] StorageSystemErr),
    #[error("Invalid request {msg:?}")]
    BadRequest { msg: String, status: StatusCode },
    #[error(transparent)]
    Any(#[from] anyhow::Error),
}

impl From<system::error::Error> for Error {
    fn from(e: system::error::Error) -> Self {
        Self::System(e)
    }
}

impl From<std::io::Error> for Error {
    fn from(e: std::io::Error) -> Self {
        Self::Io(e)
    }
}

#[derive(Clone, Debug, serde::Serialize, serde::Deserialize)]
pub struct ErrorInformation {
    pub r#type: Cow<'static, str>,
    pub message: String,
}

impl ErrorInformation {
    pub fn new(r#type: impl Into<Cow<'static, str>>, message: impl Display) -> Self {
        Self {
            r#type: r#type.into(),
            message: message.to_string(),
        }
    }
}

impl ResponseError for Error {
    fn error_response(&self) -> HttpResponse<BoxBody> {
        match self {
            Self::System(err) => {
                HttpResponse::InternalServerError().json(ErrorInformation::new("System", err))
            }
            Self::Io(err) => {
                HttpResponse::InternalServerError().json(ErrorInformation::new("System IO", err))
            }
            Self::Storage(err) => HttpResponse::InternalServerError()
                .json(ErrorInformation::new("System storage", err)),
            Self::BadRequest { msg, status } => {
                HttpResponse::build(*status).json(ErrorInformation::new("Bad request", msg))
            }
            Self::Any(err) => HttpResponse::InternalServerError()
                .json(ErrorInformation::new("System unknown", err)),
        }
    }
}
