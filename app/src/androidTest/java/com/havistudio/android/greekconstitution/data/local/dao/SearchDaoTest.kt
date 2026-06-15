package com.havistudio.android.greekconstitution.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.havistudio.android.greekconstitution.data.local.ConstitutionDatabase
import com.havistudio.android.greekconstitution.util.GreekTextNormalizer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Direct exercise of [SearchDao.search] independent of repository wrapping.
 * Complements [com.havistudio.android.greekconstitution.data.local.FtsCorrectnessTest]
 * which goes through the repository — these focus on DAO query binding,
 * ordering, and column projection.
 */
@RunWith(AndroidJUnit4::class)
class SearchDaoTest {

    private lateinit var db: ConstitutionDatabase
    private lateinit var dao: SearchDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ConstitutionDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.searchDao()

        val sdb = db.openHelper.writableDatabase

        sdb.insert(
            "parts", SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply { put("id", 1); put("`order`", 1); put("title", "Part 1") },
        )

        // Article 5 (later in `order`) and article 3 (earlier) — used to verify ordering.
        for ((id, num, order) in listOf(Triple(5, "5", 5), Triple(3, "3", 3))) {
            sdb.insert(
                "articles", SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", id); put("number", num); put("partId", 1); put("`order`", order)
                },
            )
        }

        // Two paragraphs in article 5 to test paragraph-level `order`.
        insertParagraph(sdb, id = 1, articleId = 5, number = "1", order = 1, content = "Πρώτη ελευθερία")
        insertParagraph(sdb, id = 2, articleId = 5, number = "2", order = 2, content = "Δεύτερη ελευθερία")
        insertParagraph(sdb, id = 3, articleId = 3, number = null, order = 1, content = "Άρθρο τρία ελευθερία")
        insertParagraph(sdb, id = 4, articleId = 3, number = null, order = 2, content = "Άσχετο περιεχόμενο")
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun search_singleHit_returnsExpectedColumns() = runTest {
        val results = dao.search(GreekTextNormalizer.normalize("τρία"))

        assertEquals(1, results.size)
        val hit = results[0]
        assertEquals(3, hit.paragraphId)
        assertEquals(3, hit.articleId)
        assertEquals("3", hit.articleNumber)
        assertEquals(null, hit.paragraphNumber)
        assertEquals("Άρθρο τρία ελευθερία", hit.content)
    }

    @Test
    fun search_multipleHits_orderedByArticleOrderThenParagraphOrder() = runTest {
        val results = dao.search(GreekTextNormalizer.normalize("ελευθερία"))

        // Article 3 (order=3) comes before article 5 (order=5);
        // within article 5, paragraph order 1 then 2.
        assertEquals(3, results.size)
        assertEquals(listOf(3, 5, 5), results.map { it.articleId })
        assertEquals(listOf(3, 1, 2), results.map { it.paragraphId })
    }

    @Test
    fun search_paragraphNumber_carriedThrough() = runTest {
        val results = dao.search(GreekTextNormalizer.normalize("πρώτη"))

        assertEquals(1, results.size)
        assertEquals("1", results[0].paragraphNumber)
    }

    @Test
    fun search_noMatch_returnsEmpty() = runTest {
        val results = dao.search(GreekTextNormalizer.normalize("ανύπαρκτο"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun search_normalizedQueryMatchesDiacriticInput() = runTest {
        // searchContent is stored without diacritics; the query passed in is
        // normalized too. Confirms FTS path doesn't depend on caller variants.
        val results = dao.search(GreekTextNormalizer.normalize("ΕΛΕΥΘΕΡΊΑ"))

        assertEquals(3, results.size)
    }

    private fun insertParagraph(
        sdb: androidx.sqlite.db.SupportSQLiteDatabase,
        id: Int,
        articleId: Int,
        number: String?,
        order: Int,
        content: String,
    ) {
        val search = GreekTextNormalizer.normalize(content)
        sdb.insert(
            "paragraphs", SQLiteDatabase.CONFLICT_REPLACE,
            ContentValues().apply {
                put("id", id); put("articleId", articleId)
                if (number == null) putNull("number") else put("number", number)
                put("`order`", order); put("content", content); put("searchContent", search)
            },
        )
        sdb.execSQL(
            "INSERT INTO paragraph_fts(rowid, searchContent) VALUES (?, ?)",
            arrayOf(id, search),
        )
    }
}
