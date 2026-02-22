package me.echeung.moemoekyun.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import me.echeung.moemoekyun.ui.screen.about.AboutScreen
import me.echeung.moemoekyun.ui.screen.about.LicensesScreen
import me.echeung.moemoekyun.ui.screen.auth.LoginScreen
import me.echeung.moemoekyun.ui.screen.auth.RegisterScreen
import me.echeung.moemoekyun.ui.screen.home.HomeScreen
import me.echeung.moemoekyun.ui.screen.search.SearchScreen
import me.echeung.moemoekyun.ui.screen.settings.SettingsScreen
import me.echeung.moemoekyun.ui.screen.songs.SongsScreen

@Composable
fun AppNavDisplay() {
    val backStack = rememberNavBackStack(Route.Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategy = remember { BottomSheetSceneStrategy<NavKey>() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.Home> {
                HomeScreen(
                    onNavigateSearch = { backStack.add(Route.Search) },
                    onNavigateSettings = { backStack.add(Route.Settings) },
                    onNavigateAbout = { backStack.add(Route.About) },
                    onNavigateLogin = { backStack.add(Route.Login) },
                    onNavigateRegister = { backStack.add(Route.Register) },
                    onShowHistory = { songs -> backStack.add(Route.Songs(songs)) },
                )
            }
            entry<Route.Search> {
                SearchScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onShowSongs = { songs -> backStack.add(Route.Songs(songs)) },
                )
            }
            entry<Route.Settings> {
                SettingsScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<Route.About> {
                AboutScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateLicenses = { backStack.add(Route.Licenses) },
                )
            }
            entry<Route.Licenses> {
                LicensesScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<Route.Login> {
                LoginScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<Route.Register> {
                RegisterScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<Route.Songs>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                SongsScreen(key.songs)
            }
        },
    )
}
