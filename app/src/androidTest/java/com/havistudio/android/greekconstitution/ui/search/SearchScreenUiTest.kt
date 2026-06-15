package com.havistudio.android.greekconstitution.ui.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.data.local.dao.SearchDao
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val greek = AppStrings.forLanguage(UiLanguage.Greek)
    private val english = AppStrings.forLanguage(UiLanguage.English)

    private val hit = SearchDao.SearchHit(
        paragraphId = 51,
        articleId = 5,
        articleNumber = "5",
        paragraphNumber = "1",
        content = "Καθένας έχει δικαίωμα να αναπτύσσει ελεύθερα την προσωπικότητά του.",
    )

    @Test
    fun resultItem_rendersGreekArticleLabel() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                ResultItem(query = "", hit = hit, onClick = {})
            }
        }
        composeRule.onNodeWithText("Άρθρο 5 · §1".uppercase(greek.locale)).assertIsDisplayed()
        composeRule.onNodeWithText(hit.content).assertIsDisplayed()
    }

    @Test
    fun resultItem_rendersEnglishArticleLabelWhenLanguageSwitched() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides english) {
                ResultItem(query = "", hit = hit, onClick = {})
            }
        }
        composeRule.onNodeWithText("Article 5 · §1".uppercase(english.locale)).assertIsDisplayed()
    }

    @Test
    fun resultItem_click_invokesOnClick() {
        var clicked = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                ResultItem(query = "", hit = hit, onClick = { clicked = true })
            }
        }
        composeRule.onNodeWithText(hit.content).performClick()
        assertTrue("onClick not invoked", clicked)
    }
}
