package me.budchirp.plak.data.manager

import me.budchirp.plak.data.model.Instance
import me.budchirp.plak.utils.JSON
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText

class InstanceManager() {
    private val configManager = ConfigManager()

    private val config = configManager.get()
    private val instancesDir = Path(config.dataDir).resolve("instances")

    init {
        if (!instancesDir.exists()) {
            instancesDir.createDirectories()
        }
    }

    fun get(slug: String): Instance {
        val instanceDir = instancesDir.resolve(slug)

        val instanceConfigPath = instanceDir.resolve("config.json")
        val instanceConfig = JSON.parse(Instance.serializer(), instanceConfigPath.readText())

        return instanceConfig
    }

    fun getAll(): List<Instance> {
        val instancePaths = instancesDir.listDirectoryEntries().filter { it.isDirectory() }

        return instancePaths.map { get(it.fileName.toString()) }
    }

    fun set(instance: Instance) {
        val instanceDir = instancesDir.resolve(instance.slug)
        instanceDir.createDirectories()

        val instanceConfigPath = instanceDir.resolve("config.json")
        instanceConfigPath.writeText(JSON.stringify(Instance.serializer(), instance))
    }

    @OptIn(ExperimentalPathApi::class)
    fun delete(slug: String) {
        val instanceDir = instancesDir.resolve(slug)
        instanceDir.deleteRecursively()
    }
}