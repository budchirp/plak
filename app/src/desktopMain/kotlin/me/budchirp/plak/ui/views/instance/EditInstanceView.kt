package me.budchirp.plak.ui.views.instance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.navigation.Route
import me.budchirp.plak.ui.viewmodels.EditInstanceViewModel

@Composable
fun EditInstanceView(slug: String) {
    val viewModel = viewModel { EditInstanceViewModel(slug) }

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