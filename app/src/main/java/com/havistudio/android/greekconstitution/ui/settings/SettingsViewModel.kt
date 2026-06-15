package com.havistudio.android.greekconstitution.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.havistudio.android.greekconstitution.data.local.FontSize
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.data.local.ThemePref
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.data.local.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: PreferencesManager,
) : ViewModel() {

    val state: StateFlow<UserPreferences> = prefs.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setTheme(theme: ThemePref)        = viewModelScope.launch { prefs.setTheme(theme) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { prefs.setDynamicColor(enabled) }
    fun setFontSize(size: FontSize)       = viewModelScope.launch { prefs.setFontSize(size) }
    fun setLanguage(lang: UiLanguage)     = viewModelScope.launch { prefs.setLanguage(lang) }
    fun resetDisclaimer()                 = viewModelScope.launch { prefs.resetDisclaimer() }

    class Factory(private val prefs: PreferencesManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(prefs) as T
    }
}
