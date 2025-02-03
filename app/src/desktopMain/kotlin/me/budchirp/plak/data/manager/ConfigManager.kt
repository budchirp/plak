package me.budchirp.plak.data.manager

import me.budchirp.plak.CONFIG_FILE
import me.budchirp.plak.data.model.Config
import me.budchirp.plak.utils.JSON
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ConfigManager {
    init {
        if (!CONFIG_FILE.parent.exists()) {
            CONFIG_FILE.parent.createDirectories()
        }
    }

    fun get(): Config {
        return JSON.parse(Config.serializer(), CONFIG_FILE.readText())
    }

    fun set(config: Config) {
        CONFIG_FILE.writeText(JSON.stringify(Config.serializer(), config))
    }
}