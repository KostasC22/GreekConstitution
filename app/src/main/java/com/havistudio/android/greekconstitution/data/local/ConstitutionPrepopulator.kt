package com.havistudio.android.greekconstitution.data.local

import android.content.ContentValues
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.havistudio.android.greekconstitution.util.GreekTextNormalizer
import org.json.JSONObject

/**
 * Loads `assets/constitution.json` on first database creation and inserts it
 * into the raw tables via [SupportSQLiteDatabase]. Uses raw inserts (rather
 * than DAOs) because the database callback runs before DAOs are available,
 * and we need to write the FTS shadow table alongside the parent row.
 */
internal class ConstitutionPrepopulator(private val context: Context) {

    fun populate(db: SupportSQLiteDatabase) {
        val text = context.assets.open("constitution.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)

        db.beginTransaction()
        try {
            insertParts(db, root)
            insertSections(db, root)
            insertChapters(db, root)
            insertArticles(db, root)
            insertParagraphsAndFts(db, root)
            insertInterpretiveClauses(db, root)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertParts(db: SupportSQLiteDatabase, root: JSONObject) {
        val arr = root.getJSONArray("parts")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            db.insert(
                "parts",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", o.getInt("id"))
                    put("`order`", o.getInt("order"))
                    put("title", o.getString("title"))
                    put("subtitle", o.optStringOrNull("subtitle"))
                },
            )
        }
    }

    private fun insertSections(db: SupportSQLiteDatabase, root: JSONObject) {
        val arr = root.getJSONArray("sections")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            db.insert(
                "sections",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", o.getInt("id"))
                    put("partId", o.getInt("part_id"))
                    put("`order`", o.getInt("order"))
                    put("title", o.getString("title"))
                    put("subtitle", o.optStringOrNull("subtitle"))
                },
            )
        }
    }

    private fun insertChapters(db: SupportSQLiteDatabase, root: JSONObject) {
        val arr = root.getJSONArray("chapters")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            db.insert(
                "chapters",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", o.getInt("id"))
                    put("sectionId", o.getInt("section_id"))
                    put("`order`", o.getInt("order"))
                    put("title", o.getString("title"))
                    put("subtitle", o.optStringOrNull("subtitle"))
                },
            )
        }
    }

    private fun insertArticles(db: SupportSQLiteDatabase, root: JSONObject) {
        val arr = root.getJSONArray("articles")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            db.insert(
                "articles",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", o.getInt("id"))
                    put("number", o.getString("number"))
                    put("partId", o.getInt("part_id"))
                    put("sectionId", o.optIntOrNull("section_id"))
                    put("chapterId", o.optIntOrNull("chapter_id"))
                    put("`order`", o.getInt("order"))
                    put("title", o.optStringOrNull("title"))
                },
            )
        }
    }

    private fun insertParagraphsAndFts(db: SupportSQLiteDatabase, root: JSONObject) {
        val arr = root.getJSONArray("paragraphs")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getInt("id")
            val content = o.getString("content")
            val search = GreekTextNormalizer.normalize(content)
            // Optional `refs` array on each paragraph in constitution.json,
            // serialized to the same comma-separated TEXT format `Converters`
            // produces — keeps prepopulator + Room round-trips identical and
            // means existing JSON without a `refs` key keeps working.
            val refs = o.optJSONArray("refs")
            val refsCsv = if (refs == null) "" else buildString {
                for (j in 0 until refs.length()) {
                    if (j > 0) append(',')
                    append(refs.getInt(j))
                }
            }
            db.insert(
                "paragraphs",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", id)
                    put("articleId", o.getInt("article_id"))
                    put("number", o.optStringOrNull("number"))
                    put("`order`", o.getInt("order"))
                    put("content", content)
                    put("searchContent", search)
                    put("refs", refsCsv)
                },
            )
            db.insert(
                "paragraph_fts",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("rowid", id)
                    put("searchContent", search)
                },
            )
        }
    }

    private fun insertInterpretiveClauses(db: SupportSQLiteDatabase, root: JSONObject) {
        val arr = root.getJSONArray("interpretiveClauses")
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val content = o.getString("content")
            db.insert(
                "interpretive_clauses",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                ContentValues().apply {
                    put("id", o.getInt("id"))
                    put("articleId", o.getInt("article_id"))
                    put("content", content)
                    put("searchContent", GreekTextNormalizer.normalize(content))
                },
            )
        }
    }
}

private fun JSONObject.optStringOrNull(key: String): String? =
    if (isNull(key)) null else optString(key, "")

private fun JSONObject.optIntOrNull(key: String): Int? =
    if (isNull(key)) null else optInt(key)
