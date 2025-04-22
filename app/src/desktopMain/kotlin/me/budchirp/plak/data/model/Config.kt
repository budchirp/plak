package me.budchirp.plak.data.model

import kotlinx.serialization.Serializable
import me.budchirp.plak.APP_NAME
import me.budchirp.plak.utils.FS

@Serializable
data class Config(
    val ui: UIConfig = UIConfig(),
    val launch: LaunchConfig = LaunchConfig(),
    val dataDir: String = FS.getXDGDirectory(dir = FS.XDGDirectory.DATA).resolve(
        APP_NAME
    ).toString()
)

@Serializable
data class UIConfig(val darkMode: Boolean = true)

@Serializable
data class LaunchConfig(
    val useDedicatedGPU: Boolean = true,
    val useGamemodeRun: Boolean = false,
    val useMangoHUD: Boolean = false
)