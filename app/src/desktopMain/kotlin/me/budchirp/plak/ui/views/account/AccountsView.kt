package me.budchirp.plak.ui.views.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import me.budchirp.plak.data.manager.AccountManager
import me.budchirp.plak.ui.composables.dialogs.AreYouSureDialog
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.navigation.Route

@Composable
fun AccountsView() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        val accountManager = AccountManager()
        val accounts = accountManager.getAll()

        item {
            if (accounts.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "No account found")
                }
            }
        }

        items(accounts) { account ->
            ListItem(
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp)),
                headlineContent = { Text(text = account.username) },
                trailingContent = {
                    var showDropdown by remember { mutableStateOf(false) }
                    IconButton(onClick = {
                        showDropdown = true
                    }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Account actions")
                    }

                    var showAreYouSureDialog by remember { mutableStateOf(false) }

                    val actions = listOf("Delete")
                    DropdownMenu(expanded = showDropdown, onDismissRequest = {
                        showDropdown = false
                    }) {
                        actions.map {
                            DropdownMenuItem(onClick = {
                                showDropdown = false

                                when (it) {
                                    "Delete" -> {
                                        showAreYouSureDialog = true
                                    }
                                }
                            }, text = {
                                Text(text = it)
                            })
                        }
                    }

                    val navController = LocalNavController.current

                    val scope = rememberCoroutineScope()
                    AreYouSureDialog(
                        description = "Are you sure you want to delete this account?",
                        show = showAreYouSureDialog,
                        onDismiss = {
                            showAreYouSureDialog = false
                        }) {
                        scope.launch {
                            accountManager.delete(account)
                        }
                        
                        navController.navigate(route = Route.Accounts())
                    }
                }
            )
        }
    }
}