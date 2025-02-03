package me.budchirp.plak.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Account(val offline: Boolean = true, val uuid: String = UUID.randomUUID().toString(), val token: String = "some-garbage", val username: String = "steve")
