package me.budchirp.plak.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import me.budchirp.plak.data.manager.AccountManager
import me.budchirp.plak.data.manager.InstanceManager
import me.budchirp.plak.data.model.Instance
import me.budchirp.plak.data.remote.manager.MinecraftManager
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.composition.LocalSnackbarHostState
import me.budchirp.plak.ui.navigation.Route
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun InstanceView(slug: String? = null) {
    var instance by remember { mutableStateOf<Instance?>(null) }

    val scope = rememberCoroutineScope()
    val instanceManager = InstanceManager()
    scope.launch {
        if (slug != null) {
            instance = instanceManager.get(slug)
        } else {
            instance = instanceManager.getAll().firstOrNull()
        }
    }

    if (instance == null) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "No instance found")
        }
    }

    instance?.let {
        var showAccountSelector by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        val snackbarHostState = LocalSnackbarHostState.current

        val minecraftManager = MinecraftManager()
        if (showAccountSelector) {
            Dialog(
                onDismissRequest = {
                    showAccountSelector = false
                }) {
                Card(
                    modifier = Modifier.fillMaxWidth(fraction = 0.50f),
                    shape = RoundedCornerShape(size = 16.dp),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                    ) {
                        val accountManager = AccountManager()
                        val accounts = accountManager.getAll()

                        Text(
                            text = "Select account",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                        )

                        Column(
                            modifier = Modifier.verticalScroll(
                                state = rememberScrollState()
                            ).fillMaxWidth().weight(weight = 1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                        ) {
                            accounts.forEach { account ->
                                ListItem(
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(size = 8.dp)).clickable {
                                            showAccountSelector = false

                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Launching instance...",
                                                    duration = SnackbarDuration.Long
                                                )

                                                minecraftManager.launch(
                                                    instance = instance!!,
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
                                        },
                                    headlineContent = { Text(text = account.username) }
                                )
                            }

                            if (accounts.isEmpty()) {
                                Text(
                                    text = "No account found",
                                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                                )
                            }
                        }

                        val navController = LocalNavController.current
                        Button(modifier = Modifier.fillMaxWidth(), onClick = {
                            showAccountSelector = false

                            navController.navigate(route = Route.NewAccount())
                        }) {
                            Text(text = "Add account")
                        }
                    }
                }
            }
        }

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
                Text(text = instance!!.name, style = MaterialTheme.typography.titleMedium)
                Text(text = instance!!.version)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                var downloading by remember { mutableStateOf(false) }

                Button(onClick = {
                    scope.launch {
                        if (instance!!.downloaded) {
                            showAccountSelector = true
                        } else {
                            snackbarHostState.showSnackbar(
                                message = "Downloading...",
                                duration = SnackbarDuration.Long
                            )

                            downloading = true
                            minecraftManager.download(instance = instance!!, onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Downloaded",
                                        duration = SnackbarDuration.Long
                                    )
                                }

                                instance = instanceManager.get(instance!!.slug)
                                downloading = false
                            })
                        }
                    }
                }) {
                    Text(
                        text = when (true) {
                            downloading -> "Downloading..."
                            instance!!.downloaded -> "Launch"
                            else -> "Download"
                        }
                    )
                }

                FilledTonalButton(onClick = {}) {
                    Text(text = "Edit")
                }

                FilledTonalButton(
                    onClick = {},
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