package com.havistudio.android.greekconstitution.data.local.dao

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.ConstitutionDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConstitutionDaoTest {

    private lateinit var db: ConstitutionDatabase
    private lateinit var dao: ConstitutionDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ConstitutionDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.constitutionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun insertPart(id: Int = 1, order: Int = 1, title: String = "Part $id") {
        db.openHelper.writableDatabase.insert(
            "parts", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", id); put("`order`", order); put("title", title)
            },
        )
    }

    private fun insertSection(id: Int = 1, partId: Int = 1, order: Int = 1, title: String = "Section $id") {
        db.openHelper.writableDatabase.insert(
            "sections", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", id); put("partId", partId); put("`order`", order); put("title", title)
            },
        )
    }

    private fun insertChapter(id: Int = 1, sectionId: Int = 1, order: Int = 1, title: String = "Chapter $id") {
        db.openHelper.writableDatabase.insert(
            "chapters", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", id); put("sectionId", sectionId); put("`order`", order); put("title", title)
            },
        )
    }

    private fun insertArticle(
        id: Int = 1, number: String = "1", partId: Int = 1,
        sectionId: Int? = null, chapterId: Int? = null, order: Int = 1,
    ) {
        db.openHelper.writableDatabase.insert(
            "articles", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", id); put("number", number); put("partId", partId)
                if (sectionId != null) put("sectionId", sectionId) else putNull("sectionId")
                if (chapterId != null) put("chapterId", chapterId) else putNull("chapterId")
                put("`order`", order)
            },
        )
    }

    private fun insertParagraph(
        id: Int = 1, articleId: Int = 1, number: String? = "1",
        order: Int = 1, content: String = "Content", searchContent: String = "content",
    ) {
        db.openHelper.writableDatabase.insert(
            "paragraphs", SQLiteDatabase.CONFLICT_REPLACE,
            android.content.ContentValues().apply {
                put("id", id); put("articleId", articleId)
                if (number != null) put("number", number) else putNull("number")
                put("`order`", order); put("content", content); put("searchContent", searchContent)
            },
        )
    }

    @Test
    fun observeParts_returnsInsertedParts() = runTest {
        insertPart(1, 2, "Part B")
        insertPart(2, 1, "Part A")

        val parts = dao.observeParts().first()

        assertEquals(2, parts.size)
        assertEquals("Part A", parts[0].title) // ordered by `order`
        assertEquals("Part B", parts[1].title)
    }

    @Test
    fun observeSectionsInPart_filtersCorrectly() = runTest {
        insertPart(1); insertPart(2)
        insertSection(1, partId = 1, title = "S1")
        insertSection(2, partId = 2, title = "S2")

        val sections = dao.observeSectionsInPart(1).first()

        assertEquals(1, sections.size)
        assertEquals("S1", sections[0].title)
    }

    @Test
    fun observeArticle_returnsNullForNonexistent() = runTest {
        val article = dao.observeArticle(999).first()
        assertNull(article)
    }

    @Test
    fun observeParagraphs_returnsOrderedByOrder() = runTest {
        insertPart()
        insertArticle(1)
        insertParagraph(1, articleId = 1, order = 2, content = "Second")
        insertParagraph(2, articleId = 1, order = 1, content = "First")

        val paragraphs = dao.observeParagraphs(1).first()

        assertEquals(2, paragraphs.size)
        assertEquals("First", paragraphs[0].content)
        assertEquals("Second", paragraphs[1].content)
    }

    @Test
    fun observeArticlesInChapter_filtersCorrectly() = runTest {
        insertPart()
        insertSection()
        insertChapter(1)
        insertChapter(2, sectionId = 1, order = 2)
        insertArticle(1, chapterId = 1, sectionId = 1)
        insertArticle(2, number = "2", chapterId = 2, sectionId = 1, order = 2)

        val articles = dao.observeArticlesInChapter(1).first()

        assertEquals(1, articles.size)
        assertEquals("1", articles[0].number)
    }

    @Test
    fun observeNextArticle_returnsNextByCanonicalOrder() = runTest {
        insertPart()
        insertArticle(id = 10, number = "10", order = 1)
        insertArticle(id = 11, number = "11", order = 2)
        insertArticle(id = 12, number = "12", order = 3)

        val next = dao.observeNextArticle(currentId = 11).first()

        assertEquals(12, next?.id)
    }

    @Test
    fun observeNextArticle_lastArticle_returnsNull() = runTest {
        insertPart()
        insertArticle(id = 10, number = "10", order = 1)

        assertNull(dao.observeNextArticle(currentId = 10).first())
    }

    @Test
    fun observePreviousArticle_returnsPriorByCanonicalOrder() = runTest {
        insertPart()
        insertArticle(id = 10, number = "10", order = 1)
        insertArticle(id = 11, number = "11", order = 2)

        val prev = dao.observePreviousArticle(currentId = 11).first()

        assertEquals(10, prev?.id)
    }

    @Test
    fun observePreviousArticle_firstArticle_returnsNull() = runTest {
        insertPart()
        insertArticle(id = 10, number = "10", order = 1)

        assertNull(dao.observePreviousArticle(currentId = 10).first())
    }

    @Test
    fun observePart_returnsPartById() = runTest {
        insertPart(id = 7, title = "Έβδομο Μέρος")

        val part = dao.observePart(7).first()

        assertEquals("Έβδομο Μέρος", part?.title)
    }

    @Test
    fun observePart_unknownId_returnsNull() = runTest {
        assertNull(dao.observePart(999).first())
    }

    @Test
    fun partCount_reflectsRowCount() = runTest {
        assertEquals(0, dao.partCount())
        insertPart(id = 1); insertPart(id = 2); insertPart(id = 3)
        assertEquals(3, dao.partCount())
    }

    @Test
    fun observeArticles_returnsAllByCanonicalOrder() = runTest {
        insertPart()
        insertArticle(id = 2, number = "2", order = 2)
        insertArticle(id = 1, number = "1", order = 1)
        insertArticle(id = 3, number = "3", order = 3)

        val articles = dao.observeArticles().first()

        assertEquals(listOf("1", "2", "3"), articles.map { it.number })
    }

    @Test
    fun observeAllSections_groupsByPartIdThenOrder() = runTest {
        insertPart(id = 1); insertPart(id = 2)
        insertSection(id = 1, partId = 2, order = 1, title = "P2-S1")
        insertSection(id = 2, partId = 1, order = 2, title = "P1-S2")
        insertSection(id = 3, partId = 1, order = 1, title = "P1-S1")

        val sections = dao.observeAllSections().first()

        assertEquals(listOf("P1-S1", "P1-S2", "P2-S1"), sections.map { it.title })
    }
}
