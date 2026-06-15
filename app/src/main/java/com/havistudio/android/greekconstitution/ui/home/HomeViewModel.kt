package com.havistudio.android.greekconstitution.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Chapter
import com.havistudio.android.greekconstitution.data.local.entity.Part
import com.havistudio.android.greekconstitution.data.local.entity.Section
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TocNode(
    val id: String,
    val title: String,
    val subtitle: String?,
    val depth: Int,
    val articleId: Int? = null,
    val isExpanded: Boolean = false,
    val hasChildren: Boolean = true,
)

data class HomeUiState(
    val nodes: List<TocNode> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(private val repository: ConstitutionRepository) : ViewModel() {

    private val expanded = MutableStateFlow<Set<String>>(emptySet())
    private val sections = MutableStateFlow<List<Section>>(emptyList())
    private val chapters = MutableStateFlow<List<Chapter>>(emptyList())

    init {
        viewModelScope.launch {
            repository.observeAllSections().collect { sections.value = it }
        }
        viewModelScope.launch {
            repository.observeAllChapters().collect { chapters.value = it }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeParts(),
        repository.observeArticles(),
        sections,
        chapters,
        expanded,
    ) { parts, articles, allSections, allChapters, expandedIds ->
        HomeUiState(
            nodes = buildTocNodes(parts, allSections, allChapters, articles, expandedIds),
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun buildTocNodes(
        parts: List<Part>,
        allSections: List<Section>,
        allChapters: List<Chapter>,
        allArticles: List<Article>,
        expandedIds: Set<String>,
    ): List<TocNode> {
        val nodes = mutableListOf<TocNode>()
        for (part in parts) {
            val partKey = "part_${part.id}"
            val isPartExpanded = partKey in expandedIds
            val partSections = allSections.filter { it.partId == part.id }
            nodes.add(TocNode(partKey, part.title, part.subtitle, 0, isExpanded = isPartExpanded))
            if (!isPartExpanded) continue

            if (partSections.isEmpty()) {
                // Part 2 has articles directly (no sections)
                allArticles.filter { it.partId == part.id }
                    .forEach { addArticleNode(nodes, it, 1) }
            } else {
                for (section in partSections) {
                    val sKey = "section_${section.id}"
                    val isSectionExpanded = sKey in expandedIds
                    val sectionChapters = allChapters.filter { it.sectionId == section.id }
                    nodes.add(TocNode(sKey, section.title, section.subtitle, 1, isExpanded = isSectionExpanded, hasChildren = true))
                    if (!isSectionExpanded) continue

                    if (sectionChapters.isEmpty()) {
                        allArticles.filter { it.sectionId == section.id }
                            .forEach { addArticleNode(nodes, it, 2) }
                    } else {
                        for (chapter in sectionChapters) {
                            val cKey = "chapter_${chapter.id}"
                            val isChapterExpanded = cKey in expandedIds
                            nodes.add(TocNode(cKey, chapter.title, chapter.subtitle, 2, isExpanded = isChapterExpanded))
                            if (!isChapterExpanded) continue
                            allArticles.filter { it.chapterId == chapter.id }
                                .forEach { addArticleNode(nodes, it, 3) }
                        }
                    }
                }
            }
        }
        return nodes
    }

    private fun addArticleNode(nodes: MutableList<TocNode>, article: Article, depth: Int) {
        nodes.add(
            TocNode(
                id = "article_${article.id}",
                title = "Άρθρο ${article.number}",
                subtitle = article.title,
                depth = depth,
                articleId = article.id,
                hasChildren = false,
            ),
        )
    }

    fun toggleExpanded(nodeId: String) {
        expanded.value = expanded.value.toMutableSet().apply {
            if (contains(nodeId)) remove(nodeId) else add(nodeId)
        }
    }

    class Factory(private val repository: ConstitutionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}
