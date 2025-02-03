package me.budchirp.plak.ui.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.budchirp.plak.data.manager.InstanceManager
import me.budchirp.plak.data.model.Instance

class NewInstanceViewModel : ViewModel() {
    var name by mutableStateOf("")
        private set
    val nameError by derivedStateOf {
        when (true) {
            name.isEmpty() -> true
            (name.length < 3) -> true
            (name.length > 16) -> true
            else -> false
        }
    }
    val nameErrorMessage by derivedStateOf {
        when (true) {
            name.isEmpty() -> "Username cannot be empty"
            (name.length < 3) -> "Username must be at least 3 characters"
            (name.length > 16) -> "Username must be at most 16 characters"
            else -> ""
        }
    }

    var version by mutableStateOf("")
        private set
    val versionError by derivedStateOf {
        when (true) {
            version.isEmpty() -> true
            (version.length < 3) -> true
            (version.length > 64) -> true
            else -> false
        }
    }
    val versionErrorMessage by derivedStateOf {
        when (true) {
            version.isEmpty() -> "Version cannot be empty"
            else -> ""
        }
    }

    val error by derivedStateOf {
        nameError || versionError
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun updateVersion(version: String) {
        this.version = version
    }

    fun submit(slug: String, onSuccess: () -> Unit) {
        if (!error) {
            viewModelScope.launch(Dispatchers.IO) {
                val instanceManager = InstanceManager()
                instanceManager.set(
                    instance = Instance().copy(
                        slug = slug,
                        name = name,
                        version = version
                    )
                )
            }

            onSuccess()
        }
    }
}