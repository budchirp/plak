package me.budchirp.plak.data.remote.manager

import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.budchirp.plak.APP_NAME
import me.budchirp.plak.APP_VERSION
import me.budchirp.plak.data.manager.ConfigManager
import me.budchirp.plak.data.manager.InstanceManager
import me.budchirp.plak.data.model.Account
import me.budchirp.plak.data.model.AssetIndexFile
import me.budchirp.plak.data.model.Instance
import me.budchirp.plak.data.model.VersionDetails
import me.budchirp.plak.data.model.VersionManifest
import me.budchirp.plak.utils.Commands
import me.budchirp.plak.utils.Network
import me.budchirp.plak.utils.OS
import me.budchirp.plak.utils.OperatingSystems
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists

class MinecraftManager {
    object URLs {
        val VERSION_MANIFEST = Url("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
    }

    private val network = Network()
    private val instanceManager = InstanceManager()
    private val configManager = ConfigManager()

    private val config = configManager.get()

    private val clientsDir = Path(config.dataDir).resolve("clients")
    private val librariesDir = Path(config.dataDir).resolve("libraries")
    private val nativesDir = Path(config.dataDir).resolve("natives")
    private val assetsDir = Path(config.dataDir).resolve("assets")

    suspend fun getVersions(): List<String> {
        val versionManifest = network.fetch<VersionManifest>(url = URLs.VERSION_MANIFEST)

        return versionManifest.versions.map { it.id }
    }

    private fun getClasspath(instance: Instance): String {
        val clientJar = clientsDir
            .resolve("client-${instance.version}.jar")
            .toString()

        val classpathEntries = mutableListOf(clientJar)

        val librariesDir = librariesDir.resolve(instance.version)
        Files.walk(librariesDir)
            .use { stream ->
                stream.filter { path ->
                    Files.isRegularFile(path) && path.fileName.toString().endsWith(".jar")
                }
                    .forEach { jarPath ->
                        classpathEntries.add(jarPath.toString())
                    }
            }

        return classpathEntries.joinToString(File.pathSeparator)
    }

    private fun craftArgs(instance: Instance, account: Account): List<String> {
        val gameDir = Path(config.dataDir).resolve("instances").resolve(instance.slug).resolve("minecraft")
        val versionNativesDir = nativesDir.resolve(instance.version)

        val assetIndex = instance.assetIndex.toString()
        val gameArgs = listOf<String>(
            "--version", instance.version,
            "--username", account.username,
            "--uuid", account.uuid,
            "--accessToken", account.token,
            "--userType", if (account.offline) "legacy" else "mojang",
            "--assetIndex", assetIndex,
            "--assetsDir", assetsDir.resolve(assetIndex).toString(),
            "--gameDir", gameDir.toString(),
        )

        val jvmArgs = mutableListOf<String>(
            "-Djava.library.path=$versionNativesDir",
            "-Djna.tmpdir=$versionNativesDir",
            "-Dminecraft.launcher.brand=$APP_NAME",
            "-Dminecraft.launcher.version=${APP_VERSION}",
            "-cp", getClasspath(instance),
            "-Xmx${instance.jvm.maxMemory}",
            "-Xms${instance.jvm.minMemory}",
            instance.mainClass.toString()
        )

        jvmArgs.addAll(instance.jvm.jvmArgs.split("\\s+".toRegex()))

        return jvmArgs + gameArgs
    }

    fun launch(instance: Instance, account: Account, onSuccess: () -> Unit = {}) {
        val args = mutableListOf(instance.jvm.jvmPath)

        if (config.launch.useGamemodeRun) args.add(0, "gamemoderun")
        if (config.launch.useMangoHUD) args.add(0, "mangohud")

        args.addAll(craftArgs(instance, account))

        args.forEach {
            println(it)
        }

        ProcessBuilder(args).inheritIO()
            .start()

        onSuccess()
    }

