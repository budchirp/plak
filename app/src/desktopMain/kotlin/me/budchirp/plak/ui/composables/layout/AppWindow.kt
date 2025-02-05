package me.budchirp.plak.ui.composables.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import me.budchirp.plak.ui.composition.LocalSnackbarHostState
import me.budchirp.plak.ui.composition.ProvideSnackbarHostState

@Composable
fun WindowScope.AppWindow(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(all = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(size = 16.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        ProvideSnackbarHostState {
            Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
                SnackbarHost(hostState = LocalSnackbarHostState.current)
            }) {
                Drawer {
                    content()
                }
            }
        }
    }
}