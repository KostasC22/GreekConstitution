package com.havistudio.android.greekconstitution.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the user's recent search queries to DataStore.
 *
 * Reuses the same `"settings"` DataStore as [PreferencesManager] — instantiating
 * a second `preferencesDataStore(name = "settings")` would throw at runtime.
 *
 * Storage format: a single string, queries delimited by ASCII Unit Separator
 * (``) — picked over JSON because order matters and the delimiter can't
 * appear in user-typed text.
 *
 * Most-recent-first, case-insensitively de-duplicated, capped at [MAX_RECENTS].
 */
class SearchHistoryManager(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.preferencesDataStore)

    private companion object {
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        const val DELIMITER = ''
        const val MAX_RECENTS = 8
    }

    val recents: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[RECENT_SEARCHES]
            ?.split(DELIMITER)
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    suspend fun record(rawQuery: String) {
        val query = rawQuery.trim()
        if (query.isBlank()) return
        dataStore.edit { prefs ->
            val current = prefs[RECENT_SEARCHES]
                ?.split(DELIMITER)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val deduped = listOf(query) + current.filter { !it.equals(query, ignoreCase = true) }
            prefs[RECENT_SEARCHES] = deduped.take(MAX_RECENTS).joinToString(DELIMITER.toString())
        }
    }

    suspend fun remove(query: String) {
        dataStore.edit { prefs ->
            val current = prefs[RECENT_SEARCHES]
                ?.split(DELIMITER)
                ?.filter { it.isNotBlank() && !it.equals(query, ignoreCase = true) }
                ?: emptyList()
            if (current.isEmpty()) prefs.remove(RECENT_SEARCHES)
            else prefs[RECENT_SEARCHES] = current.joinToString(DELIMITER.toString())
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs -> prefs.remove(RECENT_SEARCHES) }
    }
}
