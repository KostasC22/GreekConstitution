package com.havistudio.android.greekconstitution.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SchemaValidationTest {

    private lateinit var db: ConstitutionDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ConstitutionDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun database_opens_without_crash() {
        // Force the database to open by executing a query
        db.openHelper.readableDatabase
        assertTrue(db.isOpen)
    }

    @Test
    fun all_expected_tables_exist() = runTest {
        val expectedTables = setOf(
            "parts", "sections", "chapters", "articles",
            "paragraphs", "paragraph_fts", "interpretive_clauses",
            "bookmarks", "notes",
        )

        val cursor = db.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'room_%' AND name NOT LIKE 'android_%'",
        )

        val actualTables = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            actualTables.add(cursor.getString(0))
        }
        cursor.close()

        // FTS creates shadow tables (paragraph_fts_content, etc.) — filter to our expected set
        for (table in expectedTables) {
            assertTrue(
                "Missing table: $table. Found: $actualTables",
                actualTables.contains(table),
            )
        }
    }

    @Test
    fun daos_are_accessible() {
        // Verify Room generated all DAO implementations
        db.constitutionDao()
        db.bookmarkDao()
        db.noteDao()
        db.searchDao()
    }
}
