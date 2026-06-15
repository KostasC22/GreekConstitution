package com.havistudio.android.greekconstitution.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.util.GreekTextNormalizer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConstitutionPrepopulatorTest {

    private lateinit var context: Context
    private lateinit var db: ConstitutionDatabase
    private lateinit var prepopulator: ConstitutionPrepopulator

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ConstitutionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // Force schema creation before populate runs.
        db.openHelper.writableDatabase
        prepopulator = ConstitutionPrepopulator(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun populate_inserts_parts_articles_and_paragraphs() = runTest {
        prepopulator.populate(db.openHelper.writableDatabase)

        val partsCount = countRows("parts")
        val articlesCount = countRows("articles")
        val paragraphsCount = countRows("paragraphs")

        assertTrue("Parts not seeded ($partsCount)", partsCount > 0)
        assertTrue("Articles not seeded ($articlesCount)", articlesCount > 0)
        assertTrue("Paragraphs not seeded ($paragraphsCount)", paragraphsCount > 0)
    }

    @Test
    fun populate_is_idempotent_when_run_twice() = runTest {
        prepopulator.populate(db.openHelper.writableDatabase)
        val firstArticles = countRows("articles")
        val firstParagraphs = countRows("paragraphs")

        prepopulator.populate(db.openHelper.writableDatabase)

        // CONFLICT_REPLACE on stable IDs → counts unchanged after a re-run.
        assertEquals(firstArticles, countRows("articles"))
        assertEquals(firstParagraphs, countRows("paragraphs"))
    }

    @Test
    fun populate_writes_FTS_rows_for_every_paragraph() = runTest {
        prepopulator.populate(db.openHelper.writableDatabase)

        val paragraphCount = countRows("paragraphs")
        val ftsCount = countRows("paragraph_fts")

        assertEquals(paragraphCount, ftsCount)
    }

    @Test
    fun populate_makes_search_findable_via_searchDao() = runTest {
        prepopulator.populate(db.openHelper.writableDatabase)

        // "σύνταγμα" should appear somewhere in the seeded text.
        val results = db.searchDao().search(GreekTextNormalizer.normalize("σύνταγμα"))

        assertTrue("Expected at least one search hit for 'σύνταγμα'", results.isNotEmpty())
    }

    private fun countRows(table: String): Int {
        val cursor = db.openHelper.writableDatabase.query("SELECT COUNT(*) FROM `$table`")
        return cursor.use { c ->
            c.moveToFirst()
            c.getInt(0)
        }
    }
}
