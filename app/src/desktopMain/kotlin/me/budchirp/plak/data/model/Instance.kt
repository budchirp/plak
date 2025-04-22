package me.budchirp.plak.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Instance(
    val downloaded: Boolean = false,
    val name: String = "",
    val slug: String = "",
    val version: String = "",
    val loader: Loader = Loader(),
    val assetIndex: String? = null,
    val jvm: JvmConfig = JvmConfig()
)

@Serializable
data class JvmConfig(
    val maxMemory: String = "4096M",
    val minMemory: String = "512M",
    val jvmArgs: String = "",
    val jvmPath: String = "java"
)