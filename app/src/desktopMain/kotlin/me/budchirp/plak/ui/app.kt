package me.budchirp.plak.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.budchirp.plak.ui.composables.layout.AppLayout
import me.budchirp.plak.ui.composables.layout.AppWindow
import me.budchirp.plak.ui.composition.ProvideNavController
import me.budchirp.plak.ui.navigation.AppNavHost
import me.budchirp.plak.ui.theme.AppTheme

fun app() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "plak",
            undecorated = true,
            transparent = true
        ) {
            AppTheme {
                ProvideNavController {
                    AppWindow {
                        AppLayout {
                            AppNavHost(onClose = ::exitApplication)
                        }
                    }
                }
            }
        }
    }
}