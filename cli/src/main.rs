use std::env;
use std::fs::create_dir_all;
use std::process::{ExitCode, Termination};

use clap::Parser;
use tokio::task::{spawn_local, LocalSet};

#[allow(clippy::large_enum_variant)]
#[derive(clap::Subcommand, Debug)]
pub enum Command {
    Server(openubl_server::ServerRun),
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
    let db_dir = work_dir.join("db");
    let data_dir = work_dir.join("data");
    create_dir_all(&db_dir)?;
    create_dir_all(&data_dir)?;

    let provider = "sqlite";
    let db_path = db_dir.join("db.sqlite").display().to_string();
    log::info!("Database installed in {:?}", db_dir);

    let api = Cli::parse_from([
        "cli",
        "server",
        "--db-provider",
        provider,
        "--db-fs-path",
        &db_path,
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
