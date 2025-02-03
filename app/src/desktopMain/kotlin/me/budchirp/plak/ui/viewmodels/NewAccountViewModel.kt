package me.budchirp.plak.ui.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.budchirp.plak.data.manager.AccountManager
import me.budchirp.plak.data.model.Account
import java.util.*

class NewAccountViewModel : ViewModel() {
    var username by mutableStateOf("")
        private set
    val usernameError by derivedStateOf {
        when (true) {
            username.isEmpty() -> true
            (username.length < 3) -> true
            (username.length > 16) -> true
            else -> false
        }
    }
    val usernameErrorMessage by derivedStateOf {
        when (true) {
            username.isEmpty() -> "Username cannot be empty"
            (username.length < 3) -> "Username must be at least 3 characters"
            (username.length > 16) -> "Username must be at most 16 characters"
            else -> ""
        }
    }

    var token by mutableStateOf("")
        private set
    val tokenError by derivedStateOf {
        when (!offline) {
            token.isEmpty() -> !offline
            else -> false
        }
    }
    val tokenErrorMessage by derivedStateOf {
        when (true) {
            token.isEmpty() -> "Token cannot be empty"
            else -> ""
        }
    }

    var uuid by mutableStateOf("")
        private set
    val uuidError by derivedStateOf {
        when (true) {
            uuid.isEmpty() -> !offline
            (uuid.length != 36) -> !offline
            else -> false
        }
    }
    val uuidErrorMessage by derivedStateOf {
        when (true) {
            uuid.isEmpty() -> "UUID cannot be empty"
            (uuid.length != 36) -> "UUID must be 36 characters"
            else -> ""
        }
    }

    val error by derivedStateOf {
        usernameError || tokenError || uuidError
    }

    var offline by mutableStateOf(false)
        private set

    fun updateUsername(username: String) {
        this.username = username
    }

    fun updateToken(token: String) {
        this.token = token
    }

    fun updateUuid(uuid: String) {
        this.uuid = uuid
    }

    fun updateOffline(offline: Boolean) {
        this.offline = offline
    }

    fun submit(onSuccess: () -> Unit) {
        if (!error) {
            viewModelScope.launch(Dispatchers.IO) {
                val accountManager = AccountManager()

                accountManager.set(
                    Account(
                        username = username,
                        offline = offline,
                        token = if (offline) "gibberish" else token,
                        uuid = if (offline) UUID.randomUUID().toString() else uuid
                    )
                )
            }

            onSuccess()
        }

    }
}