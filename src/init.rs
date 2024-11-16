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
        self.create_instances_dir()?;

        Ok(())
    }

    pub fn create_config_dir(&self) -> Result<(), Box<dyn Error>> {
        if !self.config.config_dir.exists() {
            fs::create_dir_all(&self.config.config_dir)?;
        }

        Ok(())
    }

    pub fn create_instances_dir(&self) -> Result<(), Box<dyn Error>> {
        if !self.user_config_struct.instances_dir.exists() {
            fs::create_dir_all(&self.user_config_struct.instances_dir)?;
        }

        Ok(())
    }
}
