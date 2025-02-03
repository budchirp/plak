package me.budchirp.plak.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import me.budchirp.plak.APP_NAME
import me.budchirp.plak.data.manager.InstanceManager
import me.budchirp.plak.data.model.Instance
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.navigation.Route

@Composable
fun WindowScope.Drawer(content: @Composable () -> Unit) {

    PermanentNavigationDrawer(drawerContent = {
        PermanentDrawerSheet(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = 0.35f),
            drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            var instances by remember { mutableStateOf<List<Instance>>(listOf()) }

            val navController = LocalNavController.current
            val currentRoute by navController.currentBackStackEntryAsState()

            val instanceManager = InstanceManager()

            val scope = rememberCoroutineScope()
            LaunchedEffect(currentRoute) {
                scope.launch {
                    instances = instanceManager.getAll()
                }
            }

            scope.launch {
                instances = instanceManager.getAll()
            }

            Column {
                DrawerTitleBar()

                val navController = LocalNavController.current
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    items(instances) {
                        ListItem(modifier = Modifier.clip(shape = RoundedCornerShape(size = 8.dp)).clickable {
                            navController.navigate(route = Route.Instance(slug = it.slug))
                        }, headlineContent = {
                            Text(text = it.name)
                        }, supportingContent = {
                            Text(text = it.version)

                        })
                    }
                }
            }
        }
    }, content = content)
}

@Composable
fun WindowScope.DrawerTitleBar() {
    WindowDraggableArea {
        Row(
            modifier = Modifier.fillMaxWidth().height(height = 64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navController = LocalNavController.current
            IconButton(onClick = {
                navController.navigate(route = Route.NewInstance())
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New instance")
            }

            Spacer(modifier = Modifier.weight(weight = 1f))

            Text(text = APP_NAME, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.weight(weight = 1f))

            IconButton(onClick = {
                navController.navigate(route = Route.Settings())
            }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}