use sea_orm_migration::prelude::*;

#[derive(DeriveMigrationName)]
pub struct Migration;

#[async_trait::async_trait]
impl MigrationTrait for Migration {
    async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .create_table(
                Table::create()
                    .table(Document::Table)
                    .if_not_exists()
                    .col(
                        ColumnDef::new(Document::Id)
                            .integer()
                            .not_null()
                            .auto_increment()
                            .primary_key(),
                    )
                    .col(ColumnDef::new(Document::FileId).string().not_null())
                    .col(ColumnDef::new(Document::SupplierId).string().not_null())
                    .col(ColumnDef::new(Document::Identifier).string().not_null())
                    .col(ColumnDef::new(Document::Type).string().not_null())
                    .col(ColumnDef::new(Document::VoidedDocumentCode).string())
                    .col(ColumnDef::new(Document::DigestValue).string())
                    .col(ColumnDef::new(Document::Sha256).string().not_null())
                    .to_owned(),
            )
            .await
    }

    async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .drop_table(Table::drop().table(Document::Table).to_owned())
            .await
    }
}

#[derive(DeriveIden)]
pub enum Document {
    Table,
    Id,
    FileId,
    Type,
    Identifier,
    SupplierId,
    VoidedDocumentCode,
    DigestValue,
    Sha256,
}
