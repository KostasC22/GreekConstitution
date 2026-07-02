package com.havistudio.android.greekconstitution.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.data.local.SearchHistoryManager
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import com.havistudio.android.greekconstitution.ui.article.ArticleDetailScreen
import com.havistudio.android.greekconstitution.ui.bookmarks.BookmarksScreen
import com.havistudio.android.greekconstitution.ui.disclaimer.DisclaimerScreen
import com.havistudio.android.greekconstitution.ui.home.HomeScreen
import com.havistudio.android.greekconstitution.ui.search.SearchScreen
import com.havistudio.android.greekconstitution.ui.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: ConstitutionRepository,
    preferencesManager: PreferencesManager,
    searchHistory: SearchHistoryManager,
    startDestination: String,
    padding: PaddingValues = PaddingValues(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Consume the insets the outer Scaffold's padding already covers, or
        // every screen's own Scaffold/TopAppBar pads the status bar a second
        // time (double gap — glaring on tall-status-bar devices like Pixel 10).
        modifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding),
    ) {
        composable(Screen.Disclaimer.route) {
            DisclaimerScreen(
                preferencesManager = preferencesManager,
                onAccepted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Disclaimer.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                repository = repository,
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                repository = repository,
                searchHistory = searchHistory,
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
            )
        }
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                repository = repository,
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                preferencesManager = preferencesManager,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: return@composable
            ArticleDetailScreen(
                articleId = articleId,
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenArticle = { nextId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(nextId))
                },
            )
        }
    }
}
