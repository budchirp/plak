package me.budchirp.plak.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.navigation.Route

@Composable
fun WindowScope.TitleBar(route: Route, onClose: () -> Unit) {
    WindowDraggableArea {
        val isInstanceScreen = route == Route.Instance()
        Row(
            modifier = Modifier.fillMaxWidth().height(height = 64.dp)
                .padding(start = if (isInstanceScreen) 16.dp else 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                if (!isInstanceScreen) {
                    val navController = LocalNavController.current
                    IconButton(onClick = {
                        navController.navigate(route = Route.Instance())
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to instances screen")
                    }
                }

                Text(text = route.title, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Row {
                    val navController = LocalNavController.current
                    IconButton(onClick = {
                        if (route == Route.Accounts()) {
                            navController.navigate(route = Route.NewAccount())
                        } else {
                            navController.navigate(route = Route.Accounts())
                        }
                    }) {
                        Icon(
                            imageVector = if (route == Route.Accounts()) Icons.Default.PersonAdd else Icons.Default.Person,
                            contentDescription = "Account"
                        )
                    }
                }

                Divider(modifier = Modifier.fillMaxHeight().width(width = 1.dp))

                Row {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close app")
                    }
                }
            }
        }
    }
}