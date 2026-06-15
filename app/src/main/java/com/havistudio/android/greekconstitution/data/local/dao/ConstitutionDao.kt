package com.havistudio.android.greekconstitution.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Chapter
import com.havistudio.android.greekconstitution.data.local.entity.InterpretiveClause
import com.havistudio.android.greekconstitution.data.local.entity.Paragraph
import com.havistudio.android.greekconstitution.data.local.entity.Part
import com.havistudio.android.greekconstitution.data.local.entity.Section
import kotlinx.coroutines.flow.Flow

@Dao
interface ConstitutionDao {

    @Query("SELECT * FROM parts ORDER BY `order`")
    fun observeParts(): Flow<List<Part>>

    @Query("SELECT * FROM sections ORDER BY partId, `order`")
    fun observeAllSections(): Flow<List<Section>>

    @Query("SELECT * FROM sections WHERE partId = :partId ORDER BY `order`")
    fun observeSectionsInPart(partId: Int): Flow<List<Section>>

    @Query("SELECT * FROM chapters ORDER BY sectionId, `order`")
    fun observeAllChapters(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE sectionId = :sectionId ORDER BY `order`")
    fun observeChaptersInSection(sectionId: Int): Flow<List<Chapter>>

    @Query("SELECT * FROM articles ORDER BY `order`")
    fun observeArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE partId = :partId ORDER BY `order`")
    fun observeArticlesInPart(partId: Int): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE sectionId = :sectionId ORDER BY `order`")
    fun observeArticlesInSection(sectionId: Int): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE chapterId = :chapterId ORDER BY `order`")
    fun observeArticlesInChapter(chapterId: Int): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun observeArticle(id: Int): Flow<Article?>

    /**
     * Article that comes immediately after [currentId] in canonical reading
     * order (by `order`, ascending). Emits `null` when [currentId] is the
     * last article or doesn't exist.
     *
     * Uses `order` rather than `id` because article IDs are not guaranteed
     * to be contiguous — articles like "120Α" sit between "120" and "121"
     * in the printed text but won't have neighbouring numeric IDs.
     */
    @Query(
        """
        SELECT * FROM articles
        WHERE `order` > (SELECT `order` FROM articles WHERE id = :currentId)
        ORDER BY `order` ASC
        LIMIT 1
        """
    )
    fun observeNextArticle(currentId: Int): Flow<Article?>

    /** Symmetrical helper for a "Previous article" affordance. */
    @Query(
        """
        SELECT * FROM articles
        WHERE `order` < (SELECT `order` FROM articles WHERE id = :currentId)
        ORDER BY `order` DESC
        LIMIT 1
        """
    )
    fun observePreviousArticle(currentId: Int): Flow<Article?>

    @Query("SELECT * FROM parts WHERE id = :id")
    fun observePart(id: Int): Flow<Part?>

    @Query("SELECT * FROM paragraphs WHERE articleId = :articleId ORDER BY `order`")
    fun observeParagraphs(articleId: Int): Flow<List<Paragraph>>

    @Query("SELECT * FROM interpretive_clauses WHERE articleId = :articleId")
    fun observeInterpretiveClauses(articleId: Int): Flow<List<InterpretiveClause>>

    @Query("SELECT COUNT(*) FROM parts")
    suspend fun partCount(): Int
}
