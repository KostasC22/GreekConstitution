package com.havistudio.android.greekconstitution.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartDestinationTest {

    @Test
    fun forDisclaimerAccepted_true_goesHome() {
        assertEquals(Screen.Home.route, StartDestination.forDisclaimerAccepted(true))
    }

    @Test
    fun forDisclaimerAccepted_false_goesDisclaimer() {
        assertEquals(Screen.Disclaimer.route, StartDestination.forDisclaimerAccepted(false))
    }

    @Test
    fun shouldShowBottomBar_homeRoute_true() {
        assertTrue(StartDestination.shouldShowBottomBar(Screen.Home.route))
    }

    @Test
    fun shouldShowBottomBar_searchRoute_true() {
        assertTrue(StartDestination.shouldShowBottomBar(Screen.Search.route))
    }

    @Test
    fun shouldShowBottomBar_bookmarksRoute_true() {
        assertTrue(StartDestination.shouldShowBottomBar(Screen.Bookmarks.route))
    }

    @Test
    fun shouldShowBottomBar_settingsRoute_true() {
        assertTrue(StartDestination.shouldShowBottomBar(Screen.Settings.route))
    }

    @Test
    fun shouldShowBottomBar_articleDetail_false() {
        assertFalse(StartDestination.shouldShowBottomBar(Screen.ArticleDetail.route))
    }

    @Test
    fun shouldShowBottomBar_disclaimer_false() {
        assertFalse(StartDestination.shouldShowBottomBar(Screen.Disclaimer.route))
    }

    @Test
    fun shouldShowBottomBar_null_false() {
        assertFalse(StartDestination.shouldShowBottomBar(null))
    }
}
