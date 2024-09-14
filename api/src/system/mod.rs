use std::fmt::{Debug, Display, Formatter};
use std::sync::Arc;

use openubl_common::config::DatabaseProvider;
use sea_orm::{ConnectOptions, ConnectionTrait, Database, DatabaseConnection, DbErr, Statement};

use migration::{Migrator, MigratorTrait};

use crate::db::{ConnectionOrTransaction, Transactional};

pub mod credentials;
pub mod document;
pub mod error;

pub type System = Arc<InnerSystem>;

#[derive(Clone)]
pub struct InnerSystem {
    db: DatabaseConnection,
}

pub enum Error<E: Send> {
    Database(DbErr),
    Transaction(E),
}

impl<E: Send> From<DbErr> for Error<E> {
    fn from(value: DbErr) -> Self {
        Self::Database(value)
    }
}

impl<E: Send> Debug for Error<E> {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Transaction(_) => f.debug_tuple("Transaction").finish(),
            Self::Database(err) => f.debug_tuple("Database").field(err).finish(),
        }
    }
}

impl<E: Send + Display> std::fmt::Display for Error<E> {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Transaction(inner) => write!(f, "transaction error: {}", inner),
            Self::Database(err) => write!(f, "database error: {err}"),
        }
    }
}

impl<E: Send + Display> std::error::Error for Error<E> {}

impl InnerSystem {
    pub async fn bootstrap(
        database: &openubl_common::config::Database,
    ) -> Result<Self, anyhow::Error> {
        let db_name = &database.db_config.db_name;

        let url = Self::build_url(database);
        println!("bootstrap to {}", &url);
        let db = Database::connect(&url).await?;

        let _drop_db_result = db
            .execute(Statement::from_string(
                db.get_database_backend(),
                format!("DROP DATABASE IF EXISTS \"{}\";", db_name),
            ))
            .await?;

        let _create_db_result = db
            .execute(Statement::from_string(
                db.get_database_backend(),
                format!("CREATE DATABASE \"{}\";", db_name),
            ))
            .await?;

        db.close().await?;

        Self::new(&url).await
    }

    pub async fn with_config(
        database: &openubl_common::config::Database,
    ) -> Result<Self, anyhow::Error> {
        let url = Self::build_url(database);
        Self::new(&url).await
    }

    pub async fn new(url: &str) -> Result<Self, anyhow::Error> {
        println!("connect to {}", url);

        let mut opt = ConnectOptions::new(url);
        opt.min_connections(16);

        let db = Database::connect(opt).await?;

        Migrator::refresh(&db).await?;

        Ok(Self { db })
    }

    fn build_url(database: &openubl_common::config::Database) -> String {
        match database.db_provider {
            DatabaseProvider::Sqlite => {
                if let Some(e) = &database.fs_path {
                    format!("sqlite://{}?mode=rwc", e.display())
                } else {
                    "sqlite::memory:".to_string()
                }
            }
            DatabaseProvider::Postgresql => {
                let username = &database.db_config.username;
                let password = &database.db_config.password;
                let host = &database.db_config.host;
                let port = database.db_config.port;
                let db_name = &database.db_config.db_name;

                format!("postgres://{username}:{password}@{host}:{port}/{db_name}")
            }
        }
    }

    pub(crate) fn connection<'db>(
        &'db self,
        tx: Transactional<'db>,
    ) -> ConnectionOrTransaction<'db> {
        match tx {
            Transactional::None => ConnectionOrTransaction::Connection(&self.db),
            Transactional::Some(tx) => ConnectionOrTransaction::Transaction(tx),
        }
    }

    pub async fn close(self) -> anyhow::Result<()> {
        Ok(self.db.close().await?)
    }

    #[cfg(test)]
    pub async fn for_test() -> Result<Arc<Self>, anyhow::Error> {
        let db = &openubl_common::config::Database {
            db_provider: DatabaseProvider::Sqlite,
            fs_path: None,
            db_config: openubl_common::config::DatabaseConfig {
                username: "".to_string(),
                password: "".to_string(),
                host: "".to_string(),
                port: 1,
                db_name: "".to_string(),
            },
        };
        Self::bootstrap(&db).await.map(Arc::new)
    }
}
