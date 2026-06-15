package com.havistudio.android.greekconstitution.ui.bookmarks

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Note
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarksScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val greek = AppStrings.forLanguage(UiLanguage.Greek)
    private val english = AppStrings.forLanguage(UiLanguage.English)

    private val article5 = Article(
        id = 5, number = "5", partId = 1, sectionId = null, chapterId = null,
        order = 5, title = "Ελεύθερη ανάπτυξη της προσωπικότητας",
    )

    private val noteForArticle5 = Note(
        id = 11L, articleId = 5, paragraphId = null,
        content = "Σημείωση δοκιμή",
        createdAt = 1000L, updatedAt = 1000L,
    )

    @Test
    fun emptyBookmarks_rendersTitleAndHint() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                EmptyBookmarks()
            }
        }
        composeRule.onNodeWithText(greek.bookmarksEmpty).assertIsDisplayed()
        composeRule.onNodeWithText(greek.bookmarksEmptyHint).assertIsDisplayed()
    }

    @Test
    fun emptyBookmarks_rendersEnglishWhenLanguageSwitched() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides english) {
                EmptyBookmarks()
            }
        }
        composeRule.onNodeWithText(english.bookmarksEmpty).assertIsDisplayed()
    }

    @Test
    fun bookmarkItem_rendersTitleAndArticleLabel() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                BookmarkItem(
                    article = article5,
                    latestNote = null,
                    onClick = {},
                    onRemove = {},
                )
            }
        }
        composeRule.onNodeWithText(article5.title!!).assertIsDisplayed()
        composeRule.onNodeWithText("Άρθρο 5".uppercase(greek.locale)).assertIsDisplayed()
    }

    @Test
    fun bookmarkItem_withNote_showsNotePreview() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                BookmarkItem(
                    article = article5,
                    latestNote = noteForArticle5,
                    onClick = {},
                    onRemove = {},
                )
            }
        }
        composeRule.onNodeWithText(noteForArticle5.content).assertIsDisplayed()
    }

    @Test
    fun bookmarkItem_clickRow_invokesOnClick() {
        var clicked = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                BookmarkItem(
                    article = article5,
                    latestNote = null,
                    onClick = { clicked = true },
                    onRemove = {},
                )
            }
        }
        composeRule.onNodeWithText(article5.title!!).performClick()
        assertTrue("onClick not invoked", clicked)
    }

    @Test
    fun bookmarkItem_clickTrailingBookmarkIcon_invokesOnRemove() {
        var removed = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                BookmarkItem(
                    article = article5,
                    latestNote = null,
                    onClick = {},
                    onRemove = { removed = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.removeBookmark).performClick()
        assertTrue("onRemove not invoked", removed)
    }
}
