use sea_orm::ActiveValue::Set;
use sea_orm::ColumnTrait;
use sea_orm::{ActiveModelTrait, EntityTrait, PaginatorTrait, QueryFilter, QueryOrder};
use sea_query::Order::Desc;

use openubl_entity as entity;
use openubl_entity::delivery::{TargetSendFileProtocolAction, TargetVerifyTicketProtocolAction};
use xhandler::prelude::*;

use crate::db::{Paginated, PaginatedResults, Transactional};
use crate::system::error::Error;
use crate::system::InnerSystem;

pub struct DocumentContext {
    pub system: InnerSystem,
    pub document: entity::document::Model,
}

impl From<(&InnerSystem, entity::document::Model)> for DocumentContext {
    fn from((system, document): (&InnerSystem, entity::document::Model)) -> Self {
        Self {
            system: system.clone(),
            document,
        }
    }
}

impl InnerSystem {
    pub async fn find_document_by_id(
        &self,
        id: i32,
        tx: Transactional<'_>,
    ) -> Result<Option<DocumentContext>, Error> {
        Ok(entity::document::Entity::find_by_id(id)
            .one(&self.connection(tx))
            .await?
            .map(|document| (self, document).into()))
    }

    pub async fn list_documents(
        &self,
        paginated: Paginated,
        tx: Transactional<'_>,
    ) -> Result<PaginatedResults<DocumentContext>, Error> {
        let connection = self.connection(tx);
        let pagination = entity::document::Entity::find()
            .order_by(entity::document::Column::Id, Desc)
            .paginate(&connection, paginated.page_size());

        Ok(PaginatedResults {
            items: pagination
                .fetch_page(paginated.page_number())
                .await?
                .drain(0..)
                .map(|document| (self, document).into())
                .collect::<Vec<DocumentContext>>(),
            num_items: pagination.num_items().await?,
        })
    }

    pub async fn find_document_by_ubl_params(
        &self,
        ruc: &str,
        document_type: &str,
        document_id: &str,
        sha256: &str,
        tx: Transactional<'_>,
    ) -> Result<Option<DocumentContext>, Error> {
        Ok(entity::document::Entity::find()
            .filter(entity::document::Column::Type.eq(document_type))
            .filter(entity::document::Column::Identifier.eq(document_id))
            .filter(entity::document::Column::SupplierId.eq(ruc))
            .filter(entity::document::Column::Sha256.eq(sha256))
            .one(&self.connection(tx))
            .await?
            .map(|document| (self, document).into()))
    }

    pub async fn persist_document(
        &self,
        model: &entity::document::Model,
        tx: Transactional<'_>,
    ) -> Result<DocumentContext, Error> {
        let entity = entity::document::ActiveModel {
            file_id: Set(model.file_id.clone()),
            r#type: Set(model.r#type.clone()),
            identifier: Set(model.identifier.clone()),
            supplier_id: Set(model.supplier_id.clone()),
            voided_document_code: Set(model.voided_document_code.clone()),
            digest_value: Set(model.digest_value.clone()),
            sha256: Set(model.sha256.clone()),
            id: Default::default(),
        };

        let result = entity.insert(&self.connection(tx)).await?;
        Ok((self, result).into())
    }
}

impl DocumentContext {
    pub async fn add_delivery(
        &self,
        response_wrapper: &SendFileResponseWrapper,
        tx: Transactional<'_>,
    ) -> Result<(), Error> {
        let mut entity = match &response_wrapper.response {
            SendFileAggregatedResponse::Cdr(_crd_base64, cdr) => entity::delivery::ActiveModel {
                document_id: Set(self.document.id),
                response_cdr_response_code: Set(Some(cdr.response_code.clone())),
                response_cdr_description: Set(Some(cdr.description.clone())),
                // response_cdr_notes: Set(Some(cdr.notes.clone())),
                ..Default::default()
            },
            SendFileAggregatedResponse::Ticket(ticket) => entity::delivery::ActiveModel {
                document_id: Set(self.document.id),
                response_ticket: Set(Some(ticket.clone())),
                ..Default::default()
            },
            SendFileAggregatedResponse::Error(error) => entity::delivery::ActiveModel {
                document_id: Set(self.document.id),
                response_error_code: Set(Some(error.code.clone())),
                response_error_message: Set(Some(error.message.clone())),
                ..Default::default()
            },
        };

        match &response_wrapper.send_file_target {
            SendFileTarget::Soap(url, action) => {
                entity.target_send_file_url = Set(url.clone());

                match action {
                    SoapFileTargetAction::Bill => {
                        entity.target_send_file_protocol =
                            Set(TargetSendFileProtocolAction::SoapBill);
                    }
                    SoapFileTargetAction::Summary => {
                        entity.target_send_file_protocol =
                            Set(TargetSendFileProtocolAction::SoapSummary);
                    }
                    SoapFileTargetAction::Pack => {
                        entity.target_send_file_protocol =
                            Set(TargetSendFileProtocolAction::SoapPack);
                    }
                }
            }
            SendFileTarget::Rest(url, action) => {
                entity.target_send_file_url = Set(url.clone());

                match action {
                    RestFileTargetAction::SendDocument => {
                        entity.target_send_file_protocol =
                            Set(TargetSendFileProtocolAction::RestSendDocument);
                    }
                }
            }
        };

        match &response_wrapper.verify_ticket_target {
            None => {}
            Some(verify_ticket_target) => match verify_ticket_target {
                VerifyTicketTarget::Soap(url) => {
                    entity.target_verify_ticket_url = Set(Some(url.clone()));
                    entity.target_verify_ticket_protocol =
                        Set(Some(TargetVerifyTicketProtocolAction::SOAP));
                }
                VerifyTicketTarget::Rest(url) => {
                    entity.target_verify_ticket_url = Set(Some(url.clone()));
                    entity.target_verify_ticket_protocol =
                        Set(Some(TargetVerifyTicketProtocolAction::REST));
                }
            },
        };

        let _result = entity.insert(&self.system.connection(tx)).await?;
        Ok(())
    }
}
