pub use sea_orm_migration::prelude::*;

mod m20240101_104121_create_document;
mod m20240113_213636_create_keystore;
mod m20240113_213657_create_keystore_config;
mod m20240114_154538_create_credentials;
mod m20240117_142858_create_send_rule;
mod m20240717_214515_create_delivery;
pub struct Migrator;

#[async_trait::async_trait]
impl MigratorTrait for Migrator {
    fn migrations() -> Vec<Box<dyn MigrationTrait>> {
        vec![
            Box::new(m20240101_104121_create_document::Migration),
            Box::new(m20240113_213636_create_keystore::Migration),
            Box::new(m20240113_213657_create_keystore_config::Migration),
            Box::new(m20240114_154538_create_credentials::Migration),
            Box::new(m20240117_142858_create_send_rule::Migration),
            Box::new(m20240717_214515_create_delivery::Migration),
        ]
    }
}
