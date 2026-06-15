package com.havistudio.android.greekconstitution.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PreferencesManagerTest {

    private lateinit var context: Context
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        file = File(context.cacheDir, "test_${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create { file }
    }

    @After
    fun tearDown() {
        if (file.exists()) file.delete()
    }

    @Test
    fun theme_defaultsToSystem() = runTest {
        val prefs = PreferencesManager(dataStore)

        assertEquals(ThemePref.System, prefs.preferences.first().theme)
    }

    @Test
    fun setTheme_light_emitsLight() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setTheme(ThemePref.Light)

        assertEquals(ThemePref.Light, prefs.preferences.first().theme)
    }

    @Test
    fun setTheme_dark_emitsDark() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setTheme(ThemePref.Dark)

        assertEquals(ThemePref.Dark, prefs.preferences.first().theme)
    }

    @Test
    fun setTheme_persistsAcrossInstances() = runTest {
        PreferencesManager(dataStore).setTheme(ThemePref.Dark)

        val reopened = PreferencesManager(dataStore)
        assertEquals(ThemePref.Dark, reopened.preferences.first().theme)
    }

    @Test
    fun setTheme_overwritesPreviousValue() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setTheme(ThemePref.Dark)
        prefs.setTheme(ThemePref.Light)

        assertEquals(ThemePref.Light, prefs.preferences.first().theme)
    }

    @Test
    fun dynamicColor_defaultsTrue() = runTest {
        val prefs = PreferencesManager(dataStore)

        assertEquals(true, prefs.preferences.first().dynamicColor)
    }

    @Test
    fun setDynamicColor_false_emitsFalse() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setDynamicColor(false)

        assertEquals(false, prefs.preferences.first().dynamicColor)
    }

    @Test
    fun fontSize_defaultsToDefault() = runTest {
        val prefs = PreferencesManager(dataStore)

        assertEquals(FontSize.Default, prefs.preferences.first().fontSize)
    }

    @Test
    fun setFontSize_large_emitsLarge() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setFontSize(FontSize.Large)

        assertEquals(FontSize.Large, prefs.preferences.first().fontSize)
    }

    @Test
    fun language_defaultsToGreek() = runTest {
        val prefs = PreferencesManager(dataStore)

        assertEquals(UiLanguage.Greek, prefs.preferences.first().language)
    }

    @Test
    fun setLanguage_english_emitsEnglish() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setLanguage(UiLanguage.English)

        assertEquals(UiLanguage.English, prefs.preferences.first().language)
    }

    @Test
    fun disclaimerAccepted_defaultsFalse() = runTest {
        val prefs = PreferencesManager(dataStore)

        assertEquals(false, prefs.hasAcceptedDisclaimer.first())
    }

    @Test
    fun setDisclaimerAccepted_emitsTrue() = runTest {
        val prefs = PreferencesManager(dataStore)

        prefs.setDisclaimerAccepted()

        assertEquals(true, prefs.hasAcceptedDisclaimer.first())
    }

    @Test
    fun resetDisclaimer_revertsToFalse() = runTest {
        val prefs = PreferencesManager(dataStore)
        prefs.setDisclaimerAccepted()

        prefs.resetDisclaimer()

        assertEquals(false, prefs.hasAcceptedDisclaimer.first())
    }
}
