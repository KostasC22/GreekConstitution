package com.havistudio.android.greekconstitution.ui.home

import app.cash.turbine.test
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Chapter
import com.havistudio.android.greekconstitution.data.local.entity.Part
import com.havistudio.android.greekconstitution.data.local.entity.Section
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ConstitutionRepository = mockk()

    private val testPart = Part(id = 1, order = 1, title = "ΜΕΡΟΣ ΠΡΩΤΟ", subtitle = null)
    private val testSection = Section(id = 1, partId = 1, order = 1, title = "ΤΜΗΜΑ Α΄", subtitle = null)
    private val testChapter = Chapter(id = 1, sectionId = 1, order = 1, title = "ΚΕΦΑΛΑΙΟ Α΄", subtitle = null)
    private val testArticle = Article(
        id = 1, number = "1", partId = 1, sectionId = 1, chapterId = 1, order = 1, title = null,
    )
    private val directArticle = Article(
        id = 2, number = "2", partId = 1, sectionId = null, chapterId = null, order = 2, title = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubRepository(
        parts: List<Part> = listOf(testPart),
        sections: List<Section> = listOf(testSection),
        chapters: List<Chapter> = listOf(testChapter),
        articles: List<Article> = listOf(testArticle),
    ) {
        every { repository.observeParts() } returns flowOf(parts)
        every { repository.observeAllSections() } returns flowOf(sections)
        every { repository.observeAllChapters() } returns flowOf(chapters)
        every { repository.observeArticles() } returns flowOf(articles)
    }

    @Test
    fun `initial state transitions from loading to populated`() = runTest {
        stubRepository()

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.nodes.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `parts appear as top-level collapsed nodes`() = runTest {
        stubRepository()

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            val partNode = state.nodes.first()
            assertEquals("part_1", partNode.id)
            assertEquals("ΜΕΡΟΣ ΠΡΩΤΟ", partNode.title)
            assertEquals(0, partNode.depth)
            assertFalse(partNode.isExpanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `collapsed part hides children`() = runTest {
        stubRepository()

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.nodes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `expanding part reveals sections`() = runTest {
        stubRepository()

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // initial collapsed

            viewModel.toggleExpanded("part_1")
            val state = awaitItem()

            val sectionNode = state.nodes.find { it.id == "section_1" }
            assertEquals("ΤΜΗΜΑ Α΄", sectionNode?.title)
            assertEquals(1, sectionNode?.depth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `full expansion shows part, section, chapter, article`() = runTest {
        stubRepository()

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.toggleExpanded("part_1")
            awaitItem()
            viewModel.toggleExpanded("section_1")
            awaitItem()
            viewModel.toggleExpanded("chapter_1")
            val state = awaitItem()

            val ids = state.nodes.map { it.id }
            assertEquals(listOf("part_1", "section_1", "chapter_1", "article_1"), ids)

            val articleNode = state.nodes.last()
            assertEquals("Άρθρο 1", articleNode.title)
            assertEquals(3, articleNode.depth)
            assertEquals(1, articleNode.articleId)
            assertFalse(articleNode.hasChildren)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle collapse removes children`() = runTest {
        stubRepository()

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.toggleExpanded("part_1")
            awaitItem()

            viewModel.toggleExpanded("part_1") // collapse
            val state = awaitItem()

            assertEquals(1, state.nodes.size)
            assertEquals("part_1", state.nodes[0].id)
            assertFalse(state.nodes[0].isExpanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `articles without section attach directly under part`() = runTest {
        stubRepository(
            sections = emptyList(),
            chapters = emptyList(),
            articles = listOf(directArticle),
        )

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.toggleExpanded("part_1")
            val state = awaitItem()

            val articleNode = state.nodes.find { it.id == "article_2" }
            assertEquals("Άρθρο 2", articleNode?.title)
            assertEquals(1, articleNode?.depth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty parts list produces empty nodes`() = runTest {
        stubRepository(parts = emptyList(), sections = emptyList(), chapters = emptyList(), articles = emptyList())

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.nodes.isEmpty())
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
