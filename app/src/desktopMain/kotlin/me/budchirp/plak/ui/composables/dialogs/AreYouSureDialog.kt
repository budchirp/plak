package me.budchirp.plak.ui.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AreYouSureDialog(description: String, show: Boolean, onDismiss: () -> Unit, onYes: () -> Unit) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
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
                    Text(
                        text = "Are you sure?",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )

                    Column(
                        modifier = Modifier.verticalScroll(
                            state = rememberScrollState()
                        ).fillMaxWidth().weight(weight = 1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        Text(
                            text = description,
                            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.End)
                    ) {
                        Button(
                            onClick = onDismiss
                        ) {
                            Text(text = "Cancel")
                        }

                        Button(onClick = {
                            onDismiss()

                            onYes()
                        }) {
                            Text(text = "Yes")
                        }
                    }
                }
            }
        }
    }
}