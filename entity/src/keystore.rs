//! `SeaORM` Entity. Generated by sea-orm-codegen 0.12.10

use sea_orm::entity::prelude::*;

#[derive(Clone, Debug, PartialEq, DeriveEntityModel, Eq)]
#[sea_orm(table_name = "keystore")]
pub struct Model {
    #[sea_orm(primary_key)]
    pub id: i32,
    pub name: String,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveRelation)]
pub enum Relation {
    #[sea_orm(has_many = "super::keystore_config::Entity")]
    KeystoreConfig,
}

impl Related<super::keystore_config::Entity> for Entity {
    fn to() -> RelationDef {
        Relation::KeystoreConfig.def()
    }
}

impl ActiveModelBehavior for ActiveModel {}
