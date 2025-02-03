package me.budchirp.plak.data.remote.client

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class KtorClient {
    private val client: HttpClient =
        HttpClient(OkHttp) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println(message)
                        }
                    }
            }

            install(ContentNegotiation) {
                json(
                    json =
                        Json {
                            prettyPrint = true
                            ignoreUnknownKeys = true
                        },
                )
            }
        }

    fun getClient(): HttpClient = client
}