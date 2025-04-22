package me.budchirp.plak.data.model

import kotlinx.serialization.Serializable

enum class LoaderType(val type: String) {
    FABRIC("Fabric"),
    VANILLA("Vanilla"),
}

@Serializable
data class Loader(
    val type: LoaderType = LoaderType.VANILLA,
    val mainClass: String = ""
)