package me.budchirp.plak.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricLauncherMeta(
    val version: Int,
    @SerialName("min_java_version")
    val minJavaVersion: Int,
    val libraries: FabricLauncherMetaLibraries,
    @SerialName("mainClass")
    val mainClass: FabricMainClass
)

@Serializable
data class FabricLauncherMetaLibraries(
    val client: List<FabricLauncherMetaLibrary> = emptyList(),
    val common: List<FabricLauncherMetaLibrary> = emptyList(),
)

@Serializable
data class FabricLauncherMetaLibrary(
    val name: String,
    val url: String,
)

@Serializable
data class FabricMainClass(
    val client: String,
    val server: String
)

@Serializable
data class FabricIntermediary(
    val maven: String,
    val version: String,
    val stable: Boolean
)

@Serializable
data class FabricLoader(
    val separator: String,
    val build: Int,
    val maven: String,
    val version: String,
    val stable: Boolean
)

@Serializable
data class FabricMetadata(
    val loader: FabricLoader,
    val intermediary: FabricIntermediary,
    @SerialName("launcherMeta")
    val launcherMeta: FabricLauncherMeta
)