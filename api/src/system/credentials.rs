use sea_orm::ActiveValue::Set;
use sea_orm::ColumnTrait;
use sea_orm::QueryFilter;
use sea_orm::{ActiveModelTrait, EntityTrait, QuerySelect, RelationTrait};
use sea_query::JoinType;

use openubl_entity as entity;

use crate::db::Transactional;
use crate::system::error::Error;
use crate::system::InnerSystem;

pub struct CredentialsContext {
    pub system: InnerSystem,
    pub credentials: entity::credentials::Model,
}

impl From<(&InnerSystem, entity::credentials::Model)> for CredentialsContext {
    fn from((system, credentials): (&InnerSystem, entity::credentials::Model)) -> Self {
        Self {
            system: system.clone(),
            credentials,
        }
    }
}

impl InnerSystem {
    pub async fn find_credentials_by_id(
        &self,
        id: i32,
        tx: Transactional<'_>,
    ) -> Result<Option<CredentialsContext>, Error> {
        Ok(entity::credentials::Entity::find_by_id(id)
            .one(&self.connection(tx))
            .await?
            .map(|e| (self, e).into()))
    }

    pub async fn find_credentials_by_supplier_id(
        &self,
        supplier_id: &str,
        tx: Transactional<'_>,
    ) -> Result<Option<CredentialsContext>, Error> {
        let credential: Option<CredentialsContext> = entity::credentials::Entity::find()
            .join(
                JoinType::InnerJoin,
                entity::credentials::Relation::SendRule.def(),
            )
            .filter(entity::send_rule::Column::SupplierId.eq(supplier_id))
            .one(&self.connection(tx))
            .await?
            .map(|e| (self, e).into());

        match credential {
            Some(credential) => Ok(Some(credential)),

            // If no credentials found, try the default one
            None => Ok(entity::credentials::Entity::find()
                .join(
                    JoinType::InnerJoin,
                    entity::credentials::Relation::SendRule.def(),
                )
                .filter(entity::send_rule::Column::SupplierId.eq("*"))
                .one(&self.connection(tx))
                .await?
                .map(|e| (self, e).into())),
        }
    }

    pub async fn list_credentials(
        &self,
        tx: Transactional<'_>,
    ) -> Result<Vec<CredentialsContext>, Error> {
        Ok(entity::credentials::Entity::find()
            .all(&self.connection(tx))
            .await?
            .drain(0..)
            .map(|credentials| (self, credentials).into())
            .collect())
    }

    pub async fn persist_credentials(
        &self,
        model: &entity::credentials::Model,
        supplier_ids: &[String],
        tx: Transactional<'_>,
    ) -> Result<CredentialsContext, Error> {
        let entity = entity::credentials::ActiveModel {
            name: Set(model.name.clone()),
            description: Set(model.description.clone()),
            username_sol: Set(model.username_sol.clone()),
            password_sol: Set(model.password_sol.clone()),
            client_id: Set(model.client_id.clone()),
            client_secret: Set(model.client_secret.clone()),

            url_invoice: Set(model.url_invoice.clone()),
            url_despatch: Set(model.url_despatch.clone()),
            url_perception_retention: Set(model.url_perception_retention.clone()),

            id: Default::default(),
        };

        let result = entity.insert(&self.connection(tx)).await?;

        let rules = supplier_ids
            .iter()
            .map(|supplier_id| entity::send_rule::ActiveModel {
                id: Default::default(),
                supplier_id: Set(supplier_id.clone()),
                credentials_id: Set(result.id),
            })
            .collect::<Vec<_>>();
        let _rules = entity::send_rule::Entity::insert_many(rules)
            .exec(&self.connection(tx))
            .await?;

        Ok((self, result).into())
    }
}

impl CredentialsContext {
    pub async fn update(
        &self,
        model: &entity::credentials::Model,
        tx: Transactional<'_>,
    ) -> Result<(), Error> {
        let mut entity: entity::credentials::ActiveModel = self.credentials.clone().into();

        entity.name = Set(model.name.clone());
        entity.description = Set(model.description.clone());
        entity.username_sol = Set(model.username_sol.clone());
        entity.password_sol = Set(model.password_sol.clone());
        entity.client_id = Set(model.client_id.clone());
        entity.client_secret = Set(model.client_secret.clone());

        entity.update(&self.system.connection(tx)).await?;
        Ok(())
    }

    pub async fn delete(&self, tx: Transactional<'_>) -> Result<(), Error> {
        let entity: entity::credentials::ActiveModel = self.credentials.clone().into();
        entity.delete(&self.system.connection(tx)).await?;
        Ok(())
    }
}
