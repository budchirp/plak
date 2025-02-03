package me.budchirp.plak.utils

import java.nio.file.Path
import kotlin.io.path.Path

class FS {
    enum class XDGDirectory {
        CONFIG,
        DATA,
    }

    companion object {
        fun getXDGDirectory(dir: XDGDirectory): Path {
            val os = OS.getOS()

            val name = when (dir) {
                XDGDirectory.CONFIG -> "XDG_CONFIG_HOME"
                XDGDirectory.DATA -> "XDG_DATA_HOME"
            }

            val value = when (os) {
                OperatingSystems.LINUX, OperatingSystems.MAC -> {
                    System.getenv(name) ?: (System.getProperty("user.home") + when (dir) {
                        XDGDirectory.CONFIG -> "/.config"
                        XDGDirectory.DATA -> "/.local/share"
                    })
                }

                OperatingSystems.WINDOWS -> {
                    when (dir) {
                        XDGDirectory.CONFIG -> System.getenv("APPDATA")
                            ?: (System.getProperty("user.home") + "\\AppData\\Roaming")

                        XDGDirectory.DATA -> System.getenv("LOCALAPPDATA")
                            ?: (System.getProperty("user.home") + "\\AppData\\Local")
                    }
                }
            }

            return Path(value)
        }
    }
}
