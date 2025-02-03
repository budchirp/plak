package me.budchirp.plak.utils

enum class OperatingSystems {
    WINDOWS,
    LINUX,
    MAC
}

class OS {
    companion object {
        fun getOS(): OperatingSystems = when (System.getProperty("os.name").lowercase()) {
            "windows" -> OperatingSystems.WINDOWS
            "linux" -> OperatingSystems.LINUX
            "mac os x" -> OperatingSystems.MAC
            else -> OperatingSystems.LINUX
        }
    }
}