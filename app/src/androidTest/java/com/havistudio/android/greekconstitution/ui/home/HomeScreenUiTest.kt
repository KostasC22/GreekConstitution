package com.havistudio.android.greekconstitution.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
class HomeScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val greek = AppStrings.forLanguage(UiLanguage.Greek)

    private val part = TocNode(
        id = "part-1",
        title = "Μέρος Πρώτο",
        subtitle = "Βασικές διατάξεις",
        depth = 0,
        hasChildren = true,
        isExpanded = false,
    )

    private val leafArticle = TocNode(
        id = "article-5",
        title = "Άρθρο 5",
        subtitle = null,
        depth = 2,
        articleId = 5,
        hasChildren = false,
    )

    @Test
    fun tocItem_partRow_displaysTitleAndSubtitle() {
        composeRule.setContent {
            TocItem(node = part, onToggle = {}, onArticleClick = {})
        }
        composeRule.onNodeWithText(part.title).assertIsDisplayed()
        composeRule.onNodeWithText(part.subtitle!!).assertIsDisplayed()
    }

    @Test
    fun tocItem_clickPartRow_callsOnToggleNotOnArticleClick() {
        var toggled = false
        var articleClicked = false
        composeRule.setContent {
            TocItem(
                node = part,
                onToggle = { toggled = true },
                onArticleClick = { articleClicked = true },
            )
        }
        composeRule.onNodeWithText(part.title).performClick()
        assertTrue("onToggle not invoked", toggled)
        assertEquals(false, articleClicked)
    }

    @Test
    fun tocItem_clickLeafArticle_callsOnArticleClickNotOnToggle() {
        var toggled = false
        var articleClicked = false
        composeRule.setContent {
            TocItem(
                node = leafArticle,
                onToggle = { toggled = true },
                onArticleClick = { articleClicked = true },
            )
        }
        composeRule.onNodeWithText(leafArticle.title).performClick()
        assertTrue("onArticleClick not invoked", articleClicked)
        assertEquals(false, toggled)
    }

    @Test
    fun tocItem_unexpandedParent_showsExpandIconWithExpandContentDescription() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                TocItem(
                    node = part.copy(isExpanded = false),
                    onToggle = {},
                    onArticleClick = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.expand).assertIsDisplayed()
    }

    @Test
    fun tocItem_expandedParent_showsCollapseContentDescription() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                TocItem(
                    node = part.copy(isExpanded = true),
                    onToggle = {},
                    onArticleClick = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.collapse).assertIsDisplayed()
    }

    @Test
    fun tocItem_sectionDepth1_rendersTitle() {
        val section = TocNode(
            id = "section-1",
            title = "Τμήμα Α",
            subtitle = null,
            depth = 1,
            hasChildren = true,
            isExpanded = false,
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                TocItem(node = section, onToggle = {}, onArticleClick = {})
            }
        }
        composeRule.onNodeWithText(section.title).assertIsDisplayed()
    }

    @Test
    fun tocItem_chapterDepth2_rendersTitle() {
        val chapter = TocNode(
            id = "chapter-1",
            title = "Κεφάλαιο Α",
            subtitle = null,
            depth = 2,
            hasChildren = true,
            isExpanded = false,
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                TocItem(node = chapter, onToggle = {}, onArticleClick = {})
            }
        }
        composeRule.onNodeWithText(chapter.title).assertIsDisplayed()
    }

    @Test
    fun tocItem_leafArticle_doesNotShowExpandIcon() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                TocItem(node = leafArticle, onToggle = {}, onArticleClick = {})
            }
        }
        // Leaf has hasChildren = false, so neither expand nor collapse should appear.
        composeRule.onAllNodesWithContentDescription(greek.expand).assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(greek.collapse).assertCountEquals(0)
    }
}
