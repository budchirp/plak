package me.budchirp.plak.ui.views.instance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.budchirp.plak.data.manager.InstanceManager
import me.budchirp.plak.data.manager.MinecraftManager
import me.budchirp.plak.data.model.Instance
import me.budchirp.plak.ui.composables.dialogs.AccountSelectorDialog
import me.budchirp.plak.ui.composables.dialogs.AreYouSureDialog
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.composition.LocalSnackbarHostState
import me.budchirp.plak.ui.navigation.Route
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun InstanceView(slug: String? = null) {
    var instance by remember { mutableStateOf<Instance?>(null) }

    val instanceManager = InstanceManager()
    val scope = rememberCoroutineScope()
    scope.launch {
        instance = when (slug) {
            null -> instanceManager.getAll().firstOrNull()
            else -> instanceManager.get(slug)
        }
    }

    LazyColumn {
        item {
            if (instance == null) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "No instance found")
                }
            }
        }

        item {
            instance?.let { _instance ->
                val snackbarHostState = LocalSnackbarHostState.current
                val minecraftManager = MinecraftManager()

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(size = 192.dp).clip(shape = RoundedCornerShape(size = 8.dp))
                            .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                    }

                    Column {
                        Text(text = _instance.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = _instance.version + " (${_instance.loader.type.type})")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                        var downloading by remember { mutableStateOf(false) }

                        var showAccountSelector by remember { mutableStateOf(false) }
                        AccountSelectorDialog(show = showAccountSelector, onDismiss = {
                            showAccountSelector = false
                        }) { account ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Launching instance...",
                                    duration = SnackbarDuration.Long
                                )

                                minecraftManager.launch(
                                    instance = _instance,
                                    account = account,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Launched",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    })
                            }
                        }

                        val navController = LocalNavController.current
                        Button(onClick = {
                            scope.launch {
                                if (_instance.downloaded) {
                                    showAccountSelector = true
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "Downloading...",
                                        duration = SnackbarDuration.Long
                                    )

                                    downloading = true
                                    minecraftManager.download(instance = _instance, onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Downloaded",
                                                duration = SnackbarDuration.Long
                                            )
                                        }

                                        navController.navigate(route = Route.Instance(slug = _instance.slug))
                                    })
                                }
                            }
                        }) {
                            Text(
                                text = when (true) {
                                    downloading -> "Downloading..."
                                    _instance.downloaded -> "Launch"
                                    else -> "Download"
                                }
                            )
                        }

                        FilledTonalButton(onClick = {
                            navController.navigate(route = Route.EditInstance(slug = _instance.slug))
                        }) {
                            Text(text = "Edit")
                        }

                        var showAreYouSureDialog by remember { mutableStateOf(false) }
                        AreYouSureDialog(
                            description = "Are you sure you want to delete this instance?",
                            show = showAreYouSureDialog,
                            onDismiss = {
                                showAreYouSureDialog = false
                            }) {
                            scope.launch {
                                instanceManager.delete(_instance.slug)

                                snackbarHostState.showSnackbar(
                                    message = "Deleted",
                                    duration = SnackbarDuration.Long
                                )
                            }

                            navController.navigate(route = Route.Instance())
                        }

                        FilledTonalButton(
                            onClick = {
                                showAreYouSureDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(text = "Delete")
                        }
                    }
                }
            }
        }
    }
}