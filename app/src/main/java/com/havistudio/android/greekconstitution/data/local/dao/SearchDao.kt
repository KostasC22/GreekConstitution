package com.havistudio.android.greekconstitution.data.local.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SearchDao {

    /**
     * Full-text search over paragraph content. Caller must pass a
     * diacritic-stripped, lowercased query (use `GreekTextNormalizer.normalize`).
     * Returns the article the match belongs to, plus the matching paragraph's
     * original (non-normalized) content for display.
     */
    @Query(
        """
        SELECT
            p.id           AS paragraphId,
            p.articleId    AS articleId,
            a.number       AS articleNumber,
            p.number       AS paragraphNumber,
            p.content      AS content
        FROM paragraph_fts
        INNER JOIN paragraphs p ON p.id = paragraph_fts.rowid
        INNER JOIN articles a ON a.id = p.articleId
        WHERE paragraph_fts MATCH :normalizedQuery
        ORDER BY a.`order`, p.`order`
        LIMIT 200
        """
    )
    suspend fun search(normalizedQuery: String): List<SearchHit>

    data class SearchHit(
        val paragraphId: Int,
        val articleId: Int,
        val articleNumber: String,
        val paragraphNumber: String?,
        val content: String,
    )
}
