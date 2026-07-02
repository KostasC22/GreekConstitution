package com.havistudio.android.greekconstitution.data.repository

import com.havistudio.android.greekconstitution.data.local.dao.BookmarkDao
import com.havistudio.android.greekconstitution.data.local.dao.ConstitutionDao
import com.havistudio.android.greekconstitution.data.local.dao.NoteDao
import com.havistudio.android.greekconstitution.data.local.dao.SearchDao
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Bookmark
import com.havistudio.android.greekconstitution.data.local.entity.Chapter
import com.havistudio.android.greekconstitution.data.local.entity.InterpretiveClause
import com.havistudio.android.greekconstitution.data.local.entity.Note
import com.havistudio.android.greekconstitution.data.local.entity.Paragraph
import com.havistudio.android.greekconstitution.data.local.entity.Part
import com.havistudio.android.greekconstitution.data.local.entity.Section
import com.havistudio.android.greekconstitution.util.GreekTextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ConstitutionRepository(
    private val constitutionDao: ConstitutionDao,
    private val bookmarkDao: BookmarkDao,
    private val noteDao: NoteDao,
    private val searchDao: SearchDao,
) {
    // ---- Structure --------------------------------------------------------

    fun observeParts(): Flow<List<Part>> = constitutionDao.observeParts()

    fun observeAllSections(): Flow<List<Section>> = constitutionDao.observeAllSections()

    fun observeAllChapters(): Flow<List<Chapter>> = constitutionDao.observeAllChapters()

    fun observeSectionsInPart(partId: Int): Flow<List<Section>> =
        constitutionDao.observeSectionsInPart(partId)

    fun observeChaptersInSection(sectionId: Int): Flow<List<Chapter>> =
        constitutionDao.observeChaptersInSection(sectionId)

    fun observeArticles(): Flow<List<Article>> = constitutionDao.observeArticles()

    fun observeArticlesInPart(partId: Int): Flow<List<Article>> =
        constitutionDao.observeArticlesInPart(partId)

    fun observeArticlesInSection(sectionId: Int): Flow<List<Article>> =
        constitutionDao.observeArticlesInSection(sectionId)

    fun observeArticlesInChapter(chapterId: Int): Flow<List<Article>> =
        constitutionDao.observeArticlesInChapter(chapterId)

    fun observeArticle(id: Int): Flow<Article?> = constitutionDao.observeArticle(id)

    /** Next article in canonical reading order (by `order`). */
    fun observeNextArticle(currentId: Int): Flow<Article?> =
        constitutionDao.observeNextArticle(currentId)

    /** Previous article in canonical reading order — for symmetry. */
    fun observePreviousArticle(currentId: Int): Flow<Article?> =
        constitutionDao.observePreviousArticle(currentId)

    fun observePart(id: Int): Flow<Part?> = constitutionDao.observePart(id)

    fun observeParagraphs(articleId: Int): Flow<List<Paragraph>> =
        constitutionDao.observeParagraphs(articleId)

    fun observeInterpretiveClauses(articleId: Int): Flow<List<InterpretiveClause>> =
        constitutionDao.observeInterpretiveClauses(articleId)

    // ---- Search -----------------------------------------------------------

    suspend fun search(rawQuery: String): List<SearchDao.SearchHit> {
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) return emptyList()
        val normalized = GreekTextNormalizer.normalize(trimmed)
        // Escape double-quotes (FTS special char) and append * for prefix match
        val escaped = normalized.replace("\"", "\"\"")
        val ftsQuery = "\"$escaped\"*"
        return searchDao.search(ftsQuery)
    }

    // ---- Bookmarks --------------------------------------------------------

    fun observeBookmarkedArticles(): Flow<List<Article>> =
        bookmarkDao.observeBookmarkedArticles()

    fun observeIsBookmarked(articleId: Int): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(articleId)

    suspend fun toggleBookmark(articleId: Int) {
        val removed = bookmarkDao.removeByArticle(articleId)
        if (removed == 0) {
            bookmarkDao.add(Bookmark(articleId = articleId, createdAt = System.currentTimeMillis()))
        }
    }

    suspend fun addBookmark(articleId: Int) {
        bookmarkDao.add(Bookmark(articleId = articleId, createdAt = System.currentTimeMillis()))
    }

    suspend fun removeBookmark(articleId: Int) {
        bookmarkDao.removeByArticle(articleId)
    }

    // ---- Notes ------------------------------------------------------------

    fun observeNotesForArticle(articleId: Int): Flow<List<Note>> =
        noteDao.observeNotesForArticle(articleId)

    fun observeAllNotes(): Flow<List<Note>> = noteDao.observeAllNotes()

    fun observeLatestNotePerArticle(): Flow<Map<Int, Note>> =
        noteDao.observeLatestNotePerArticle().map { list ->
            list.associateBy { it.articleId }
        }

    fun observeBookmarkedArticlesWithNotes(): Flow<List<Pair<Article, Note?>>> =
        combine(
            observeBookmarkedArticles(),
            observeLatestNotePerArticle(),
        ) { articles, notesByArticle ->
            articles.map { it to notesByArticle[it.id] }
        }

    suspend fun addNote(articleId: Int, paragraphId: Int? = null, content: String): Long {
        val now = System.currentTimeMillis()
        return noteDao.insert(
            Note(
                articleId = articleId,
                paragraphId = paragraphId,
                content = content,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateNote(note: Note) {
        noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }
}
