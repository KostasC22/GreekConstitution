package com.havistudio.android.greekconstitution.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.havistudio.android.greekconstitution.data.local.FontSize
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.data.local.ThemePref
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.data.local.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dataStoreFile: File
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var prefs: PreferencesManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStoreFile = Files.createTempFile("settings_vm_test_", ".preferences_pb").toFile()
        dataStoreFile.delete() // factory wants the file to not pre-exist
        dataStoreScope = CoroutineScope(SupervisorJob() + testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) { dataStoreFile }
        prefs = PreferencesManager(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        dataStoreFile.delete()
        Dispatchers.resetMain()
    }

    @Test
    fun `state emits default UserPreferences initially`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            assertEquals(UserPreferences(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme Light updates state`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            awaitItem() // default
            vm.setTheme(ThemePref.Light)
            assertEquals(ThemePref.Light, awaitItem().theme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme Dark updates state`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            awaitItem()
            vm.setTheme(ThemePref.Dark)
            assertEquals(ThemePref.Dark, awaitItem().theme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTheme System persists round-trip`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            awaitItem()
            vm.setTheme(ThemePref.Dark)
            awaitItem()
            vm.setTheme(ThemePref.System)
            assertEquals(ThemePref.System, awaitItem().theme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDynamicColor false updates state`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            assertEquals(true, awaitItem().dynamicColor) // default true
            vm.setDynamicColor(false)
            assertEquals(false, awaitItem().dynamicColor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setFontSize cycles through every value`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            assertEquals(FontSize.Default, awaitItem().fontSize)
            vm.setFontSize(FontSize.Small)
            assertEquals(FontSize.Small, awaitItem().fontSize)
            vm.setFontSize(FontSize.Large)
            assertEquals(FontSize.Large, awaitItem().fontSize)
            vm.setFontSize(FontSize.XLarge)
            assertEquals(FontSize.XLarge, awaitItem().fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLanguage English updates state`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            assertEquals(UiLanguage.Greek, awaitItem().language) // default
            vm.setLanguage(UiLanguage.English)
            assertEquals(UiLanguage.English, awaitItem().language)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetDisclaimer clears disclaimerAccepted`() = runTest {
        prefs.setDisclaimerAccepted()
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            // Drain any pre-reset emission(s) until we observe accepted=true,
            // then trigger reset.
            var current = awaitItem()
            while (!current.disclaimerAccepted) current = awaitItem()
            assertEquals(true, current.disclaimerAccepted)

            vm.resetDisclaimer()

            assertEquals(false, awaitItem().disclaimerAccepted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `combined setters propagate across fields`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            awaitItem()
            vm.setTheme(ThemePref.Dark)
            vm.setLanguage(UiLanguage.English)
            vm.setFontSize(FontSize.Large)

            // Each setter triggers an emission; consume until all three are visible.
            var s = awaitItem()
            while (s.theme != ThemePref.Dark ||
                s.language != UiLanguage.English ||
                s.fontSize != FontSize.Large
            ) s = awaitItem()

            assertEquals(ThemePref.Dark, s.theme)
            assertEquals(UiLanguage.English, s.language)
            assertEquals(FontSize.Large, s.fontSize)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setting same value does not regress other fields`() = runTest {
        val vm = SettingsViewModel(prefs)

        vm.state.test {
            awaitItem()
            vm.setLanguage(UiLanguage.English)
            val afterLang = awaitItem()
            assertEquals(UiLanguage.English, afterLang.language)
            assertEquals(ThemePref.System, afterLang.theme)

            vm.setTheme(ThemePref.Light)
            val afterTheme = awaitItem()
            assertEquals(UiLanguage.English, afterTheme.language) // language preserved
            assertEquals(ThemePref.Light, afterTheme.theme)

            assertNotEquals(UserPreferences(), afterTheme)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
