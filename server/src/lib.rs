use std::fmt::Debug;
use std::process::ExitCode;
use std::sync::Arc;

use actix_multipart::form::tempfile::TempFileConfig;
use actix_web::middleware::Logger;
use actix_web::{web, App, HttpServer};

use openubl_api::system::InnerSystem;
use openubl_common::config::Database;
use openubl_storage::StorageSystem;

use crate::server::credentials::{
    create_credentials, delete_credentials, get_credentials, list_credentials, update_credentials,
};
use crate::server::document::{get_document_file, list_documents, send_document};
use crate::server::health;

mod dto;
pub mod server;

/// Run the API server
#[derive(clap::Args, Debug)]
pub struct ServerRun {
    #[arg(short, long, env, default_value = "[::1]:8080")]
    pub bind_addr: String,

    #[command(flatten)]
    pub database: Database,

    #[arg(long, env)]
    pub bootstrap: bool,

    // #[command(flatten)]
    // pub oidc: openubl_oidc::config::Oidc,
    #[command(subcommand)]
    pub storage: openubl_storage::config::Storage,
}

impl ServerRun {
    pub async fn run(self) -> anyhow::Result<ExitCode> {
        env_logger::init();

        // Oidc
        // let oidc = Oidc::new(OidcConfig::Issuer(self.oidc.auth_server_url.clone().into()))
        //     .await
        //     .unwrap();
        // let oidc_validator = OidcBiscuitValidator {
        //     options: ValidationOptions {
        //         issuer: Validation::Validate(self.oidc.auth_server_url.clone()),
        //         ..ValidationOptions::default()
        //     },
        // };

        // Database
        let system = match self.bootstrap {
            true => {
                InnerSystem::bootstrap(
                    &self.database.username,
                    &self.database.password,
                    &self.database.host,
                    self.database.port,
                    &self.database.name,
                )
                .await?
            }
            false => InnerSystem::with_config(&self.database).await?,
        };

        // Storage
        let storage = StorageSystem::new(&self.storage).await?;

        // Search Engine
        // let search_engine = SearchEngineSystem::new(&self.search_engine).await?;

        let app_state = Arc::new(AppState { system, storage });

        HttpServer::new(move || {
            App::new()
                .app_data(web::Data::from(app_state.clone()))
                .wrap(Logger::default())
                // .wrap(oidc_validator.clone())
                // .app_data(oidc.clone())
                .app_data(TempFileConfig::default())
                .configure(configure)
        })
        .bind(self.bind_addr)?
        .run()
        .await?;

        Ok(ExitCode::SUCCESS)
    }
}

pub struct AppState {
    pub system: InnerSystem,
    pub storage: StorageSystem,
    // pub search_engine: SearchEngineSystem,
}

pub fn configure(config: &mut web::ServiceConfig) {
    // Health
    config.service(health::liveness);
    config.service(health::readiness);

    // Files
    // config.service(files::upload_file);

    // Documents
    config.service(list_documents);
    config.service(get_document_file);
    config.service(send_document);

    // Credentials
    config.service(list_credentials);
    config.service(create_credentials);
    config.service(get_credentials);
    config.service(update_credentials);
    config.service(delete_credentials);
}
