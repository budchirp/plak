package me.budchirp.plak.ui.composition

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackbarHostState: ProvidableCompositionLocal<SnackbarHostState> =
    staticCompositionLocalOf<SnackbarHostState> { error("Not provided") }

@Composable
fun ProvideSnackbarHostState(content: @Composable () -> Unit) {
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        value = LocalSnackbarHostState provides snackbarHostState,
    ) {
        content()
    }
}