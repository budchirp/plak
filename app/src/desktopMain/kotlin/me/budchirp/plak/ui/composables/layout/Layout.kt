package me.budchirp.plak.ui.composables.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowScope
import me.budchirp.plak.ui.navigation.Route

@Composable
fun WindowScope.Layout(route: Route, onClose: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(route = route, onClose = onClose)

        content()
    }
}