package com.havistudio.android.greekconstitution.data.repository

import com.havistudio.android.greekconstitution.data.local.dao.BookmarkDao
import com.havistudio.android.greekconstitution.data.local.dao.ConstitutionDao
import com.havistudio.android.greekconstitution.data.local.dao.NoteDao
import com.havistudio.android.greekconstitution.data.local.dao.SearchDao
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Bookmark
import com.havistudio.android.greekconstitution.data.local.entity.Note
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConstitutionRepositoryTest {

    private val constitutionDao: ConstitutionDao = mockk()
    private val bookmarkDao: BookmarkDao = mockk()
    private val noteDao: NoteDao = mockk()
    private val searchDao: SearchDao = mockk()

    private lateinit var repository: ConstitutionRepository

    @Before
    fun setup() {
        repository = ConstitutionRepository(
            constitutionDao = constitutionDao,
            bookmarkDao = bookmarkDao,
            noteDao = noteDao,
            searchDao = searchDao,
        )
    }

    // ---- Search -----------------------------------------------------------

    @Test
    fun `search normalizes Greek query before delegating`() = runTest {
        val querySlot = slot<String>()
        coEvery { searchDao.search(capture(querySlot)) } returns emptyList()

        repository.search("Δικαιώματα")

        assertTrue(querySlot.captured.contains("δικαιωματα"))
    }

    @Test
    fun `search with empty string returns empty without calling DAO`() = runTest {
        val result = repository.search("")
        assertEquals(emptyList<SearchDao.SearchHit>(), result)
    }

    @Test
    fun `search with blank string returns empty without calling DAO`() = runTest {
        val result = repository.search("   ")
        assertEquals(emptyList<SearchDao.SearchHit>(), result)
    }

    @Test
    fun `search wraps query in FTS prefix match syntax`() = runTest {
        val querySlot = slot<String>()
        coEvery { searchDao.search(capture(querySlot)) } returns emptyList()

        repository.search("βουλή")

        assertEquals("\"βουλη\"*", querySlot.captured)
    }

    @Test
    fun `search escapes double quotes in query`() = runTest {
        val querySlot = slot<String>()
        coEvery { searchDao.search(capture(querySlot)) } returns emptyList()

        repository.search("test\"value")

        assertTrue(querySlot.captured.contains("\"\""))
    }

    // ---- Bookmarks --------------------------------------------------------

    @Test
    fun `addBookmark inserts bookmark with correct articleId`() = runTest {
        val bookmarkSlot = slot<Bookmark>()
        coEvery { bookmarkDao.add(capture(bookmarkSlot)) } returns 1L

        repository.addBookmark(42)

        assertEquals(42, bookmarkSlot.captured.articleId)
        assertTrue(bookmarkSlot.captured.createdAt > 0)
    }

    @Test
    fun `removeBookmark delegates to DAO`() = runTest {
        coEvery { bookmarkDao.removeByArticle(42) } returns 1

        repository.removeBookmark(42)

        coVerify { bookmarkDao.removeByArticle(42) }
    }

    @Test
    fun `toggleBookmark on bookmarked article removes it without re-adding`() = runTest {
        // DAO reports one row deleted — the article was bookmarked.
        coEvery { bookmarkDao.removeByArticle(42) } returns 1

        repository.toggleBookmark(42)

        coVerify(exactly = 1) { bookmarkDao.removeByArticle(42) }
        coVerify(exactly = 0) { bookmarkDao.add(any()) }
    }

    @Test
    fun `toggleBookmark on non-bookmarked article adds it`() = runTest {
        // DAO reports zero rows deleted — the article was not bookmarked.
        coEvery { bookmarkDao.removeByArticle(42) } returns 0
        val bookmarkSlot = slot<Bookmark>()
        coEvery { bookmarkDao.add(capture(bookmarkSlot)) } returns 1L

        repository.toggleBookmark(42)

        assertEquals(42, bookmarkSlot.captured.articleId)
    }

    @Test
    fun `observeBookmarkedArticles delegates to DAO`() {
        every { bookmarkDao.observeBookmarkedArticles() } returns flowOf(emptyList())

        val flow = repository.observeBookmarkedArticles()

        assertEquals(bookmarkDao.observeBookmarkedArticles(), flow)
    }

    // ---- Notes ------------------------------------------------------------

    @Test
    fun `addNote inserts note with timestamps`() = runTest {
        val noteSlot = slot<Note>()
        coEvery { noteDao.insert(capture(noteSlot)) } returns 1L

        repository.addNote(articleId = 1, content = "Test note")

        val captured = noteSlot.captured
        assertEquals(1, captured.articleId)
        assertEquals("Test note", captured.content)
        assertTrue(captured.createdAt > 0)
        assertEquals(captured.createdAt, captured.updatedAt)
    }

    @Test
    fun `updateNote bumps updatedAt`() = runTest {
        val noteSlot = slot<Note>()
        coEvery { noteDao.update(capture(noteSlot)) } returns Unit

        val original = Note(
            id = 1, articleId = 1, paragraphId = null,
            content = "old", createdAt = 1000L, updatedAt = 1000L,
        )
        repository.updateNote(original.copy(content = "new"))

        val captured = noteSlot.captured
        assertEquals("new", captured.content)
        assertTrue(captured.updatedAt > 1000L)
    }

    @Test
    fun `deleteNote delegates to DAO`() = runTest {
        val note = Note(
            id = 1, articleId = 1, paragraphId = null,
            content = "x", createdAt = 1000L, updatedAt = 1000L,
        )
        coEvery { noteDao.delete(note) } returns Unit

        repository.deleteNote(note)

        coVerify { noteDao.delete(note) }
    }

    // ---- Flow delegation --------------------------------------------------

    @Test
    fun `observeParts delegates to constitutionDao`() {
        every { constitutionDao.observeParts() } returns flowOf(emptyList())
        val flow = repository.observeParts()
        assertEquals(constitutionDao.observeParts(), flow)
    }

    @Test
    fun `observeNotesForArticle delegates to noteDao`() {
        every { noteDao.observeNotesForArticle(1) } returns flowOf(emptyList())
        val flow = repository.observeNotesForArticle(1)
        assertEquals(noteDao.observeNotesForArticle(1), flow)
    }

    // ---- Latest-note projection + bookmarks-with-notes --------------------

    private fun note(id: Long, articleId: Int, content: String = "n") = Note(
        id = id, articleId = articleId, paragraphId = null, content = content,
        createdAt = 1000L, updatedAt = 1000L,
    )

    private fun article(id: Int, number: String = id.toString()) = Article(
        id = id, number = number, partId = 1, sectionId = null, chapterId = null,
        order = id, title = null,
    )

    @Test
    fun `observeLatestNotePerArticle projects DAO list into Map keyed by articleId`() = runTest {
        every { noteDao.observeLatestNotePerArticle() } returns flowOf(
            listOf(note(1, articleId = 5), note(2, articleId = 7)),
        )

        val map = repository.observeLatestNotePerArticle().first()

        assertEquals(2, map.size)
        assertEquals(1L, map[5]?.id)
        assertEquals(7, map[7]?.articleId)
    }

    @Test
    fun `observeLatestNotePerArticle on empty DAO emits empty Map`() = runTest {
        every { noteDao.observeLatestNotePerArticle() } returns flowOf(emptyList())

        val map = repository.observeLatestNotePerArticle().first()

        assertTrue(map.isEmpty())
    }

    @Test
    fun `observeBookmarkedArticlesWithNotes pairs each article with its latest note`() = runTest {
        every { bookmarkDao.observeBookmarkedArticles() } returns flowOf(
            listOf(article(5), article(7)),
        )
        every { noteDao.observeLatestNotePerArticle() } returns flowOf(
            listOf(note(11, articleId = 5, content = "five")),
        )

        val result = repository.observeBookmarkedArticlesWithNotes().first()

        assertEquals(2, result.size)
        assertEquals(5, result[0].first.id)
        assertEquals("five", result[0].second?.content)
        // Article 7 has no note — second of the pair must be null.
        assertEquals(7, result[1].first.id)
        assertEquals(null, result[1].second)
    }

    @Test
    fun `observeBookmarkedArticlesWithNotes empty bookmarks emits empty list`() = runTest {
        every { bookmarkDao.observeBookmarkedArticles() } returns flowOf(emptyList())
        every { noteDao.observeLatestNotePerArticle() } returns flowOf(emptyList())

        val result = repository.observeBookmarkedArticlesWithNotes().first()

        assertTrue(result.isEmpty())
    }
}
