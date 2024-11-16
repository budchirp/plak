use std::{error::Error, fs, path::PathBuf};

use serde::{Deserialize, Serialize};

use crate::toml::Toml;

use super::user_config::UserConfigManager;

#[derive(Deserialize, Serialize, Clone)]
pub struct InstanceConfigStruct {
    pub downloaded: bool,

    pub image_path: String,

    pub name: String,
    pub slug: String,

    pub version: String,

    pub main_class: String,

    pub asset_index: String,
}

#[derive(Clone)]
pub struct InstanceConfigManager {
    toml: Toml,

    instance_dir: PathBuf,
    instance_config_file: PathBuf,
}

impl InstanceConfigManager {
    pub fn new(slug: &str) -> Self {
        let user_config = UserConfigManager::new().get();

        let instances_dir = user_config.data_dir.join("instances");
        let instance_dir = instances_dir.join(slug);
        let instance_config_file = instance_dir.join("instance.toml");

        let _ = fs::create_dir_all(&instance_dir);

        Self {
            toml: Toml::new(&instance_config_file),

            instance_dir,
            instance_config_file,
        }
    }

    fn check_status(&self) -> Result<(), Box<dyn Error>> {
        if !self.instance_config_file.exists() {
            fs::write(&self.instance_config_file, "").expect(&format!(
                "Failed to create file, {}",
                self.instance_config_file.to_str().unwrap()
            ));
        }

        let content = fs::read_to_string(&self.instance_config_file).expect(&format!(
            "Failed to read file, {}",
            self.instance_config_file.to_str().unwrap()
        ));
        if content.is_empty() {
            self.set(&InstanceConfigStruct {
                downloaded: false,

                image_path: "".to_string(),

                name: "".to_string(),
                slug: "".to_string(),

                version: "".to_string(),

                main_class: "".to_string(),

                asset_index: "".to_string(),
            })?;
        }

        Ok(())
    }

    pub fn get(&self) -> InstanceConfigStruct {
        let _ = self.check_status();

        self.toml.parse::<InstanceConfigStruct>()
    }

    pub fn get_all() -> Result<Vec<InstanceConfigStruct>, Box<dyn Error>> {
        let user_config = UserConfigManager::new().get();

        let mut instances = Vec::<InstanceConfigStruct>::new();
        for instance in fs::read_dir(&user_config.data_dir.join("instances"))? {
            let instance = instance?;

            let path = instance.path().join("instance.toml");
            if !path.exists() {
                continue;
            }

            let toml = Toml::new(&path);
            let parsed = toml.parse::<InstanceConfigStruct>();

            instances.push(parsed);
        }

        Ok(instances)
    }

    pub fn set(&self, instance_config: &InstanceConfigStruct) -> Result<(), Box<dyn Error>> {
        self.toml.set::<InstanceConfigStruct>(&instance_config)
    }

    pub fn delete(&self) -> Result<(), Box<dyn Error>> {
        fs::remove_dir_all(&self.instance_dir).expect(
            format!(
                "Failed to remove instance, {}",
                self.instance_dir.to_str().unwrap()
            )
            .as_str(),
        );

        Ok(())
    }
}
