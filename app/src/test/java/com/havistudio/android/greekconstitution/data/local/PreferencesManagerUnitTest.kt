package com.havistudio.android.greekconstitution.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * JVM-only counterpart to the instrumented [PreferencesManagerTest]. Uses a
 * temp-file `PreferenceDataStoreFactory.create` so no Android Context is
 * required, which lets the coverage land in the unit suite.
 *
 * The two suites overlap by design: the instrumented test exists to catch a
 * future regression in the Context-coupled `preferencesDataStore` extension;
 * this one exercises the same surface as a fast unit pass.
 */
class PreferencesManagerUnitTest {

    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setup() {
        file = Files.createTempFile("prefs_unit", ".preferences_pb").toFile().apply { delete() }
        dataStore = PreferenceDataStoreFactory.create { file }
    }

    @After
    fun tearDown() {
        if (file.exists()) file.delete()
    }

    @Test
    fun preferences_defaultValues() = runTest {
        val prefs = PreferencesManager(dataStore).preferences.first()

        assertEquals(ThemePref.System, prefs.theme)
        assertEquals(true, prefs.dynamicColor)
        assertEquals(FontSize.Default, prefs.fontSize)
        assertEquals(UiLanguage.Greek, prefs.language)
        assertEquals(false, prefs.disclaimerAccepted)
    }

    @Test
    fun setTheme_eachVariantRoundTrips() = runTest {
        val mgr = PreferencesManager(dataStore)

        mgr.setTheme(ThemePref.Light)
        assertEquals(ThemePref.Light, mgr.preferences.first().theme)

        mgr.setTheme(ThemePref.Dark)
        assertEquals(ThemePref.Dark, mgr.preferences.first().theme)

        mgr.setTheme(ThemePref.System)
        assertEquals(ThemePref.System, mgr.preferences.first().theme)
    }

    @Test
    fun setDynamicColor_roundTrips() = runTest {
        val mgr = PreferencesManager(dataStore)

        mgr.setDynamicColor(false)
        assertEquals(false, mgr.preferences.first().dynamicColor)

        mgr.setDynamicColor(true)
        assertEquals(true, mgr.preferences.first().dynamicColor)
    }

    @Test
    fun setFontSize_eachVariantRoundTrips() = runTest {
        val mgr = PreferencesManager(dataStore)

        FontSize.values().forEach { size ->
            mgr.setFontSize(size)
            assertEquals(size, mgr.preferences.first().fontSize)
        }
    }

    @Test
    fun setLanguage_eachVariantRoundTrips() = runTest {
        val mgr = PreferencesManager(dataStore)

        UiLanguage.values().forEach { lang ->
            mgr.setLanguage(lang)
            assertEquals(lang, mgr.preferences.first().language)
        }
    }

    @Test
    fun disclaimer_acceptThenReset() = runTest {
        val mgr = PreferencesManager(dataStore)

        assertEquals(false, mgr.hasAcceptedDisclaimer.first())

        mgr.setDisclaimerAccepted()
        assertEquals(true, mgr.hasAcceptedDisclaimer.first())
        assertEquals(true, mgr.preferences.first().disclaimerAccepted)

        mgr.resetDisclaimer()
        assertEquals(false, mgr.hasAcceptedDisclaimer.first())
        assertEquals(false, mgr.preferences.first().disclaimerAccepted)
    }

    @Test
    fun valuesPersistAcrossInstances() = runTest {
        PreferencesManager(dataStore).apply {
            setTheme(ThemePref.Dark)
            setFontSize(FontSize.Large)
            setLanguage(UiLanguage.English)
            setDynamicColor(false)
        }

        val reopened = PreferencesManager(dataStore).preferences.first()

        assertEquals(ThemePref.Dark, reopened.theme)
        assertEquals(FontSize.Large, reopened.fontSize)
        assertEquals(UiLanguage.English, reopened.language)
        assertEquals(false, reopened.dynamicColor)
    }

    @Test
    fun themePref_fromKey_unknownFallsBackToSystem() {
        assertEquals(ThemePref.System, ThemePref.fromKey(null))
        assertEquals(ThemePref.System, ThemePref.fromKey("nonsense"))
        assertEquals(ThemePref.Light, ThemePref.fromKey("light"))
    }

    @Test
    fun uiLanguage_fromTag_unknownFallsBackToGreek() {
        assertEquals(UiLanguage.Greek, UiLanguage.fromTag(null))
        assertEquals(UiLanguage.Greek, UiLanguage.fromTag("xx"))
        assertEquals(UiLanguage.English, UiLanguage.fromTag("en"))
    }

    @Test
    fun fontSize_fromIndex_unknownFallsBackToDefault() {
        assertEquals(FontSize.Default, FontSize.fromIndex(null))
        assertEquals(FontSize.Default, FontSize.fromIndex(99))
        assertEquals(FontSize.XLarge, FontSize.fromIndex(3))
    }
}
