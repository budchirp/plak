package me.budchirp.plak.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.budchirp.plak.data.remote.manager.MinecraftManager
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.composition.LocalSnackbarHostState
import me.budchirp.plak.ui.navigation.Route
import me.budchirp.plak.ui.viewmodels.NewInstanceViewModel
import java.util.*

fun String.toSlug(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("\\s+"), "-")
        .replace(Regex("-+"), "-")
        .trim('-')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInstanceView() {
    val viewModel = viewModel { NewInstanceViewModel() }

    var versionSearch by remember { mutableStateOf("") }

    var versions by remember { mutableStateOf<List<String>?>(null) }

    var showVersionDropdown by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            val minecraftManager = MinecraftManager()
            versions = minecraftManager.getVersions()
        }
    }

    LazyColumn(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            Column {
                OutlinedTextField(
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text(text = "Name") },
                    isError = viewModel.nameError, supportingText = {
                        if (viewModel.nameError) {
                            Text(text = viewModel.nameErrorMessage)
                        }
                    })
            }
        }

        item {
            Column {
                ExposedDropdownMenuBox(expanded = showVersionDropdown, onExpandedChange = {
                    showVersionDropdown = !showVersionDropdown
                }) {
                    OutlinedTextField(
                        maxLines = 1,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = if (viewModel.version.isEmpty()) versionSearch else viewModel.version,
                        onValueChange = {
                            versionSearch = it
                        },
                        label = { Text(text = "Search version") },
                        placeholder = { Text(text = if (versions?.isEmpty() != false) "Loading..." else "") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVersionDropdown)
                        },
                        isError = viewModel.versionError, supportingText = {
                            if (viewModel.versionError) {
                                Text(text = viewModel.versionErrorMessage)
                            }
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = showVersionDropdown,
                        onDismissRequest = {
                            showVersionDropdown = false
                        }
                    ) {
                        versions?.filter { it.contains(versionSearch) }?.forEach {
                            DropdownMenuItem(text = { Text(text = it) }, onClick = {
                                viewModel.updateVersion(it)

                                showVersionDropdown = false
                            })
                        }

                        if (versions?.isEmpty() ?: true) {
                            DropdownMenuItem(text = { Text(text = "Loading...") }, onClick = {})
                        }
                    }
                }
            }
        }

        item {
            Row {
                val navController = LocalNavController.current
                val snackbarHostState = LocalSnackbarHostState.current

                val scope = rememberCoroutineScope()
                Button(enabled = !viewModel.error, onClick = {
                    val slug = if (viewModel.name.isEmpty()) UUID.randomUUID().toString() else viewModel.name.toSlug()
                    viewModel.submit(slug) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Successfully created instance",
                                duration = SnackbarDuration.Long
                            )
                        }

                        navController.navigate(route = Route.Instance(slug = slug))
                    }
                }) { Text(text = "Create") }
            }
        }
    }
}