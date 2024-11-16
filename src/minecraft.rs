use rayon::prelude::*;
use serde::Deserialize;
use std::{
    collections::HashMap,
    error::Error,
    fs,
    path::{Path, PathBuf},
    process::Command,
    sync::Arc,
};

use crate::{
    config::{
        instance_config::InstanceConfigManager,
        user_config::{UserConfigManager, UserConfigStruct},
    },
    network::Network,
};

#[derive(Deserialize)]
pub struct VersionManifest {
    pub versions: Vec<Version>,
}

#[derive(Deserialize)]
pub struct Version {
    pub id: String,
    pub url: String,
}

#[derive(Deserialize)]
pub struct VersionDetails {
    downloads: DownloadDetails,
    libraries: Vec<Library>,
    #[serde(rename = "assetIndex")]
    pub asset_index: AssetIndex,
    #[serde(rename = "mainClass")]
    pub main_class: String,
}

#[derive(Deserialize)]
struct DownloadDetails {
    client: Download,
}

#[derive(Deserialize)]
struct Download {
    url: String,
}

#[derive(Deserialize)]
struct Library {
    downloads: LibraryDownload,
}

#[derive(Deserialize)]
struct ArtifactDownload {
    url: String,
    path: String,
}

#[derive(Deserialize)]
struct LibraryDownload {
    artifact: Option<ArtifactDownload>,
    classifiers: Option<Classifiers>,
}

#[derive(Deserialize)]
pub struct AssetIndex {
    url: String,
    pub id: String,
}

#[derive(Deserialize)]
struct Classifiers {
    #[serde(rename = "natives-linux")]
    natives_linux: Option<Download>,
}

#[derive(Deserialize)]
struct AssetIndexFile {
    objects: HashMap<String, AssetObject>,
}

#[derive(Deserialize, Clone)]
struct AssetObject {
    hash: String,
}

#[derive(Clone)]
pub struct Minecraft {
    network: Network,

    user_config_struct: UserConfigStruct,

    instance_config_manager: InstanceConfigManager,

    game_dir: PathBuf,
    clients_dir: PathBuf,
    libraries_dir: PathBuf,
    natives_dir: PathBuf,
    assets_dir: PathBuf,

    slug: String,
}

impl Minecraft {
    pub fn new(slug: &str) -> Self {
        let user_config_struct = UserConfigManager::new().get();

        let instance_config_manager = InstanceConfigManager::new(&slug);

        Self {
            network: Network::new(),

            user_config_struct: user_config_struct.clone(),

            instance_config_manager: instance_config_manager.clone(),

            game_dir: user_config_struct.data_dir.join("instances").join(&slug).join("minecraft"),
            clients_dir: user_config_struct.data_dir.join("clients"),
            libraries_dir: user_config_struct.data_dir.join("libraries"),
            natives_dir: user_config_struct.data_dir.join("natives"),
            assets_dir: user_config_struct.data_dir.join("assets"),

            slug: slug.to_string(),
        }
    }

    fn collect_jar_dirs(&self, path: &Path, classpath: &mut String) {
        if path.is_dir() {
            let mut contains_jar = false;
            if let Ok(entries) = fs::read_dir(path) {
                for entry in entries {
                    if let Ok(entry) = entry {
                        let entry_path = entry.path();
                        if entry_path.is_dir() {
                            self.collect_jar_dirs(&entry_path, classpath);
                        } else if let Some(extension) = entry_path.extension() {
                            if extension == "jar" {
                                contains_jar = true;
                            }
                        }
                    }
                }
            }

            if contains_jar {
                classpath.push_str(&format!("{}/*:", path.to_str().unwrap()));
            }
        }
    }

    fn get_classpath(&self) -> String {
        let mut classpath = String::new();

        let instance_config_struct = self.instance_config_manager.get();

        let client_jar = self
            .clients_dir
            .join(&format!("client-{}.jar", &instance_config_struct.version))
            .to_string_lossy()
            .to_string();

        classpath.push_str(&format!("{}:", &client_jar));

        let libraries_dir = self.libraries_dir.join(&instance_config_struct.version);
        self.collect_jar_dirs(&libraries_dir, &mut classpath);

        if classpath.ends_with(':') {
            classpath.pop();
        }

        classpath
    }

