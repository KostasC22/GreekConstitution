package com.havistudio.android.greekconstitution.data.local.dao

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.ConstitutionDatabase
import com.havistudio.android.greekconstitution.data.local.entity.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: ConstitutionDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ConstitutionDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.noteDao()
        // Satisfy FK constraints
        db.openHelper.writableDatabase.insert(
            "parts", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", 1); put("`order`", 1); put("title", "Part 1")
            },
        )
        db.openHelper.writableDatabase.insert(
            "articles", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", 1); put("number", "1"); put("partId", 1); put("`order`", 1)
            },
        )
        db.openHelper.writableDatabase.insert(
            "articles", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", 2); put("number", "2"); put("partId", 1); put("`order`", 2)
            },
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeNote(
        articleId: Int = 1, content: String = "Note", createdAt: Long = 1000L, updatedAt: Long = 1000L,
    ) = Note(articleId = articleId, paragraphId = null, content = content, createdAt = createdAt, updatedAt = updatedAt)

    @Test
    fun insert_and_observeNotesForArticle() = runTest {
        dao.insert(makeNote(content = "Test"))

        val notes = dao.observeNotesForArticle(1).first()

        assertEquals(1, notes.size)
        assertEquals("Test", notes[0].content)
    }

    @Test
    fun update_changesContent() = runTest {
        dao.insert(makeNote(content = "Old"))
        val note = dao.observeNotesForArticle(1).first().first()

        dao.update(note.copy(content = "New"))

        val updated = dao.observeNotesForArticle(1).first().first()
        assertEquals("New", updated.content)
    }

    @Test
    fun delete_removesNote() = runTest {
        dao.insert(makeNote())
        val note = dao.observeNotesForArticle(1).first().first()

        dao.delete(note)

        val notes = dao.observeNotesForArticle(1).first()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun notes_for_different_articles_dont_cross() = runTest {
        dao.insert(makeNote(articleId = 1, content = "A"))
        dao.insert(makeNote(articleId = 2, content = "B"))

        val notesForArticle1 = dao.observeNotesForArticle(1).first()
        val notesForArticle2 = dao.observeNotesForArticle(2).first()

        assertEquals(1, notesForArticle1.size)
        assertEquals("A", notesForArticle1[0].content)
        assertEquals(1, notesForArticle2.size)
        assertEquals("B", notesForArticle2[0].content)
    }

    @Test
    fun observeNotesForArticle_orderedByUpdatedAtDesc() = runTest {
        dao.insert(makeNote(content = "Older", updatedAt = 1000L))
        dao.insert(makeNote(content = "Newer", updatedAt = 2000L))

        val notes = dao.observeNotesForArticle(1).first()

        assertEquals("Newer", notes[0].content)
        assertEquals("Older", notes[1].content)
    }

    @Test
    fun observeAllNotes_returnsAllArticles() = runTest {
        dao.insert(makeNote(articleId = 1, content = "A"))
        dao.insert(makeNote(articleId = 2, content = "B"))

        val notes = dao.observeAllNotes().first()

        assertEquals(2, notes.size)
    }

    @Test
    fun observeLatestNotePerArticle_pickslatestPerArticle() = runTest {
        // article 1: two notes, "newer" should win
        dao.insert(makeNote(articleId = 1, content = "old1", updatedAt = 1000L))
        dao.insert(makeNote(articleId = 1, content = "new1", updatedAt = 2000L))
        // article 2: one note
        dao.insert(makeNote(articleId = 2, content = "only2", updatedAt = 1500L))

        val rows = dao.observeLatestNotePerArticle().first()

        assertEquals(2, rows.size)
        val byArticle = rows.associateBy { it.articleId }
        assertEquals("new1", byArticle[1]?.content)
        assertEquals("only2", byArticle[2]?.content)
    }

    @Test
    fun observeLatestNotePerArticle_emptyDb_returnsEmpty() = runTest {
        val rows = dao.observeLatestNotePerArticle().first()
        assertTrue(rows.isEmpty())
    }

    @Test
    fun observeLatestNotePerArticle_orderedByArticleId() = runTest {
        // Insert article 2 first to make sure the ORDER BY articleId is the
        // observable behaviour, not insert order.
        dao.insert(makeNote(articleId = 2, content = "b"))
        dao.insert(makeNote(articleId = 1, content = "a"))

        val rows = dao.observeLatestNotePerArticle().first()

        assertEquals(listOf(1, 2), rows.map { it.articleId })
    }
}
