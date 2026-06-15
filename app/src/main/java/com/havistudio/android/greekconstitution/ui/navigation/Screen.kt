package com.havistudio.android.greekconstitution.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.havistudio.android.greekconstitution.ui.strings.AppStrings

sealed class Screen(val route: String) {
    data object Disclaimer : Screen("disclaimer")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Bookmarks : Screen("bookmarks")
    data object Settings : Screen("settings")
    data object ArticleDetail : Screen("article/{articleId}") {
        fun createRoute(articleId: Int) = "article/$articleId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: (AppStrings) -> String,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home) { it.home },
    BottomNavItem(Screen.Search, Icons.Default.Search) { it.search },
    BottomNavItem(Screen.Bookmarks, Icons.Default.BookmarkBorder) { it.bookmarks },
    BottomNavItem(Screen.Settings, Icons.Default.Settings) { it.settingsNav },
)
