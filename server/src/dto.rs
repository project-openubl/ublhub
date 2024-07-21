use openubl_entity as entity;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
pub struct DocumentDto {
    pub id: i32,
    pub supplier_id: String,
    pub document_id: String,
    pub document_type: String,
    pub voided_document_code: Option<String>,
}

impl From<entity::document::Model> for DocumentDto {
    fn from(value: entity::document::Model) -> Self {
        Self {
            id: value.id,
            supplier_id: value.supplier_id.clone(),
            document_id: value.identifier.clone(),
            document_type: value.r#type.clone(),
            voided_document_code: value.voided_document_code.clone(),
        }
    }
}

#[derive(Serialize, Deserialize)]
pub struct CredentialsDto {
    pub id: i32,
    pub name: String,
    pub description: Option<String>,
    pub username_sol: String,
    pub client_id: String,
    pub url_invoice: String,
    pub url_despatch: String,
    pub url_perception_retention: String,
}

#[derive(Serialize, Deserialize)]
pub struct NewCredentialsDto {
    pub name: String,
    pub description: Option<String>,
    pub username_sol: String,
    pub password_sol: String,
    pub client_id: String,
    pub client_secret: String,
    pub url_invoice: String,
    pub url_despatch: String,
    pub url_perception_retention: String,
    pub supplier_ids_applied_to: Vec<String>,
}

impl From<entity::credentials::Model> for CredentialsDto {
    fn from(value: entity::credentials::Model) -> Self {
        Self {
            id: value.id,
            name: value.name.clone(),
            description: value.description.clone(),
            username_sol: value.username_sol.clone(),
            client_id: value.client_id.clone(),
            url_invoice: value.url_invoice.clone(),
            url_despatch: value.url_despatch.clone(),
            url_perception_retention: value.url_perception_retention.clone(),
        }
    }
}

impl From<NewCredentialsDto> for entity::credentials::Model {
    fn from(value: NewCredentialsDto) -> Self {
        Self {
            id: 0,
            name: value.name.clone(),
            description: value.description.clone(),
            username_sol: value.username_sol.clone(),
            password_sol: value.password_sol,
            client_id: value.client_id.clone(),
            client_secret: value.client_secret,
            url_invoice: value.url_invoice.clone(),
            url_despatch: value.url_despatch.clone(),
            url_perception_retention: value.url_perception_retention.clone(),
        }
    }
}
