use std::env;
use std::fs::create_dir_all;
use std::process::{ExitCode, Termination};
use std::time::Duration;

use clap::Parser;
use postgresql_embedded::PostgreSQL;
use tokio::task::{spawn_local, LocalSet};

#[allow(clippy::large_enum_variant)]
#[derive(clap::Subcommand, Debug)]
pub enum Command {
    Server(openubl_server::ServerRun),
    // Sender(SenderRun),
}

#[derive(clap::Parser, Debug)]
#[command(
    author,
    version = env ! ("CARGO_PKG_VERSION"),
    about = "openubl",
    long_about = None
)]
pub struct Cli {
    #[command(subcommand)]
    pub(crate) command: Option<Command>,
}

impl Cli {
    async fn run(self) -> anyhow::Result<ExitCode> {
        match self.command {
            Some(Command::Server(run)) => run.run().await,
            None => dev_mode().await,
        }
    }
}

async fn dev_mode() -> anyhow::Result<ExitCode> {
    log::warn!("Setting up managed DB; not suitable for production use!");

    let current_dir = env::current_dir()?;
    let work_dir = current_dir.join(".openubl");
    let db_dir = work_dir.join("postgres");
    let data_dir = work_dir.join("data");
    create_dir_all(&data_dir)?;
    let settings = postgresql_embedded::Settings {
        username: "postgres".to_string(),
        password: "openubl".to_string(),
        temporary: false,
        installation_dir: db_dir.clone(),
        timeout: Some(Duration::from_secs(30)),
        data_dir,
        ..Default::default()
    };
    let mut postgresql = PostgreSQL::new(PostgreSQL::default_version(), settings);
    postgresql.setup().await?;
    postgresql.start().await?;

    let database_name = "openubl";
    if !postgresql.database_exists(database_name).await? {
        postgresql.create_database(database_name).await?;
    }

    let port = postgresql.settings().port;
    let username = &postgresql.settings().username;
    let password = &postgresql.settings().password;

    log::info!("PostgreSQL installed in {:?}", db_dir);
    log::info!("Running on port {}", port);

    let api = Cli::parse_from([
        "cli",
        "server",
        "--db-name",
        database_name,
        "--db-user",
        username,
        "--db-password",
        password,
        "--db-port",
        &port.to_string(),
        "local",
        "--storage-local-dir",
        ".openubl/storage",
    ]);

    LocalSet::new()
        .run_until(async { spawn_local(api.run()).await? })
        .await
}

#[actix_web::main]
async fn main() -> impl Termination {
    Cli::parse().run().await
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn verify_cli() {
        use clap::CommandFactory;
        Cli::command().debug_assert();
    }
}
