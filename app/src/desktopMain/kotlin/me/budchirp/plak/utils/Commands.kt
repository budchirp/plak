package me.budchirp.plak.utils

import java.io.File
import java.nio.file.Path
import java.util.zip.ZipInputStream

class Commands {
    companion object {
        fun unzip(file: Path, destination: Path) {
            val zipFile = File(file.toString())
            val destDir = File(destination.toString())
            
            ZipInputStream(zipFile.inputStream()).use { zip ->
                generateSequence { zip.nextEntry }.forEach { entry ->
                    val file = destDir.resolve(entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        file.outputStream().use { out ->
                            out.write(zip.readBytes())
                        }
                    }
                }
            }
        }
    }
}