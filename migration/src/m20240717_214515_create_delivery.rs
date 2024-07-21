use sea_orm_migration::prelude::*;

use crate::m20240101_104121_create_document::Document;

#[derive(DeriveMigrationName)]
pub struct Migration;

#[async_trait::async_trait]
impl MigrationTrait for Migration {
    async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .create_table(
                Table::create()
                    .table(Delivery::Table)
                    .if_not_exists()
                    .col(
                        ColumnDef::new(Delivery::Id)
                            .integer()
                            .not_null()
                            .auto_increment()
                            .primary_key(),
                    )
                    .col(ColumnDef::new(Delivery::ResponseTicket).string())
                    .col(ColumnDef::new(Delivery::ResponseCdrDescription).string())
                    .col(ColumnDef::new(Delivery::ResponseCdrResponseCode).string())
                    .col(ColumnDef::new(Delivery::ResponseCdrNotes).text())
                    .col(ColumnDef::new(Delivery::ResponseErrorCode).string())
                    .col(ColumnDef::new(Delivery::ResponseErrorMessage).string())
                    .col(
                        ColumnDef::new(Delivery::TargetSendFileProtocol)
                            .string_len(1)
                            .not_null(),
                    )
                    .col(
                        ColumnDef::new(Delivery::TargetSendFileUrl)
                            .string()
                            .not_null(),
                    )
                    .col(
                        ColumnDef::new(Delivery::TargetSendFileAction)
                            .string()
                            .not_null(),
                    )
                    .col(
                        ColumnDef::new(Delivery::TargetVerifyTicketProtocol)
                            .string_len(1)
                            .not_null(),
                    )
                    .col(ColumnDef::new(Delivery::TargetVerifyTicketUrl).string())
                    .col(ColumnDef::new(Delivery::DocumentId).integer().not_null())
                    .foreign_key(
                        ForeignKey::create()
                            .from_col(Delivery::DocumentId)
                            .to(Document::Table, Document::Id)
                            .on_delete(ForeignKeyAction::Cascade),
                    )
                    .to_owned(),
            )
            .await
    }

    async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .drop_table(Table::drop().table(Delivery::Table).to_owned())
            .await
    }
}

#[derive(DeriveIden)]
enum Delivery {
    Table,
    Id,
    ResponseTicket,
    ResponseCdrDescription,
    ResponseCdrResponseCode,
    ResponseCdrNotes,
    ResponseErrorCode,
    ResponseErrorMessage,
    TargetSendFileProtocol,
    TargetSendFileUrl,
    TargetSendFileAction,
    TargetVerifyTicketProtocol,
    TargetVerifyTicketUrl,
    DocumentId,
}
