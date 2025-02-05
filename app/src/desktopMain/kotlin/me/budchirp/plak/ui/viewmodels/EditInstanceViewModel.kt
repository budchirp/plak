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

class EditInstanceViewModel : ViewModel() {
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
            name.isEmpty() -> "Name cannot be empty"
            (name.length < 3) -> "Name must be at least 3 characters"
            (name.length > 16) -> "Name must be at most 16 characters"
            else -> ""
        }
    }

    val error by derivedStateOf {
        nameError
    }

    fun updateName(name: String) {
        this.name = name
    }

    fun submit(slug: String, onSuccess: () -> Unit) {
        if (!error) {
            viewModelScope.launch(Dispatchers.IO) {
                val instanceManager = InstanceManager()
                instanceManager.set(
                    instance = instanceManager.get(slug).copy(
                        slug = slug,
                        name = name
                    )
                )
            }

            onSuccess()
        }
    }
}