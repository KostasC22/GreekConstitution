package com.havistudio.android.greekconstitution.ui.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchEmptyStateUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val greek = AppStrings.forLanguage(UiLanguage.Greek)

    @Test
    fun emptyQueryContent_showsSuggestedQueriesWhenRecentsEmpty() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyQueryContent(
                    recents = emptyList(),
                    onPick = {},
                    onRemoveRecent = {},
                    onClearRecents = {},
                )
            }
        }
        composeRule.onNodeWithText("ΔΟΚΙΜΑΣΤΕ").assertIsDisplayed()
        composeRule.onNodeWithText("ελευθερία").assertIsDisplayed()
        composeRule.onNodeWithText("ισότητα").assertIsDisplayed()
    }

    @Test
    fun emptyQueryContent_showsRecentsHeaderWhenRecentsNonEmpty() {
        // Use recents that don't collide with the hard-coded suggested-chip
        // labels (`ελευθερία`, `ισότητα`, …) so `onNodeWithText` resolves
        // uniquely.
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyQueryContent(
                    recents = listOf("βουλή", "δικαίωμα"),
                    onPick = {},
                    onRemoveRecent = {},
                    onClearRecents = {},
                )
            }
        }
        composeRule.onNodeWithText("ΠΡΟΣΦΑΤΕΣ ΑΝΑΖΗΤΗΣΕΙΣ").assertIsDisplayed()
        composeRule.onNodeWithText("βουλή").assertIsDisplayed()
        composeRule.onNodeWithText("δικαίωμα").assertIsDisplayed()
        composeRule.onNodeWithText(greek.clear).assertIsDisplayed()
    }

    @Test
    fun emptyQueryContent_tapRecent_invokesOnPickWithThatQuery() {
        var picked: String? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyQueryContent(
                    recents = listOf("βουλή"),
                    onPick = { picked = it },
                    onRemoveRecent = {},
                    onClearRecents = {},
                )
            }
        }
        composeRule.onNodeWithText("βουλή").performClick()
        assertEquals("βουλή", picked)
    }

    @Test
    fun emptyQueryContent_tapClearHeader_invokesOnClearRecents() {
        var cleared = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyQueryContent(
                    recents = listOf("a", "b"),
                    onPick = {},
                    onRemoveRecent = {},
                    onClearRecents = { cleared = true },
                )
            }
        }
        composeRule.onNodeWithText(greek.clear).performClick()
        assertTrue("onClearRecents not invoked", cleared)
    }

    @Test
    fun emptyQueryContent_tapSuggestionChip_invokesOnPick() {
        var picked: String? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyQueryContent(
                    recents = emptyList(),
                    onPick = { picked = it },
                    onRemoveRecent = {},
                    onClearRecents = {},
                )
            }
        }
        composeRule.onNodeWithText("παιδεία").performClick()
        assertEquals("παιδεία", picked)
    }

    @Test
    fun recentRow_tapRemove_invokesOnRemove() {
        var removed = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                RecentRow(query = "παιδεία", onPick = {}, onRemove = { removed = true })
            }
        }
        composeRule.onNodeWithContentDescription(greek.remove).performClick()
        assertTrue("onRemove not invoked", removed)
    }

    @Test
    fun emptyResults_rendersQueryAndHint() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyResults(query = "ξενοτεκλο")
            }
        }
        composeRule.onNodeWithText(greek.searchEmptyForQuery("ξενοτεκλο")).assertIsDisplayed()
        composeRule.onNodeWithText(greek.searchEmptyHint).assertIsDisplayed()
    }
}
