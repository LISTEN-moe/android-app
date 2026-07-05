package me.echeung.moemoekyun.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import me.echeung.moemoekyun.ui.screen.about.AboutScreen
import me.echeung.moemoekyun.ui.screen.about.LicensesScreen
import me.echeung.moemoekyun.ui.screen.auth.LoginScreen
import me.echeung.moemoekyun.ui.screen.auth.RegisterScreen
import me.echeung.moemoekyun.ui.screen.home.HomeScreen
import me.echeung.moemoekyun.ui.screen.music.MusicScreen
import me.echeung.moemoekyun.ui.screen.rankings.RankingsScreen
import me.echeung.moemoekyun.ui.screen.settings.SettingsScreen
import me.echeung.moemoekyun.ui.screen.songs.SongsScreen

@Composable
fun AppNavDisplay() {
    val backStack = rememberNavBackStack(Route.Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(BottomSheetSceneStrategy()),
        predictivePopTransitionSpec = { edge ->
            val direction = if (edge == NavigationEvent.EDGE_RIGHT) 1 else -1
            val effectsSpec = spring<Float>(dampingRatio = 1.0f, stiffness = 1600.0f)
            ContentTransform(
                fadeIn(effectsSpec) +
                    slideInHorizontally { fullWidth -> -direction * fullWidth / 4 },
                slideOutHorizontally { fullWidth -> direction * fullWidth },
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.Home> {
                HomeScreen(
                    onNavigateSearch = { backStack.add(Route.Music) },
                    onNavigateRankings = { backStack.add(Route.Rankings) },
                    onNavigateSettings = { backStack.add(Route.Settings) },
                    onNavigateAbout = { backStack.add(Route.About) },
                    onNavigateLogin = { backStack.add(Route.Login) },
                    onNavigateRegister = { backStack.add(Route.Register) },
                    onShowHistory = { songs, moreUrl -> backStack.add(Route.Songs(songs, moreUrl)) },
                )
            }
            entry<Route.Music> {
                MusicScreen(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<Route.Rankings> {
                RankingsScreen(
                    onBack = { backStack.removeLastOrNull() },
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
                SongsScreen(key.songs, key.moreUrl)
            }
        },
    )
}
