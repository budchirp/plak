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
use uuid::Uuid;

use crate::{
    command_line::CommandLine,
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

        Self {
            network: Network::new(),

            user_config_struct: user_config_struct.clone(),

            instance_config_manager: InstanceConfigManager::new(&slug),

            game_dir: user_config_struct
                .data_dir
                .join("instances")
                .join(&slug)
                .join("minecraft"),
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
        let instance_config_struct = self.instance_config_manager.get();

        let client_jar = self
            .clients_dir
            .join(&format!("client-{}.jar", &instance_config_struct.version))
            .to_string_lossy()
            .to_string();

        let mut classpath = String::new();
        classpath.push_str(&format!("{}:", &client_jar));

        let libraries_dir = self.libraries_dir.join(&instance_config_struct.version);
        self.collect_jar_dirs(&libraries_dir, &mut classpath);

        classpath.trim_end_matches(':').to_string();
        classpath
    }

    fn craft_arguments(
        &self,
        offline: bool,
        username: &str,
        token: Option<&str>,
        uuid: Option<&str>,
    ) -> Vec<String> {
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

        let offline_uuid = Uuid::new_v4().to_string();
        let game_args = vec![
            "--version",
            &instance_config_struct.version,
            "--username",
            username,
            "--uuid",
            // TODO: temporary
            uuid.unwrap_or(offline_uuid.as_str()),
            "--accessToken",
            token.unwrap_or("gibberish"),
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

    pub fn launch(
        &self,
        offline: bool,
        // TODO: Instead of getting these like that, get profile id and read from toml
        username: &str,
        token: Option<&str>,
        uuid: Option<&str>,
    ) -> Result<CommandLine, Box<dyn Error>> {
        let mut binary = self.user_config_struct.java_bin.clone();
        if self.user_config_struct.use_gamemoderun {
            binary = CommandLine::get_path("gamemoderun");
        }

        let mut command = Command::new(binary);
        if self.user_config_struct.use_gamemoderun {
            command.arg(&self.user_config_struct.java_bin);
        }

        command.args(&self.craft_arguments(offline, username, token, uuid));

        if self.user_config_struct.use_dedicated_gpu {
            command
                .env("__NV_PRIME_RENDER_OFFLOAD", "1")
                .env("__VK_LAYER_NV_optimus", "NVIDIA_only")
                .env("__GLX_VENDOR_LIBRARY_NAME", "nvidia");
        }

        Ok(CommandLine::new(command.spawn()?))
    }

    pub fn download(&self, version: &str) -> Result<(), Box<dyn Error>> {
        let manifest = self.network.fetch::<VersionManifest>(
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json",
        )?;

        let selected_version = manifest.versions.iter().find(|v| v.id == version).unwrap();
        let version_details = self
            .network
            .fetch::<VersionDetails>(&selected_version.url)?;

        self.download_client(&version_details, version)?;
        self.download_libraries(&version_details, version)?;
        self.download_assets(&version_details)?;

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

    fn download_client(
        &self,
        details: &VersionDetails,
        version: &str,
    ) -> Result<(), Box<dyn Error>> {
        self.network.download(
            &details.downloads.client.url,
            self.clients_dir.join(format!("client-{}.jar", version)),
        )
    }

    fn download_libraries(
        &self,
        details: &VersionDetails,
        version: &str,
    ) -> Result<(), Box<dyn Error>> {
        let libraries_dir = self.libraries_dir.join(version);
        let natives_dir = self.natives_dir.join(version);

        fs::create_dir_all(&libraries_dir)?;
        fs::create_dir_all(&natives_dir)?;

        let network = Arc::new(self.network.clone());
        let libraries_dir = Arc::new(libraries_dir);
        let natives_dir = Arc::new(natives_dir);

        details.libraries.par_iter().for_each(|library| {
            if let Some(artifact) = &library.downloads.artifact {
                let path = libraries_dir.join(&artifact.path);
                fs::create_dir_all(path.parent().unwrap()).unwrap();

                if let Err(error) = network.download(&artifact.url, path) {
                    eprintln!("Failed to download {}: {}", &artifact.url, error);
                }
            }

            if let Some(classifiers) = &library.downloads.classifiers {
                if let Some(natives_linux) = &classifiers.natives_linux {
                    let zip_file =
                        natives_dir.join(&Path::new(&natives_linux.url).file_name().unwrap());

                    if let Err(error) = network.download(&natives_linux.url, zip_file.clone()) {
                        eprintln!(
                            "Failed to download {}: {}",
                            zip_file.to_path_buf().to_string_lossy(),
                            error
                        );
                    }

                    Command::new("unzip")
                        .arg("-o")
                        .arg(&zip_file)
                        .arg("-d")
                        .arg(natives_dir.clone().to_path_buf())
                        .status()
                        .expect(&format!("Failed to unzip {}", zip_file.to_string_lossy()));
                    fs::remove_file(&zip_file)
                        .expect(&format!("Failed to remove {}", zip_file.to_string_lossy()));
                }
            }
        });

        Ok(())
    }

    fn download_assets(&self, details: &VersionDetails) -> Result<(), Box<dyn Error>> {
        let assets_dir = self.assets_dir.join(&details.asset_index.id);
        let objects_dir = assets_dir.join("objects");
        let indexes_dir = assets_dir.join("indexes");

        fs::create_dir_all(&objects_dir)?;
        fs::create_dir_all(&indexes_dir)?;

        let index_path = indexes_dir.join(format!("{}.json", &details.asset_index.id));
        self.network
            .download(&details.asset_index.url, index_path)?;

        let asset_index: AssetIndexFile = self.network.fetch(&details.asset_index.url)?;
        let network = Arc::new(self.network.clone());
        let objects_dir = Arc::new(objects_dir);

        asset_index.objects.par_iter().for_each(|(_, asset)| {
            let asset_url = format!(
                "https://resources.download.minecraft.net/{}/{}",
                &asset.hash[..2],
                asset.hash
            );
            let asset_path = objects_dir.join(&asset.hash[..2]).join(&asset.hash);

            fs::create_dir_all(asset_path.parent().unwrap()).unwrap();

            if let Err(error) = network.download(&asset_url, asset_path.clone()) {
                eprintln!(
                    "Failed to download {}: {}",
                    asset_path.to_string_lossy(),
                    error
                );
            }
        });

        Ok(())
    }
}
