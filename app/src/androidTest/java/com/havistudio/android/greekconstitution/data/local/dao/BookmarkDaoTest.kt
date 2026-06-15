package com.havistudio.android.greekconstitution.data.local.dao

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.ConstitutionDatabase
import com.havistudio.android.greekconstitution.data.local.entity.Bookmark
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkDaoTest {

    private lateinit var db: ConstitutionDatabase
    private lateinit var dao: BookmarkDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ConstitutionDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.bookmarkDao()
        // Insert a part + articles so FK constraints are satisfied
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

    @Test
    fun add_and_observeIsBookmarked() = runTest {
        dao.add(Bookmark(articleId = 1, createdAt = 1000L))

        assertTrue(dao.observeIsBookmarked(1).first())
    }

    @Test
    fun observeIsBookmarked_returnsFalseWhenNotBookmarked() = runTest {
        assertFalse(dao.observeIsBookmarked(1).first())
    }

    @Test
    fun removeByArticle_unbookmarks() = runTest {
        dao.add(Bookmark(articleId = 1, createdAt = 1000L))
        dao.removeByArticle(1)

        assertFalse(dao.observeIsBookmarked(1).first())
    }

    @Test
    fun duplicate_insert_is_ignored() = runTest {
        dao.add(Bookmark(articleId = 1, createdAt = 1000L))
        val secondId = dao.add(Bookmark(articleId = 1, createdAt = 2000L))

        // IGNORE strategy returns -1 on conflict
        assertEquals(-1L, secondId)
        assertTrue(dao.observeIsBookmarked(1).first())
    }

    @Test
    fun observeBookmarkedArticles_returnsOnlyBookmarked() = runTest {
        dao.add(Bookmark(articleId = 1, createdAt = 1000L))

        val articles = dao.observeBookmarkedArticles().first()

        assertEquals(1, articles.size)
        assertEquals("1", articles[0].number)
    }

    @Test
    fun observeBookmarkedArticles_orderedByCreatedAtDesc() = runTest {
        dao.add(Bookmark(articleId = 1, createdAt = 1000L))
        dao.add(Bookmark(articleId = 2, createdAt = 2000L))

        val articles = dao.observeBookmarkedArticles().first()

        assertEquals(2, articles.size)
        assertEquals("2", articles[0].number) // newer first
        assertEquals("1", articles[1].number)
    }
}
