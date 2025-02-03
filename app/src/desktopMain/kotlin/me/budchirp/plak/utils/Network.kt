package me.budchirp.plak.utils

import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import me.budchirp.plak.data.remote.client.KtorClient
import io.ktor.http.Url
import java.nio.file.Path
import kotlin.io.path.writeBytes

class Network {
    val client = KtorClient().getClient()

    suspend inline fun <reified T> fetch(url: Url): T {
        return client.get(url).body()
    }

    suspend fun download(url: Url, destination: Path) {
        client.get(url).body<ByteArray>().let { bytes ->
            destination.writeBytes(bytes)
        }
    }
}