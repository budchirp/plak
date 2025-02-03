package me.budchirp.plak.utils

import me.budchirp.plak.CONFIG_FILE
import me.budchirp.plak.data.manager.ConfigManager
import me.budchirp.plak.data.model.Config
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class Init {
    companion object {
        fun init() {
            if (!CONFIG_FILE.parent.isDirectory()) CONFIG_FILE.parent.createDirectories()

            val configManager = ConfigManager()
            if (!CONFIG_FILE.exists()) {
                configManager.set(Config())
            }
        }
    }
}