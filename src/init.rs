use std::{error::Error, fs};

use crate::config::{
    user_config::{UserConfigManager, UserConfigStruct},
    Config,
};

pub struct Init {
    config: Config,
    user_config_struct: UserConfigStruct,
}

impl Init {
    pub fn new() -> Self {
        Self {
            config: Config::new(),
            user_config_struct: UserConfigManager::new().get(),
        }
    }

    pub fn initialize(&self) -> Result<(), Box<dyn Error>> {
        self.create_config_dir()?;
        self.create_data_dir()?;

        Ok(())
    }

    pub fn create_config_dir(&self) -> Result<(), Box<dyn Error>> {
        if !self.config.config_dir.exists() {
            fs::create_dir_all(&self.config.config_dir)?;
        }

        Ok(())
    }

    pub fn create_data_dir(&self) -> Result<(), Box<dyn Error>> {
        if !self.user_config_struct.data_dir.exists() {
            fs::create_dir_all(&self.user_config_struct.data_dir)?;
        }

        if !self.user_config_struct.data_dir.join("instances").exists() {
            fs::create_dir_all(&self.user_config_struct.data_dir.join("instances"))?;
        }

        if !self.user_config_struct.data_dir.join("clients").exists() {
            fs::create_dir_all(&self.user_config_struct.data_dir.join("clients"))?;
        }

        if !self.user_config_struct.data_dir.join("libraries").exists() {
            fs::create_dir_all(&self.user_config_struct.data_dir.join("libraries"))?;
        }

        if !self.user_config_struct.data_dir.join("natives").exists() {
            fs::create_dir_all(&self.user_config_struct.data_dir.join("natives"))?;
        }

        if !self.user_config_struct.data_dir.join("assets").exists() {
            fs::create_dir_all(&self.user_config_struct.data_dir.join("assets"))?;
        }

        Ok(())
    }
}
