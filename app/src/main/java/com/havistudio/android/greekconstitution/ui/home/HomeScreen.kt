package com.havistudio.android.greekconstitution.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ConstitutionRepository,
    onArticleClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository)),
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(strings.appTitle) })
        },
    ) { padding ->
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(state.nodes, key = { it.id }) { node ->
                    TocItem(
                        node = node,
                        onToggle = { viewModel.toggleExpanded(node.id) },
                        onArticleClick = { node.articleId?.let(onArticleClick) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun TocItem(
    node: TocNode,
    onToggle: () -> Unit,
    onArticleClick: () -> Unit,
) {
    val startPadding = (16 + node.depth * 24).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (node.hasChildren) onToggle() else onArticleClick()
            }
            .padding(start = startPadding, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.title,
                style = when (node.depth) {
                    0 -> MaterialTheme.typography.titleMedium
                    1 -> MaterialTheme.typography.titleSmall
                    else -> MaterialTheme.typography.bodyLarge
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (node.subtitle != null) {
                Text(
                    text = node.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (node.hasChildren) {
            val s = LocalAppStrings.current
            Icon(
                imageVector = if (node.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (node.isExpanded) s.collapse else s.expand,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
