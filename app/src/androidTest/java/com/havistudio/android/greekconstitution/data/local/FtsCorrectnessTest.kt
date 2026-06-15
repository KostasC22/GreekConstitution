package com.havistudio.android.greekconstitution.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.dao.SearchDao
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import com.havistudio.android.greekconstitution.util.GreekTextNormalizer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FtsCorrectnessTest {

    private lateinit var db: ConstitutionDatabase
    private lateinit var searchDao: SearchDao
    private lateinit var repository: ConstitutionRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ConstitutionDatabase::class.java,
        ).allowMainThreadQueries().build()

        searchDao = db.searchDao()
        repository = ConstitutionRepository(
            constitutionDao = db.constitutionDao(),
            bookmarkDao = db.bookmarkDao(),
            noteDao = db.noteDao(),
            searchDao = searchDao,
        )

        val sdb = db.openHelper.writableDatabase

        // Insert parent part
        sdb.insert("parts", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", 1); put("`order`", 1); put("title", "Part 1")
            },
        )

        // Insert articles
        for (i in 1..5) {
            sdb.insert("articles", SQLiteDatabase.CONFLICT_REPLACE,
                android.content.ContentValues().apply {
                    put("id", i); put("number", "$i"); put("partId", 1); put("`order`", i)
                },
            )
        }

        // Insert paragraphs with original content and normalized searchContent
        val testData = listOf(
            Triple(1, 1, "Το Σύνταγμα της Ελλάδας"),
            Triple(2, 2, "Ατομικά και κοινωνικά δικαιώματα"),
            Triple(3, 3, "Η Βουλή των Ελλήνων"),
            Triple(4, 4, "Ο Πρόεδρος της Δημοκρατίας"),
            Triple(5, 5, "Η ελευθερία της θρησκευτικής συνείδησης"),
        )
        for ((id, articleId, content) in testData) {
            val searchContent = GreekTextNormalizer.normalize(content)
            sdb.insert("paragraphs", SQLiteDatabase.CONFLICT_REPLACE,
                android.content.ContentValues().apply {
                    put("id", id); put("articleId", articleId); putNull("number")
                    put("`order`", 1); put("content", content); put("searchContent", searchContent)
                },
            )
            // Also insert into FTS table
            sdb.execSQL(
                "INSERT INTO paragraph_fts(rowid, searchContent) VALUES (?, ?)",
                arrayOf(id, searchContent),
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun search_syntagma_matchesArticle1() = runTest {
        val results = repository.search("σύνταγμα")
        assertEquals(1, results.size)
        assertEquals(1, results[0].articleId)
    }

    @Test
    fun search_dikaiomata_matchesArticle2() = runTest {
        val results = repository.search("δικαιώματα")
        assertEquals(1, results.size)
        assertEquals(2, results[0].articleId)
    }

    @Test
    fun search_vouli_matchesArticle3() = runTest {
        val results = repository.search("βουλή")
        assertEquals(1, results.size)
        assertEquals(3, results[0].articleId)
    }

    @Test
    fun search_proedros_matchesArticle4() = runTest {
        val results = repository.search("πρόεδρος")
        assertEquals(1, results.size)
        assertEquals(4, results[0].articleId)
    }

    @Test
    fun search_eleutheria_matchesArticle5() = runTest {
        val results = repository.search("ελευθερία")
        assertEquals(1, results.size)
        assertEquals(5, results[0].articleId)
    }

    @Test
    fun search_uppercase_stillMatches() = runTest {
        val results = repository.search("ΣΥΝΤΑΓΜΑ")
        assertEquals(1, results.size)
        assertEquals(1, results[0].articleId)
    }

    @Test
    fun search_alreadyNormalized_stillMatches() = runTest {
        val results = repository.search("συνταγμα")
        assertEquals(1, results.size)
        assertEquals(1, results[0].articleId)
    }

    @Test
    fun search_empty_returnsNoResults() = runTest {
        val results = repository.search("")
        assertTrue(results.isEmpty())
    }

    @Test
    fun search_whitespace_returnsNoResults() = runTest {
        val results = repository.search("   ")
        assertTrue(results.isEmpty())
    }

    @Test
    fun search_nonexistent_returnsNoResults() = runTest {
        val results = repository.search("nonexistent")
        assertTrue(results.isEmpty())
    }

    @Test
    fun search_returnsOriginalContent() = runTest {
        val results = repository.search("σύνταγμα")
        assertEquals("Το Σύνταγμα της Ελλάδας", results[0].content)
    }
}
