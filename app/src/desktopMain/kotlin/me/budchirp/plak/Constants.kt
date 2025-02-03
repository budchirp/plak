package me.budchirp.plak

import me.budchirp.plak.utils.FS
import java.nio.file.Path

const val APP_NAME = "plak"
const val APP_VERSION = "1.0.0"

val CONFIG_FILE: Path = FS.getXDGDirectory(FS.XDGDirectory.CONFIG).resolve(APP_NAME).resolve("config.json")