package com.havistudio.android.greekconstitution.ui.article

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.entity.Paragraph
import com.havistudio.android.greekconstitution.ui.strings.LocalReadingFontScale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleDetailFontScaleTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sample = Paragraph(
        id = 1,
        articleId = 1,
        number = "1",
        order = 0,
        content = "Test paragraph content for font scale rendering.",
        searchContent = "test paragraph content for font scale rendering",
    )

    @Test
    fun paragraphItem_rendersAtDefaultScale() {
        composeRule.setContent {
            CompositionLocalProvider(LocalReadingFontScale provides 1.0f) {
                ParagraphItem(sample)
            }
        }
        composeRule.onNodeWithText(sample.content).assertIsDisplayed()
        composeRule.onNodeWithText("1.").assertIsDisplayed()
    }

    @Test
    fun paragraphItem_rendersAtLargerScaleWithoutCrash() {
        composeRule.setContent {
            CompositionLocalProvider(LocalReadingFontScale provides 1.5f) {
                ParagraphItem(sample)
            }
        }
        composeRule.onNodeWithText(sample.content).assertIsDisplayed()
        composeRule.onNodeWithText("1.").assertIsDisplayed()
    }

    @Test
    fun paragraphItem_rendersAtSmallerScaleWithoutCrash() {
        composeRule.setContent {
            CompositionLocalProvider(LocalReadingFontScale provides 0.875f) {
                ParagraphItem(sample)
            }
        }
        composeRule.onNodeWithText(sample.content).assertIsDisplayed()
        composeRule.onNodeWithText("1.").assertIsDisplayed()
    }
}
