package me.budchirp.plak.ui.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                shape = RoundedCornerShape(size = 24.dp),
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(all = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 24.dp)
                ) {
                    Text(
                        text = "Are you sure?",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = description
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.End)
                    ) {
                        Button(
                            onClick = onDismiss
                        ) {
                            Text(text = "Cancel")
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ), onClick = {
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