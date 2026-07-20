package com.havistudio.android.greekconstitution.ui.disclaimer

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DisclaimerScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val greek = AppStrings.forLanguage(UiLanguage.Greek)

    private lateinit var context: Context
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        file = File(context.cacheDir, "disclaimer_${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create { file }
        preferencesManager = PreferencesManager(dataStore)
    }

    @After
    fun tearDown() {
        if (file.exists()) file.delete()
    }

    @Test
    fun rendersTitleBodyAndAccept() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                DisclaimerScreen(preferencesManager = preferencesManager, onAccepted = {})
            }
        }
        composeRule.onNodeWithText(greek.appTitle).assertIsDisplayed()
        composeRule.onNodeWithText(greek.disclaimerBody).assertIsDisplayed()
        composeRule.onNodeWithText(greek.accept).assertIsDisplayed()
    }

    @Test
    fun tappingAccept_persistsFlagAndInvokesOnAccepted() {
        var accepted = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                DisclaimerScreen(
                    preferencesManager = preferencesManager,
                    onAccepted = { accepted = true },
                )
            }
        }

        composeRule.onNodeWithText(greek.accept).performClick()
        composeRule.waitForIdle()

        // Poll briefly for the launched coroutine to finish — Compose's waitForIdle
        // covers recomposition but not arbitrary suspending side-effects.
        repeat(20) {
            if (accepted && runBlocking { preferencesManager.hasAcceptedDisclaimer.first() }) return@repeat
            Thread.sleep(50)
        }

        assertTrue("onAccepted should fire", accepted)
        assertEquals(true, runBlocking { preferencesManager.hasAcceptedDisclaimer.first() })
    }

    @Test
    fun rendersTappableSourceLink() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppStrings provides greek) {
                DisclaimerScreen(preferencesManager = preferencesManager, onAccepted = {})
            }
        }
        composeRule.onNodeWithText("hellenicparliament.gr")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
