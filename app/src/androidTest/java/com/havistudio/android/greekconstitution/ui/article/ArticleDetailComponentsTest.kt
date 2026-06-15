package com.havistudio.android.greekconstitution.ui.article

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.data.local.entity.InterpretiveClause
import com.havistudio.android.greekconstitution.data.local.entity.Note
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose tests for the smaller, side-effect-free composables that make up
 * [ArticleDetailScreen]. The full screen needs a real repository + DataStore
 * to render — covered (indirectly) by the bookmarks / disclaimer flows; these
 * tests exercise the leaf composables in isolation.
 */
@RunWith(AndroidJUnit4::class)
class ArticleDetailComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val greek = AppStrings.forLanguage(UiLanguage.Greek)
    private val english = AppStrings.forLanguage(UiLanguage.English)

    private val noteSample = Note(
        id = 1L, articleId = 5, paragraphId = null,
        content = "Παρατήρηση δοκιμή",
        createdAt = 1_700_000_000_000L, updatedAt = 1_700_000_000_000L,
    )

    // ── ArticleEyebrow ─────────────────────────────────────────────

    @Test
    fun articleEyebrow_paragraphsOnly() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                ArticleEyebrow(paragraphsLabel = greek.paragraphsCount(3), clausesLabel = null)
            }
        }
        composeRule.onNodeWithText(greek.paragraphsCount(3).uppercase(greek.locale))
            .assertIsDisplayed()
    }

    @Test
    fun articleEyebrow_withClauses_joinsWithMiddot() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                ArticleEyebrow(
                    paragraphsLabel = greek.paragraphsCount(2),
                    clausesLabel = greek.clausesCount(1),
                )
            }
        }
        val expected = "${greek.paragraphsCount(2)} · ${greek.clausesCount(1)}".uppercase(greek.locale)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }

    // ── ArticleHeadline ────────────────────────────────────────────

    @Test
    fun articleHeadline_rendersGivenTitle() {
        composeRule.setContent { ArticleHeadline(title = "Άρθρο 5") }
        composeRule.onNodeWithText("Άρθρο 5").assertIsDisplayed()
    }

    // ── CrossRefChip ───────────────────────────────────────────────

    @Test
    fun crossRefChip_rendersArrowAndArticlePrefix() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                CrossRefChip(refId = 13, onClick = {})
            }
        }
        composeRule.onNodeWithText("↗ ${greek.articlePrefix} 13").assertIsDisplayed()
    }

    @Test
    fun crossRefChip_click_invokesCallbackWithRefId() {
        var clickedId: Int? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                CrossRefChip(refId = 13, onClick = { clickedId = 13 })
            }
        }
        composeRule.onNodeWithText("↗ ${greek.articlePrefix} 13").performClick()
        assertEquals(13, clickedId)
    }

    // ── InterpretiveClauseItem ─────────────────────────────────────

    @Test
    fun interpretiveClauseItem_rendersHeaderAndContent() {
        val clause = InterpretiveClause(
            id = 1, articleId = 5,
            content = "Δηλωτική παρατήρηση δοκιμή.",
            searchContent = "δηλωτικη παρατηρηση δοκιμη",
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                InterpretiveClauseItem(clause = clause)
            }
        }
        composeRule.onNodeWithText(greek.interpretiveClause.uppercase(greek.locale))
            .assertIsDisplayed()
        composeRule.onNodeWithText(clause.content).assertIsDisplayed()
    }

    // ── NextArticleFooter ──────────────────────────────────────────

    @Test
    fun nextArticleFooter_rendersEyebrowAndLabel() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NextArticleFooter(
                    eyebrow = greek.nextArticleEyebrow,
                    label = "Άρθρο 6 — Ιδιοκτησία",
                    onClick = {},
                )
            }
        }
        composeRule.onNodeWithText(greek.nextArticleEyebrow.uppercase(greek.locale))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Άρθρο 6 — Ιδιοκτησία").assertIsDisplayed()
    }

    @Test
    fun nextArticleFooter_click_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NextArticleFooter(
                    eyebrow = greek.nextArticleEyebrow,
                    label = "Άρθρο 6",
                    onClick = { clicked = true },
                )
            }
        }
        composeRule.onNodeWithText("Άρθρο 6").performClick()
        assertTrue("onClick should fire", clicked)
    }

    // ── NoteSection ─────────────────────────────────────────────────

    @Test
    fun noteSection_addButton_invokesOnAddRequest() {
        var requested = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteSection(
                    notes = emptyList(),
                    onAddRequest = { requested = true },
                    onEditRequest = {},
                    onDelete = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.addNote).performClick()
        assertTrue("onAddRequest should fire", requested)
    }

    @Test
    fun noteSection_listsExistingNotes() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteSection(
                    notes = listOf(noteSample),
                    onAddRequest = {},
                    onEditRequest = {},
                    onDelete = {},
                )
            }
        }
        composeRule.onNodeWithText(greek.notes).assertIsDisplayed()
        composeRule.onNodeWithText(noteSample.content).assertIsDisplayed()
    }

    // ── NoteItem ───────────────────────────────────────────────────

    @Test
    fun noteItem_editIcon_invokesOnEdit() {
        var edited = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteItem(
                    note = noteSample,
                    onEdit = { edited = true },
                    onDelete = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.editNote).performClick()
        assertTrue("onEdit should fire", edited)
    }

    @Test
    fun noteItem_deleteIcon_showsConfirmDialog_andConfirmTriggersDelete() {
        var deleted = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteItem(
                    note = noteSample,
                    onEdit = {},
                    onDelete = { deleted = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.deleteNote).performClick()
        // Confirmation dialog title + confirm button are visible
        composeRule.onNodeWithText(greek.deleteNoteTitle).assertIsDisplayed()
        composeRule.onNodeWithText(greek.deleteNoteBody).assertIsDisplayed()
        // Confirm the deletion — confirm button shares the `deleteNote` label.
        composeRule.onNodeWithText(greek.deleteNote).performClick()
        assertTrue("onDelete should fire after confirm", deleted)
    }

    @Test
    fun noteItem_deleteIcon_cancelDialog_doesNotTriggerDelete() {
        var deleted = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteItem(
                    note = noteSample,
                    onEdit = {},
                    onDelete = { deleted = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription(greek.deleteNote).performClick()
        composeRule.onNodeWithText(greek.cancel).performClick()
        assertEquals(false, deleted)
    }

    // ── NoteEditorSheet ────────────────────────────────────────────

    @Test
    fun noteEditorSheet_newTarget_rendersNewNoteTitle_andSaveDisabledUntilNonBlank() {
        var saved: String? = null
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteEditorSheet(
                    target = NoteSheetTarget.New,
                    onDismiss = {},
                    onSave = { saved = it },
                )
            }
        }
        composeRule.onNodeWithText(greek.newNoteTitle).assertIsDisplayed()
        // Save shouldn't fire while the draft is blank.
        composeRule.onNodeWithText(greek.save).performClick()
        assertEquals(null, saved)

        // Type something — Save should now be active.
        composeRule.onNodeWithText(greek.noteHint).performTextInput("Σκέψη")
        composeRule.onNodeWithText(greek.save).performClick()
        assertEquals("Σκέψη", saved)
    }

    @Test
    fun noteEditorSheet_editTarget_rendersEditTitle_andPrefillsDraft() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteEditorSheet(
                    target = NoteSheetTarget.Edit(noteSample),
                    onDismiss = {},
                    onSave = {},
                )
            }
        }
        composeRule.onNodeWithText(greek.editNoteTitle).assertIsDisplayed()
        composeRule.onNodeWithText(noteSample.content).assertIsDisplayed()
    }

    @Test
    fun noteEditorSheet_cancelButton_invokesOnDismiss() {
        var dismissed = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                NoteEditorSheet(
                    target = NoteSheetTarget.New,
                    onDismiss = { dismissed = true },
                    onSave = {},
                )
            }
        }
        composeRule.onNodeWithText(greek.cancel).performClick()
        assertTrue("onDismiss should fire", dismissed)
    }

    @Test
    fun articleEyebrow_localeUppercaseSwitchesToEnglish() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides english) {
                ArticleEyebrow(paragraphsLabel = english.paragraphsCount(2), clausesLabel = null)
            }
        }
        composeRule.onNodeWithText(english.paragraphsCount(2).uppercase(english.locale))
            .assertIsDisplayed()
    }
}
