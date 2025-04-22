package me.budchirp.plak.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.budchirp.plak.ui.composition.LocalSnackbarHostState
import me.budchirp.plak.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsView() {
    val viewModel = viewModel { SettingsViewModel() }

    LazyColumn(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            Column {
                Text("UI")

                Row(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Dark mode") },
                        leadingContent = {
                            Switch(
                                checked = viewModel.darkMode,
                                onCheckedChange = { viewModel.updateDarkMode(it) }
                            )
                        }
                    )
                }
            }
        }

        item {
            Column {
                Text("Minecraft")

                Row(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Use dedicated GPU") },
                        leadingContent = {
                            Switch(
                                checked = viewModel.useDedicatedGPU,
                                onCheckedChange = { viewModel.updateUseDedicatedGPU(it) }
                            )
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Use GameMode") },
                        leadingContent = {
                            Switch(
                                checked = viewModel.useGamemodeRun,
                                onCheckedChange = { viewModel.updateUseGamemodeRun(it) }
                            )
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Use MangoHUD") },
                        leadingContent = {
                            Switch(
                                checked = viewModel.useMangoHUD,
                                onCheckedChange = { viewModel.updateUseMangoHUD(it) }
                            )
                        }
                    )
                }
            }
        }

        item {
            Column {
                OutlinedTextField(
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.dataDir,
                    onValueChange = { viewModel.updateDataDir(it) },
                    label = { Text(text = "Data dir") },
                )
            }
        }

        item {
            Row {
                val snackbarHostState = LocalSnackbarHostState.current

                val scope = rememberCoroutineScope()
                Button(onClick = {
                    viewModel.submit {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Success",
                                duration = SnackbarDuration.Long
                            )
                        }
                        viewModel.refresh()
                    }
                }) { Text(text = "Submit") }
            }
        }
    }
}