    suspend fun download(instance: Instance, onSuccess: () -> Unit = {}) {
        val versionManifest = network.fetch<VersionManifest>(url = URLs.VERSION_MANIFEST)

        val version = versionManifest.versions.first { it.id == instance.version }
        val versionDetails = network.fetch<VersionDetails>(url = Url(version.url))

        downloadClient(instance = instance, versionDetails = versionDetails, version = version.id)
        downloadLibraries(instance = instance, versionDetails = versionDetails, version = version.id)
        downloadAssets(instance = instance, versionDetails = versionDetails)

        instanceManager.set(
            instance = instance.copy(
                downloaded = true,
                mainClass = versionDetails.mainClass,
                assetIndex = versionDetails.assetIndex.id
            )
        )

        onSuccess()
    }

    private suspend fun downloadClient(instance: Instance, versionDetails: VersionDetails, version: String) {
        println("Downloading client")

        clientsDir.createDirectories()

        val jar = clientsDir.resolve("client-${version}.jar")
        if (instance.downloaded && jar.exists()) return

        network.download(url = Url(versionDetails.downloads.client.url), destination = jar)
    }

    private suspend fun downloadLibraries(instance: Instance, versionDetails: VersionDetails, version: String): Unit =
        coroutineScope {
            println("Downloading libraries")

            librariesDir.createDirectories()
            nativesDir.createDirectories()

            val versionLibsDir = librariesDir.resolve(version)
            if (instance.downloaded && versionLibsDir.exists()) return@coroutineScope
            versionLibsDir.createDirectories()

            val versionNativesDir = nativesDir.resolve(version)
            versionNativesDir.createDirectories()

            versionDetails.libraries.map { library ->
                async {
                    library.downloads.artifact?.let { artifact ->
                        val file = versionLibsDir.resolve(artifact.path)
                        file.parent.createDirectories()

                        network.download(url = Url(artifact.url), destination = file)
                    }

                    when (OS.getOS()) {
                        OperatingSystems.LINUX -> {
                            library.downloads.classifiers?.nativesLinux?.let { native ->
                                val file = versionNativesDir.resolve(native.url.substringAfterLast('/'))
                                network.download(url = Url(native.url), destination = file)

                                Commands.unzip(file = file, destination = versionNativesDir)
                                file.deleteExisting()
                            }
                        }

                        OperatingSystems.MAC -> {
                            library.downloads.classifiers?.nativesMacOS?.let { native ->
                                val file = versionNativesDir.resolve(native.url.substringAfterLast('/'))
                                network.download(url = Url(native.url), destination = file)

                                Commands.unzip(file = file, destination = versionNativesDir)
                                file.deleteExisting()
                            }
                        }

                        OperatingSystems.WINDOWS -> {
                            library.downloads.classifiers?.nativesWindows?.let { native ->
                                val file = versionNativesDir.resolve(native.url.substringAfterLast('/'))
                                network.download(url = Url(native.url), destination = file)

                                Commands.unzip(file = file, destination = versionNativesDir)
                                file.deleteExisting()
                            }
                        }

                        else -> {}
                    }
                }
            }.awaitAll()
        }

    private suspend fun downloadAssets(instance: Instance, versionDetails: VersionDetails): Unit = coroutineScope {
        println("Downloading assets")

        assetsDir.createDirectories()

        val assetsVersionDir = assetsDir.resolve(versionDetails.assetIndex.id)
        if (instance.downloaded && assetsVersionDir.exists()) return@coroutineScope
        assetsVersionDir.createDirectories()

        val indexesDir = assetsVersionDir.resolve("indexes")
        indexesDir.createDirectories()

        network.download(
            url = Url(versionDetails.assetIndex.url),
            destination = indexesDir.resolve("${versionDetails.assetIndex.id}.json")
        )
        val assetIndex = network.fetch<AssetIndexFile>(url = Url(versionDetails.assetIndex.url))

        assetIndex.objects.map { (name, asset) ->
            async {
                val assetUrl = "https://resources.download.minecraft.net/${
                    asset.hash.take(2)
                }/${asset.hash}"

                val file = assetsVersionDir.resolve("objects").resolve("${asset.hash.take(2)}/${asset.hash}")
                file.parent.createDirectories()

                network.download(url = Url(assetUrl), destination = file)
            }
        }.awaitAll()
    }
}