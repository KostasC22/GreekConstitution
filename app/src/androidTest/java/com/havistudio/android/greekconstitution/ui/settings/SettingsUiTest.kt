package com.havistudio.android.greekconstitution.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.FontSize
import com.havistudio.android.greekconstitution.data.local.ThemePref
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val labels = AppStrings.forLanguage(UiLanguage.Greek)

    @Test
    fun themeSegmented_clickLight_callsOnSelectWithLight() {
        var picked: ThemePref? = null
        composeRule.setContent {
            ThemeSegmented(
                selected = ThemePref.System,
                labels = labels,
                onSelect = { picked = it },
            )
        }

        composeRule.onNodeWithText(labels.themeLight).performClick()

        assertEquals(ThemePref.Light, picked)
    }

    @Test
    fun themeSegmented_currentSelectionIsDisplayed() {
        composeRule.setContent {
            ThemeSegmented(
                selected = ThemePref.Dark,
                labels = labels,
                onSelect = {},
            )
        }

        composeRule.onNodeWithText(labels.themeSystem).assertIsDisplayed()
        composeRule.onNodeWithText(labels.themeLight).assertIsDisplayed()
        composeRule.onNodeWithText(labels.themeDark).assertIsDisplayed()
    }

    @Test
    fun themeSegmented_clickAlreadySelected_stillFiresCallback() {
        var picked: ThemePref? = null
        composeRule.setContent {
            ThemeSegmented(
                selected = ThemePref.System,
                labels = labels,
                onSelect = { picked = it },
            )
        }

        composeRule.onNodeWithText(labels.themeSystem).performClick()

        assertEquals(ThemePref.System, picked)
    }

    @Test
    fun languageChip_unselected_clickFiresCallback() {
        var clicked = false
        composeRule.setContent {
            LanguageChip(
                label = "English",
                selected = false,
                onClick = { clicked = true },
            )
        }

        composeRule.onNodeWithText("English").performClick()

        assertEquals(true, clicked)
    }

    @Test
    fun languageChip_selectedAndUnselected_bothShowLabel() {
        composeRule.setContent {
            LanguageChip(label = "Ελληνικά", selected = true, onClick = {})
        }
        composeRule.onNodeWithText("Ελληνικά").assertIsDisplayed()
    }

    // ── SectionHeader ──────────────────────────────────────────────

    @Test
    fun sectionHeader_uppercasesText() {
        composeRule.setContent {
            SectionHeader(text = labels.appearance)
        }
        composeRule.onNodeWithText(labels.appearance.uppercase()).assertIsDisplayed()
    }

    // ── ThemeRow ───────────────────────────────────────────────────

    @Test
    fun themeRow_rendersLabelAndSegmentedOptions() {
        composeRule.setContent {
            ThemeRow(labels = labels, selected = ThemePref.System, onSelect = {})
        }
        composeRule.onNodeWithText(labels.theme).assertIsDisplayed()
        composeRule.onNodeWithText(labels.themeSystem).assertIsDisplayed()
        composeRule.onNodeWithText(labels.themeLight).assertIsDisplayed()
        composeRule.onNodeWithText(labels.themeDark).assertIsDisplayed()
    }

    @Test
    fun themeRow_clickDarkOption_propagates() {
        var picked: ThemePref? = null
        composeRule.setContent {
            ThemeRow(labels = labels, selected = ThemePref.System, onSelect = { picked = it })
        }
        composeRule.onNodeWithText(labels.themeDark).performClick()
        assertEquals(ThemePref.Dark, picked)
    }

    // ── SwitchRow ──────────────────────────────────────────────────

    @Test
    fun switchRow_clickRow_togglesCheckedState() {
        var toggled: Boolean? = null
        composeRule.setContent {
            SwitchRow(
                icon = Icons.Default.Brightness6,
                headline = labels.dynamicColor,
                supporting = labels.dynamicColorSub,
                checked = false,
                onCheckedChange = { toggled = it },
            )
        }
        composeRule.onNodeWithText(labels.dynamicColor).performClick()
        assertEquals(true, toggled)
    }

    @Test
    fun switchRow_showsSupportingTextWhenProvided() {
        composeRule.setContent {
            SwitchRow(
                icon = Icons.Default.Brightness6,
                headline = labels.dynamicColor,
                supporting = labels.dynamicColorSub,
                checked = true,
                onCheckedChange = {},
            )
        }
        composeRule.onNodeWithText(labels.dynamicColorSub).assertIsDisplayed()
    }

    // ── FontSizeRow ────────────────────────────────────────────────

    @Test
    fun fontSizeRow_rendersHeadlineAndCurrentSelectionLabel() {
        composeRule.setContent {
            FontSizeRow(labels = labels, selected = FontSize.Large, onSelect = {})
        }
        composeRule.onNodeWithText(labels.fontSize).assertIsDisplayed()
        composeRule.onNodeWithText(labels.fontLarge).assertIsDisplayed()
    }

    @Test
    fun fontSizeRow_rendersPreviewSection() {
        composeRule.setContent {
            FontSizeRow(labels = labels, selected = FontSize.Default, onSelect = {})
        }
        composeRule.onNodeWithText(labels.previewLabel).assertIsDisplayed()
    }

    // ── LanguageRow ────────────────────────────────────────────────

    @Test
    fun languageRow_rendersBothLanguageChipsAndNote() {
        composeRule.setContent {
            LanguageRow(selected = UiLanguage.Greek, onSelect = {}, note = labels.languageNote)
        }
        composeRule.onNodeWithText("Ελληνικά").assertIsDisplayed()
        composeRule.onNodeWithText("English").assertIsDisplayed()
        composeRule.onNodeWithText(labels.languageNote).assertIsDisplayed()
    }

    @Test
    fun languageRow_clickEnglish_invokesOnSelectWithEnglish() {
        var picked: UiLanguage? = null
        composeRule.setContent {
            LanguageRow(
                selected = UiLanguage.Greek,
                onSelect = { picked = it },
                note = labels.languageNote,
            )
        }
        composeRule.onNodeWithText("English").performClick()
        assertEquals(UiLanguage.English, picked)
    }
}
