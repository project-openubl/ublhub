use std::{
    fmt::{Display, Formatter},
    path::PathBuf,
};

#[derive(Debug, Clone, clap::ValueEnum)]
pub enum DatabaseProvider {
    Sqlite,
    Postgresql,
}

impl Display for DatabaseProvider {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            DatabaseProvider::Sqlite => write!(f, "sqlite"),
            DatabaseProvider::Postgresql => write!(f, "postgresql"),
        }
    }
}

#[derive(clap::Args, Debug, Clone)]
#[command(next_help_heading = "Database")]
pub struct Database {
    #[arg(
        id = "db-provider",
        long,
        env = "DB_PROVIDER",
        requires_ifs([("postgresql", "db-user"), ("postgresql", "db-password"), ("postgresql", "db-host"), ("postgresql", "db-port"), ("postgresql", "db-name")]),
        default_value_t = DatabaseProvider::Sqlite
    )]
    pub db_provider: DatabaseProvider,

    #[arg(
        id = "db-fs-path",
        long,
        env = "DB_FS_PATH",
        conflicts_with = "postgresql",
        help_heading = "Sqlite"
    )]
    pub fs_path: Option<PathBuf>,

    #[command(flatten)]
    pub db_config: DatabaseConfig,
}

#[derive(Clone, Debug, Default, clap::Args)]
#[command(next_help_heading = "Postgresql")]
#[group(id = "postgresql", requires = "db-provider")]
pub struct DatabaseConfig {
    #[arg(id = "db-user", long, env = "DB_USER", default_value = "postgres")]
    pub username: String,
    #[arg(
        id = "db-password",
        long,
        env = "DB_PASSWORD",
        default_value = "password"
    )]
    pub password: String,
    #[arg(id = "db-host", long, env = "DB_HOST", default_value = "localhost")]
    pub host: String,
    #[arg(id = "db-port", long, env = "DB_PORT", default_value_t = 5432)]
    pub port: u16,
    #[arg(id = "db-name", long, env = "DB_NAME", default_value = "openubl")]
    pub db_name: String,
}
