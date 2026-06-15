package com.havistudio.android.greekconstitution.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SearchHistoryManagerTest {

    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setup() {
        file = Files.createTempFile("search_history", ".preferences_pb").toFile().apply { delete() }
        dataStore = PreferenceDataStoreFactory.create { file }
    }

    @After
    fun tearDown() {
        if (file.exists()) file.delete()
    }

    @Test
    fun recents_defaultsToEmpty() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        assertEquals(emptyList<String>(), mgr.recents.first())
    }

    @Test
    fun record_thenReadBack() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("ελευθερία")

        assertEquals(listOf("ελευθερία"), mgr.recents.first())
    }

    @Test
    fun record_putsNewestFirst() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("ισότητα")
        mgr.record("ελευθερία")
        mgr.record("παιδεία")

        assertEquals(listOf("παιδεία", "ελευθερία", "ισότητα"), mgr.recents.first())
    }

    @Test
    fun record_blank_isNoOp() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("")
        mgr.record("   ")

        assertEquals(emptyList<String>(), mgr.recents.first())
    }

    @Test
    fun record_trims_whitespace() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("  ελευθερία  ")

        assertEquals(listOf("ελευθερία"), mgr.recents.first())
    }

    @Test
    fun record_dedupesCaseInsensitively_bumpsToTop() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("ελευθερία")
        mgr.record("ισότητα")
        mgr.record("Ελευθερία") // same as first, different case

        // Only the latest spelling sticks, and it moves to the front.
        assertEquals(listOf("Ελευθερία", "ισότητα"), mgr.recents.first())
    }

    @Test
    fun record_capsAt8_oldestFallsOff() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        repeat(9) { i -> mgr.record("query$i") }

        val list = mgr.recents.first()
        assertEquals(8, list.size)
        assertEquals("query8", list.first())
        // query0 should have been evicted
        assertTrue("query0 should be gone: $list", "query0" !in list)
    }

    @Test
    fun remove_dropsSingleEntry() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("a")
        mgr.record("b")
        mgr.record("c")

        mgr.remove("b")

        assertEquals(listOf("c", "a"), mgr.recents.first())
    }

    @Test
    fun remove_isCaseInsensitive() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        // Case-insensitive only — diacritics are compared verbatim (the current
        // contract uses `equals(ignoreCase = true)`, not GreekTextNormalizer).
        mgr.record("ελευθερια")
        mgr.record("ισότητα")

        mgr.remove("ΕΛΕΥΘΕΡΙΑ")

        assertEquals(listOf("ισότητα"), mgr.recents.first())
    }

    @Test
    fun remove_lastEntry_emptiesList() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.record("only")
        mgr.remove("only")

        assertEquals(emptyList<String>(), mgr.recents.first())
    }

    @Test
    fun clearAll_wipesEverything() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        repeat(5) { i -> mgr.record("q$i") }

        mgr.clearAll()

        assertEquals(emptyList<String>(), mgr.recents.first())
    }

    @Test
    fun clearAll_onEmpty_isHarmless() = runTest {
        val mgr = SearchHistoryManager(dataStore)

        mgr.clearAll()

        assertEquals(emptyList<String>(), mgr.recents.first())
    }
}
