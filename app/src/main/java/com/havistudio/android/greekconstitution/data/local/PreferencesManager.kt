package com.havistudio.android.greekconstitution.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Shared `"settings"` DataStore. Exposed `internal` so siblings in this package
 * — like [SearchHistoryManager] — can read/write the same preference file
 * instead of opening a second `preferencesDataStore(name = "settings")`, which
 * throws at runtime.
 */
internal val Context.preferencesDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "settings")

/**
 * Theme preference for the user-facing app UI. Persisted as a string so we
 * never have to migrate ordinal values if a new option is added later.
 */
enum class ThemePref(val key: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromKey(key: String?): ThemePref =
            values().firstOrNull { it.key == key } ?: System
    }
}

/** UI language. Constitution body text always stays in Greek. */
enum class UiLanguage(val tag: String) {
    Greek("el"),
    English("en");

    companion object {
        fun fromTag(tag: String?): UiLanguage =
            values().firstOrNull { it.tag == tag } ?: Greek
    }
}

/**
 * Reading-pane font size — discrete steps so we ship a tested type
 * ramp instead of a free slider. Indices map to scale factors for
 * downstream use.
 */
enum class FontSize(val index: Int, val scale: Float) {
    Small(0, 0.875f),
    Default(1, 1.0f),
    Large(2, 1.15f),
    XLarge(3, 1.3f);

    companion object {
        fun fromIndex(idx: Int?): FontSize =
            values().firstOrNull { it.index == idx } ?: Default
    }
}

data class UserPreferences(
    val theme: ThemePref = ThemePref.System,
    val dynamicColor: Boolean = true,
    val fontSize: FontSize = FontSize.Default,
    val language: UiLanguage = UiLanguage.Greek,
    val disclaimerAccepted: Boolean = false,
)

class PreferencesManager(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.preferencesDataStore)

    /** Single source of truth — everything else derives from this. */
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            theme              = ThemePref.fromKey(prefs[THEME]),
            dynamicColor       = prefs[DYNAMIC_COLOR] ?: true,
            fontSize           = FontSize.fromIndex(prefs[FONT_SIZE]),
            language           = UiLanguage.fromTag(prefs[UI_LANGUAGE]),
            disclaimerAccepted = prefs[DISCLAIMER_ACCEPTED] == true,
        )
    }

    val hasAcceptedDisclaimer: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[DISCLAIMER_ACCEPTED] == true }

    suspend fun setDisclaimerAccepted() {
        dataStore.edit { it[DISCLAIMER_ACCEPTED] = true }
    }

    /** Re-arm the disclaimer so the user sees it again on next launch. */
    suspend fun resetDisclaimer() {
        dataStore.edit { it[DISCLAIMER_ACCEPTED] = false }
    }

    suspend fun setTheme(theme: ThemePref) {
        dataStore.edit { it[THEME] = theme.key }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setFontSize(size: FontSize) {
        dataStore.edit { it[FONT_SIZE] = size.index }
    }

    suspend fun setLanguage(language: UiLanguage) {
        dataStore.edit { it[UI_LANGUAGE] = language.tag }
    }

    private companion object {
        val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
        val THEME               = stringPreferencesKey("theme")
        val DYNAMIC_COLOR       = booleanPreferencesKey("dynamic_color")
        val FONT_SIZE           = intPreferencesKey("font_size")
        val UI_LANGUAGE         = stringPreferencesKey("ui_language")
    }
}
