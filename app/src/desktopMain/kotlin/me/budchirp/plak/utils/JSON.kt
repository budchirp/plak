package me.budchirp.plak.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

class JSON {
    companion object {
        fun <T> parse(serializer: DeserializationStrategy<T>, str: String): T {
            val json = Json {
                encodeDefaults = true
                prettyPrint = true
                ignoreUnknownKeys = true
            }

            return json.decodeFromString(serializer, str)
        }

        fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String {
            val json = Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            return json.encodeToString(serializer, obj)
        }
    }
}