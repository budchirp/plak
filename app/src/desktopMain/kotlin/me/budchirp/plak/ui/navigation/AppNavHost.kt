package me.budchirp.plak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.layout.Layout
import me.budchirp.plak.ui.views.AccountsView
import me.budchirp.plak.ui.views.InstanceView
import me.budchirp.plak.ui.views.NewAccountView
import me.budchirp.plak.ui.views.NewInstanceView
import me.budchirp.plak.ui.views.SettingsView

abstract class Route() {
    abstract val id: String
    abstract val title: String

    @Serializable
    data class Instance(
        override val id: String = "instance",
        override val title: String = "Instance",
        val slug: String? = null
    ) : Route()

    @Serializable
    data class NewInstance(override val id: String = "new-instance", override val title: String = "New instance") :
        Route()

    @Serializable
    data class NewAccount(override val id: String = "new-account", override val title: String = "New account") : Route()

    @Serializable
    data class Accounts(override val id: String = "accounts", override val title: String = "Accounts") : Route()

    @Serializable
    data class Settings(override val id: String = "settings", override val title: String = "Settings") : Route()
}

@Composable
fun WindowScope.AppNavHost(onClose: () -> Unit) {
    val navController = LocalNavController.current

    NavHost(navController = navController, startDestination = Route.Instance()) {
        composable<Route.Instance> { backStackEntry ->
            Layout(route = Route.Instance(), onClose = onClose) {
                val route = backStackEntry.toRoute<Route.Instance>()
                InstanceView(route.slug)
            }
        }

        composable<Route.NewInstance> {
            Layout(route = Route.NewInstance(), onClose = onClose) {
                NewInstanceView()
            }
        }

        composable<Route.NewAccount> {
            Layout(route = Route.NewAccount(), onClose = onClose) {
                NewAccountView()
            }
        }

        composable<Route.Accounts> {
            Layout(route = Route.Accounts(), onClose = onClose) {
                AccountsView()
            }
        }

        composable<Route.Settings> {
            Layout(route = Route.Settings(), onClose = onClose) {
                SettingsView()
            }
        }
    }
}