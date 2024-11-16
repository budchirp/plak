use std::{error::Error, fs::File, io::copy, path::PathBuf};

use reqwest::blocking::Client;
use serde::de::DeserializeOwned;

#[derive(Clone)]
pub struct Network {
    client: Client,
}

impl Network {
    pub fn new() -> Self {
        Self {
            client: Client::new(),
        }
    }

    pub fn fetch<T: DeserializeOwned>(&self, url: &str) -> Result<T, Box<dyn Error>> {
        let response = self
            .client
            .get(url)
            .send()?
            .json::<T>()
            .expect(&format!("Failed to fetch {}", url));

        println!("Fetched {}", url);

        Ok(response)
    }

    pub fn download(&self, url: &str, path: PathBuf) -> Result<(), Box<dyn Error>> {
        let mut response = self
            .client
            .get(url)
            .send()
            .expect(&format!("Failed to fetch {}", url));
        let mut file = File::create(path.clone())?;

        copy(&mut response, &mut file).expect(&format!(
            "Failed to copy downloaded file to {}",
            &path.to_str().unwrap()
        ));

        println!("Downloaded {}", url);

        Ok(())
    }
}
