use std::{error::Error, fs, path::PathBuf};

use serde::{de::DeserializeOwned, Serialize};

#[derive(Clone)]
pub struct Toml {
    path: PathBuf
}

impl Toml {
    pub fn new(path: &PathBuf) -> Self {
        Self {
            path: path.clone()
        }
    }

    pub fn parse<T : DeserializeOwned>(&self) -> T {
        let content = fs::read_to_string(&self.path).expect(&format!(
            "Failed to read file, {}",
            self.path.to_str().unwrap()
        ));

        toml::from_str(&content).expect("Failed to parse TOML!")
    }

    pub fn set<T : Serialize>(&self, data: &T) -> Result<(), Box<dyn Error>> {
        let content = toml::to_string(data).expect("Failed to convert struct to TOML!");
        fs::write(&self.path, &content).expect(&format!(
            "Failed to write to file, {}",
            &self.path.to_str().unwrap()
        ));

        Ok(())
    }
}
