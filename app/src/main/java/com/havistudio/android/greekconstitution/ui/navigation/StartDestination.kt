package com.havistudio.android.greekconstitution.ui.navigation

/**
 * Pure routing helpers extracted from `MainActivity` so they can be unit-tested
 * without spinning up the Activity.
 */
object StartDestination {

    /**
     * Resolve which top-level route to open at app launch based on whether the
     * user has accepted the disclaimer.
     */
    fun forDisclaimerAccepted(accepted: Boolean): String =
        if (accepted) Screen.Home.route else Screen.Disclaimer.route

    /**
     * Whether the bottom navigation bar should be visible for [currentRoute].
     * Hidden on article-detail and on the disclaimer gate so the reading view
     * and the gate fill the screen.
     */
    fun shouldShowBottomBar(currentRoute: String?): Boolean =
        currentRoute != null &&
            currentRoute != Screen.ArticleDetail.route &&
            currentRoute != Screen.Disclaimer.route
}
