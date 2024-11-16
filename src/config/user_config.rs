use core::str;
use std::{error::Error, fs, path::PathBuf, process::Command};

use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::toml::Toml;

use super::Config;

#[derive(Deserialize, Serialize, Clone)]
pub struct UserConfigStruct {
    pub is_initial_setup_done: bool,

    pub instances_dir: PathBuf,

    pub username: String,
    pub uuid: String,
    pub token: String,

    pub max_memory: String,
    pub initial_memory: String,

    pub java_bin: String,
    pub jvm_args: String,

    pub use_dedicated_gpu: bool,
}

#[derive(Clone)]
pub struct UserConfigManager {
    toml: Toml,

    config: Config,
}

impl UserConfigManager {
    pub fn new() -> Self {
        let config = Config::new();

        let _ = fs::create_dir_all(&config.config_dir);

        Self {
            toml: Toml::new(&config.config_file),
            config,
        }
    }

    pub fn check_status(&self) -> Result<(), Box<dyn Error>> {
        if !self.config.config_file.exists() {
            fs::write(&self.config.config_file, "").expect(&format!(
                "Failed to create file, {}",
                self.config.config_file.to_str().unwrap()
            ));
        }

        let content = fs::read_to_string(&self.config.config_file).expect(&format!(
            "Failed to read file, {}",
            self.config.config_file.to_str().unwrap()
        ));
        if content.is_empty() {
            let new_user_config = UserConfigStruct {
                is_initial_setup_done: false,

                instances_dir: dirs::data_dir().unwrap().join("plak/instances"),

                username: "steve".to_string(),
                uuid: Uuid::new_v4().to_string(),
                token: "gibberish".to_string(),

                max_memory: "2G".to_string(),
                initial_memory: "1G".to_string(),

                java_bin: Command::new("which")
                    .arg("java")
                    .output()
                    .map(|output| {
                        str::from_utf8(&output.stdout)
                            .unwrap_or("java")
                            .trim()
                            .to_string()
                    })
                    .unwrap_or_else(|_| "java".to_string()),
                jvm_args: "".to_string(),

                use_dedicated_gpu: false,
            };

            self.set(&new_user_config)?;
        }

        Ok(())
    }

    pub fn get(&self) -> UserConfigStruct {
        let _ = self.check_status();

        self.toml.parse::<UserConfigStruct>()
    }

    pub fn set(&self, config: &UserConfigStruct) -> Result<(), Box<dyn Error>> {
        self.toml.set::<UserConfigStruct>(&config)
    }
}
