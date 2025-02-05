package me.budchirp.plak.ui.composables.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.budchirp.plak.data.manager.AccountManager
import me.budchirp.plak.data.model.Account
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.navigation.Route

@Composable
fun AccountSelectorDialog(show: Boolean, onDismiss: () -> Unit, onSelect: (Account) -> Unit) {
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
                    val accountManager = AccountManager()
                    val accounts = accountManager.getAll()

                    Text(
                        text = "Select account",
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
                        accounts.forEach { account ->
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(size = 8.dp)).clickable {
                                        onDismiss()

                                        onSelect(account)
                                    },
                                headlineContent = { Text(text = account.username) }
                            )
                        }

                        if (accounts.isEmpty()) {
                            Text(
                                text = "No account found",
                                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val navController = LocalNavController.current
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        onDismiss()

                        navController.navigate(route = Route.NewAccount())
                    }) {
                        Text(text = "Add account")
                    }
                }
            }
        }
    }
}