use crate::m20240113_213636_create_keystore::Keystore;
use sea_orm_migration::prelude::*;

#[derive(DeriveMigrationName)]
pub struct Migration;

#[async_trait::async_trait]
impl MigrationTrait for Migration {
    async fn up(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .create_table(
                Table::create()
                    .table(KeystoreConfig::Table)
                    .if_not_exists()
                    .col(
                        ColumnDef::new(KeystoreConfig::Id)
                            .integer()
                            .not_null()
                            .auto_increment()
                            .primary_key(),
                    )
                    .col(ColumnDef::new(KeystoreConfig::Name).string().not_null())
                    .col(ColumnDef::new(KeystoreConfig::Val).string().not_null())
                    .col(
                        ColumnDef::new(KeystoreConfig::KeystoreId)
                            .integer()
                            .not_null(),
                    )
                    .foreign_key(
                        ForeignKey::create()
                            .from_col(KeystoreConfig::KeystoreId)
                            .to(Keystore::Table, Keystore::Id)
                            .on_delete(ForeignKeyAction::Cascade),
                    )
                    .to_owned(),
            )
            .await
    }

    async fn down(&self, manager: &SchemaManager) -> Result<(), DbErr> {
        manager
            .drop_table(Table::drop().table(KeystoreConfig::Table).to_owned())
            .await
    }
}

#[derive(DeriveIden)]
enum KeystoreConfig {
    Table,
    Id,
    Name,
    Val,
    KeystoreId,
}
