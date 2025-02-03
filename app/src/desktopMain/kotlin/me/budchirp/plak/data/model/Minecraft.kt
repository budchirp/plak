package me.budchirp.plak.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VersionManifest(
    val versions: List<Version>
)

@Serializable
data class Version(
    val id: String,
    val url: String
)

@Serializable
data class VersionDetails(
    val downloads: DownloadDetails,
    val libraries: List<Library>,
    @SerialName("assetIndex") val assetIndex: AssetIndex,
    @SerialName("mainClass") val mainClass: String
)

@Serializable
data class DownloadDetails(
    val client: Download
)

@Serializable
data class Download(
    val url: String
)

@Serializable
data class Library(
    val downloads: LibraryDownload
)

@Serializable
data class ArtifactDownload(
    val url: String,
    val path: String
)

@Serializable
data class LibraryDownload(
    val artifact: ArtifactDownload? = null,
    val classifiers: Classifiers? = null
)

@Serializable
data class AssetIndex(
    val url: String,
    val id: String
)

@Serializable
data class Classifiers(
    @SerialName("natives-linux") val nativesLinux: Download? = null,
    @SerialName("natives-macos") val nativesMacOS: Download? = null,
    @SerialName("natives-windows") val nativesWindows: Download? = null
)

@Serializable
data class AssetIndexFile(
    val objects: Map<String, AssetObject>
)

@Serializable
data class AssetObject(
    val hash: String
)
