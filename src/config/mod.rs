pub mod instance_config;
pub mod user_config;

use crate::constants;
use std::path::PathBuf;

#[derive(Clone)]
#[allow(dead_code)]
pub struct Config {
    pub config_dir: PathBuf,
    pub config_file: PathBuf,
}

impl Config {
    pub fn new() -> Self {
        let config_dir = dirs::config_dir().unwrap().join(constants::CONFIG_DIR);
        let config_file = config_dir.join(constants::CONFIG_FILE);

        Self {
            config_dir,
            config_file,
        }
    }
}
