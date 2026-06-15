package com.havistudio.android.greekconstitution

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.havistudio.android.greekconstitution.data.local.ConstitutionDatabase
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.data.local.SearchHistoryManager
import com.havistudio.android.greekconstitution.data.local.UserPreferences
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import com.havistudio.android.greekconstitution.ui.navigation.AppNavGraph
import com.havistudio.android.greekconstitution.ui.navigation.Screen
import com.havistudio.android.greekconstitution.ui.navigation.StartDestination
import com.havistudio.android.greekconstitution.ui.navigation.bottomNavItems
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalReadingFontScale
import com.havistudio.android.greekconstitution.ui.theme.GreekConstitutionTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        val db = ConstitutionDatabase.get(this)
        ConstitutionRepository(
            constitutionDao = db.constitutionDao(),
            bookmarkDao = db.bookmarkDao(),
            noteDao = db.noteDao(),
            searchDao = db.searchDao(),
        )
    }

    private val preferencesManager by lazy { PreferencesManager(this) }

    private val searchHistory by lazy { SearchHistoryManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val prefs by preferencesManager.preferences
                .collectAsState(initial = UserPreferences())
            val strings = AppStrings.forLanguage(prefs.language)
            GreekConstitutionTheme(
                themePref = prefs.theme,
                dynamicColor = prefs.dynamicColor,
            ) {
                CompositionLocalProvider(
                    LocalAppStrings provides strings,
                    LocalReadingFontScale provides prefs.fontSize.scale,
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    val disclaimerAccepted by preferencesManager.hasAcceptedDisclaimer
                        .collectAsState(initial = null)

                    // While loading preferences, show nothing (splash still visible)
                    val accepted = disclaimerAccepted ?: return@CompositionLocalProvider

                    val startDestination = StartDestination.forDisclaimerAccepted(accepted)

                    val currentRoute = currentDestination?.route
                    val showBottomBar = StartDestination.shouldShowBottomBar(currentRoute)

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    bottomNavItems.forEach { item ->
                                        val label = item.label(strings)
                                        val selected = currentDestination?.hierarchy?.any {
                                            it.route == item.screen.route
                                        } == true
                                        NavigationBarItem(
                                            icon = { Icon(item.icon, contentDescription = label) },
                                            label = { Text(label) },
                                            selected = selected,
                                            onClick = {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(Screen.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        },
                    ) { innerPadding ->
                        AppNavGraph(
                            navController = navController,
                            repository = repository,
                            preferencesManager = preferencesManager,
                            searchHistory = searchHistory,
                            startDestination = startDestination,
                            padding = innerPadding,
                        )
                    }
                }
            }
        }
    }
}
