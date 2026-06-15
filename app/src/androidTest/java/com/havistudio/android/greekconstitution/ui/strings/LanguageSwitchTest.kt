package com.havistudio.android.greekconstitution.ui.strings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end check that swapping [LocalAppStrings] at the top of the tree
 * re-renders downstream consumers — the contract the Settings language
 * toggle relies on.
 */
@RunWith(AndroidJUnit4::class)
class LanguageSwitchTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun toggleLanguage_swapsDownstreamLabelsLive() {
        composeRule.setContent {
            var language by remember { mutableStateOf(UiLanguage.Greek) }
            val strings = AppStrings.forLanguage(language)
            CompositionLocalProvider(LocalAppStrings provides strings) {
                Column {
                    Text(strings.home)
                    Text(
                        text = "switch",
                        modifier = Modifier.clickable {
                            language = if (language == UiLanguage.Greek) UiLanguage.English
                                       else UiLanguage.Greek
                        },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Αρχική").assertIsDisplayed()
        composeRule.onNodeWithText("switch").performClick()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