    fn craft_arguments(&self, offline: bool) -> Vec<String> {
        let instance_config_struct = self.instance_config_manager.get();

        let natives_dir = self
            .natives_dir
            .join(&instance_config_struct.version)
            .to_string_lossy()
            .to_string();
        let asssets_dir = self
            .assets_dir
            .join(&instance_config_struct.asset_index)
            .to_string_lossy()
            .to_string();
        let game_dir = self.game_dir.to_string_lossy().to_string();

        let game_args = vec![
            "--version",
            &instance_config_struct.version,
            "--username",
            &self.user_config_struct.username,
            "--uuid",
            &self.user_config_struct.uuid,
            "--accessToken",
            &self.user_config_struct.token,
            "--userType",
            if offline { "legacy" } else { "mojang" },
            "--assetIndex",
            &instance_config_struct.asset_index,
            "--assetsDir",
            &asssets_dir,
            "--gameDir",
            &game_dir,
        ];

        let mut args = vec![
            format!("-Djava.library.path={}", &natives_dir),
            "-cp".to_string(),
            self.get_classpath(),
            instance_config_struct.main_class,
            format!("-Xmx{}", &self.user_config_struct.max_memory),
            format!("-Xms{}", &self.user_config_struct.initial_memory),
            self.user_config_struct.jvm_args.clone(),
        ];

        args.extend(game_args.iter().map(|&arg| arg.to_string()));

        args
    }

    pub fn launch(&self, offline: bool) -> Result<(), Box<dyn Error>> {
        let mut command = Command::new(&self.user_config_struct.java_bin);
        command.args(&self.craft_arguments(offline));

        if self.user_config_struct.use_dedicated_gpu {
            command
                .env("__NV_PRIME_RENDER_OFFLOAD", "1")
                .env("__VK_LAYER_NV_optimus", "NVIDIA_only")
                .env("__GLX_VENDOR_LIBRARY_NAME", "nvidia");
        }

        let status = command.status()?;
        if status.success() {
            Ok(())
        } else {
            Err(format!("Status code: {}", &status.code().unwrap()).into())
        }
    }

    pub fn download(&self, version: &str) -> Result<(), Box<dyn Error>> {
        let manifest = self.network.fetch::<VersionManifest>(
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json",
        )?;

        let selected_version = manifest.versions.iter().find(|v| v.id == version).unwrap();
        let version_details = self
            .network
            .fetch::<VersionDetails>(&selected_version.url)?;

        self.network.download(
            &version_details.downloads.client.url,
            self.clients_dir.join(&format!("client-{}.jar", &version)),
        )?;

        let libraries_dir = self.libraries_dir.join(&version);
        let natives_dir = self.natives_dir.join(&version);
        fs::create_dir_all(&libraries_dir)?;
        fs::create_dir_all(&natives_dir)?;
        for library in version_details.libraries {
            if let Some(artifact) = library.downloads.artifact {
                let path = libraries_dir.join(&artifact.path);
                fs::create_dir_all(path.parent().unwrap())?;

                self.network.download(&artifact.url, path)?;
            };

            if let Some(classifiers) = library.downloads.classifiers {
                if let Some(natives_linux) = classifiers.natives_linux {
                    let path =
                        &natives_dir.join(&Path::new(&natives_linux.url).file_name().unwrap());
                    self.network.download(&natives_linux.url, path.clone())?;

                    Command::new("unzip")
                        .arg("-o")
                        .arg(&path)
                        .arg("-d")
                        .arg(&natives_dir)
                        .status()
                        .expect(&format!("Failed to unzip {}", path.to_str().unwrap()));
                    fs::remove_file(&path)
                        .expect(&format!("Failed to remove {}", path.to_str().unwrap()));
                }
            }
        }

        let assets_dir = self.assets_dir.join(&version_details.asset_index.id);
        let objects_dir = assets_dir.join("objects");
        let indexes_dir = assets_dir.join("indexes");
        fs::create_dir_all(&objects_dir)?;
        fs::create_dir_all(&indexes_dir)?;

        self.network.download(
            &version_details.asset_index.url,
            indexes_dir.join(&format!("{}.json", &version_details.asset_index.id)),
        )?;

        let asset_index = self
            .network
            .fetch::<AssetIndexFile>(&version_details.asset_index.url)
            .unwrap();

        let asset_list: Vec<(String, AssetObject)> = asset_index.objects.into_iter().collect();
        let network = Arc::new(self.network.clone());
        let objects_dir = Arc::new(objects_dir);
        asset_list.par_iter().for_each(|(_, asset)| {
            let asset_url = format!(
                "https://resources.download.minecraft.net/{}/{}",
                &asset.hash[..2],
                asset.hash
            );
            let asset_path = objects_dir.join(&asset.hash[..2]);
            fs::create_dir_all(&asset_path).unwrap();

            if let Err(e) = network.download(&asset_url, asset_path.join(&asset.hash)) {
                eprintln!("Failed to download {}: {}", asset_url, e);
            }
        });

        let mut new_instance_config_struct = self.instance_config_manager.get();
        new_instance_config_struct.downloaded = true;
        new_instance_config_struct.version = version.to_string();
        new_instance_config_struct.asset_index = version_details.asset_index.id;
        new_instance_config_struct.main_class = version_details.main_class;

        self.instance_config_manager
            .set(&new_instance_config_struct)
            .expect(&format!("Failed to create instance.toml for {}", self.slug));

        Ok(())
    }
}
