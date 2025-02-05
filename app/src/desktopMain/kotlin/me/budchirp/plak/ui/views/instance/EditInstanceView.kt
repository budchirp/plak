package me.budchirp.plak.ui.views.instance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.budchirp.plak.data.manager.InstanceManager
import me.budchirp.plak.data.model.Instance
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.navigation.Route
import me.budchirp.plak.ui.viewmodels.EditInstanceViewModel

@Composable
fun EditInstanceView(slug: String) {
    val viewModel = viewModel { EditInstanceViewModel() }

    var instance by remember { mutableStateOf<Instance?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch {
            val instanceManager = InstanceManager()
            instance = instanceManager.get(slug)
        }
    }

    LazyColumn(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            if (instance == null) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Loading...")
                }
            }
        }

        item {
            if (instance != null) {
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
        }

        item {
            if (instance != null) {
                Row {
                    val navController = LocalNavController.current
                    Button(enabled = !viewModel.error, onClick = {
                        viewModel.submit(slug) {
                            navController.navigate(route = Route.Instance(slug = slug))
                        }
                    }) { Text(text = "Submit") }
                }
            }
        }
    }
}