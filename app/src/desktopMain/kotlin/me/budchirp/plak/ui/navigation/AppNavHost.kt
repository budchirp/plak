package me.budchirp.plak.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.budchirp.plak.ui.composables.layout.Layout
import me.budchirp.plak.ui.composition.LocalNavController
import me.budchirp.plak.ui.views.SettingsView
import me.budchirp.plak.ui.views.account.AccountsView
import me.budchirp.plak.ui.views.account.NewAccountView
import me.budchirp.plak.ui.views.instance.EditInstanceView
import me.budchirp.plak.ui.views.instance.InstanceView
import me.budchirp.plak.ui.views.instance.NewInstanceView

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
    data class EditInstance(
        override val id: String = "edit-instance",
        override val title: String = "Edit instance",
        val slug: String
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
            val route = backStackEntry.toRoute<Route.Instance>()
            val slug = route.slug

            Layout(route = Route.Instance(slug = slug), onClose = onClose) {
                InstanceView(slug)
            }
        }

        composable<Route.EditInstance> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Instance>()
            val slug = route.slug!!

            Layout(route = Route.EditInstance(slug = slug), onClose = onClose) {
                EditInstanceView(slug)
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