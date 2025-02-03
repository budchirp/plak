package me.budchirp.plak.ui.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

val LocalNavController: ProvidableCompositionLocal<NavHostController> =
    staticCompositionLocalOf<NavHostController> { error("Not provided") }

@Composable
fun ProvideNavController(content: @Composable () -> Unit) {
    val navController: NavHostController = rememberNavController()

    CompositionLocalProvider(
        value = LocalNavController provides navController,
    ) {
        content()
    }
}