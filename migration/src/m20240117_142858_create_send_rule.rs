use crate::m20240114_154538_create_credentials::Credentials;
use sea_orm_migration::prelude::*;

#[derive(DeriveMigrationName)]
pub struct Migration;

#[async_trait::async_trait]
impl MigrationTrait for Migration {
    async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .create_table(
                Table::create()
                    .table(SendRule::Table)
                    .if_not_exists()
                    .col(
                        ColumnDef::new(SendRule::Id)
                            .integer()
                            .not_null()
                            .auto_increment()
                            .primary_key(),
                    )
                    .col(ColumnDef::new(SendRule::SupplierId).string().not_null())
                    .col(ColumnDef::new(SendRule::CredentialsId).integer().not_null())
                    .foreign_key(
                        ForeignKey::create()
                            .from_col(SendRule::CredentialsId)
                            .to(Credentials::Table, Credentials::Id)
                            .on_delete(ForeignKeyAction::Cascade),
                    )
                    .to_owned(),
            )
            .await
    }

    async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .drop_table(Table::drop().table(SendRule::Table).to_owned())
            .await
    }
}

#[derive(DeriveIden)]
enum SendRule {
    Table,
    Id,
    SupplierId,
    CredentialsId,
}
