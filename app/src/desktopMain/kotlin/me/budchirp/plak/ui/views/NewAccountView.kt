package me.budchirp.plak.ui.views

import androidx.compose.foundation.layout.Arrangement
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
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.composition.LocalSnackbarHostState
import me.budchirp.plak.ui.navigation.Route
import me.budchirp.plak.ui.viewmodels.NewAccountViewModel

@Composable
fun NewAccountView() {
    val viewModel = viewModel { NewAccountViewModel() }

    LazyColumn(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            Row {
                OutlinedTextField(
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text(text = "Username") },
                    isError = viewModel.usernameError, supportingText = {
                        if (viewModel.usernameError) {
                            Text(text = viewModel.usernameErrorMessage)
                        }
                    })
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Offline") },
                    leadingContent = {
                        Switch(
                            checked = viewModel.offline,
                            onCheckedChange = { viewModel.updateOffline(it) }
                        )
                    }
                )
            }
        }

        item {
            if (!viewModel.offline) {
                Row {
                    OutlinedTextField(
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        value = viewModel.token,
                        onValueChange = { viewModel.updateToken(it) },
                        label = { Text(text = "Token") },
                        isError = viewModel.tokenError, supportingText = {
                            if (viewModel.tokenError) {
                                Text(text = viewModel.tokenErrorMessage)
                            }
                        })
                }

                Row {
                    OutlinedTextField(
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        value = viewModel.uuid,
                        onValueChange = { viewModel.updateUuid(it) },
                        label = { Text(text = "UUID") },
                        isError = viewModel.uuidError, supportingText = {
                            if (viewModel.uuidError) {
                                Text(text = viewModel.uuidErrorMessage)
                            }
                        })
                }
            }
        }

        item {
            Row {
                val navController = LocalNavController.current
                val snackbarHostState = LocalSnackbarHostState.current

                val scope = rememberCoroutineScope()
                Button(
                    enabled = !viewModel.error,
                    onClick = {
                        viewModel.submit {
                            navController.navigate(route = Route.Instance(slug = null))

                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Successfully added account",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }

                    }) {
                    Text("Save")
                }
            }
        }
    }
}