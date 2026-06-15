package com.havistudio.android.greekconstitution.ui.article

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.havistudio.android.greekconstitution.data.local.entity.InterpretiveClause
import com.havistudio.android.greekconstitution.data.local.entity.Note
import com.havistudio.android.greekconstitution.data.local.entity.Paragraph
import com.havistudio.android.greekconstitution.data.repository.ConstitutionRepository
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalReadingFontScale
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Detail view for a single Article.
 *
 * Design: Variant A · "Strict M3 polish" (see `Article Detail Designs.html`).
 * — Hanging paragraph numerals in `primary`
 * — Interpretive clause as a tonal `surfaceContainerHigh` card with primary-coloured eyebrow
 * — Top reading-progress bar tied to scroll position
 * — "Next article" tonal footer card
 *
 * Body font size honours `LocalReadingFontScale` so the screen responds to
 * the global Settings → Font size selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int,
    repository: ConstitutionRepository,
    onBack: () -> Unit,
    onOpenArticle: (Int) -> Unit = {},
) {
    val article by repository.observeArticle(articleId).collectAsState(initial = null)
    val paragraphs by repository.observeParagraphs(articleId).collectAsState(initial = emptyList())
    val clauses by repository.observeInterpretiveClauses(articleId).collectAsState(initial = emptyList())
    val isBookmarked by repository.observeIsBookmarked(articleId).collectAsState(initial = false)
    val notes by repository.observeNotesForArticle(articleId).collectAsState(initial = emptyList())
    val nextArticle by repository.observeNextArticle(articleId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    val currentArticle = article ?: return
    val part by repository.observePart(currentArticle.partId).collectAsState(initial = null)

    val title = "${strings.articlePrefix} ${currentArticle.number}"
    val fullText = buildArticleText(title, paragraphs, clauses, strings)

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    // Note-editor bottom sheet: null = closed, otherwise New or Edit(note).
    // Hoisted here so the sheet survives recomposition of NoteSection and the
    // section itself stays display-only.
    var noteSheetTarget by remember { mutableStateOf<NoteSheetTarget?>(null) }
    val progress by remember {
        derivedStateOf {
            val max = scrollState.maxValue
            if (max <= 0) 0f else (scrollState.value.toFloat() / max).coerceIn(0f, 1f)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            part?.subtitle?.takeIf { it.isNotBlank() }?.let { sub ->
                                Text(
                                    text = "${part?.title ?: ""} · $sub".trim(' ', '·'),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } ?: part?.title?.let { t ->
                                Text(
                                    text = t,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // Snapshot state at click time so the snackbar copy reflects what
                            // the user just did, not whatever the next Flow emission says.
                            val wasBookmarked = isBookmarked
                            scope.launch {
                                if (wasBookmarked) repository.removeBookmark(articleId)
                                else repository.addBookmark(articleId)
                                val message = if (wasBookmarked) strings.bookmarkRemovedShortSnackbar
                                else strings.bookmarkAddedSnackbar
                                val result = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = strings.undo,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    if (wasBookmarked) repository.addBookmark(articleId)
                                    else repository.removeBookmark(articleId)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) strings.removeBookmark else strings.addBookmark,
                            )
                        }
                        IconButton(onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, fullText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, strings.share))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = strings.share)
                        }
                    },
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            ArticleEyebrow(
                paragraphsLabel = strings.paragraphsCount(paragraphs.size),
                clausesLabel = if (clauses.isNotEmpty()) strings.clausesCount(clauses.size) else null,
            )
            Spacer(Modifier.height(8.dp))
            ArticleHeadline(title = title)
            Spacer(Modifier.height(20.dp))

            for (paragraph in paragraphs) {
                ParagraphItem(paragraph, onRefClick = onOpenArticle)
                Spacer(Modifier.height(18.dp))
            }

            for (clause in clauses) {
                InterpretiveClauseItem(clause)
                Spacer(Modifier.height(12.dp))
            }

            nextArticle?.let { next ->
                Spacer(Modifier.height(12.dp))
                NextArticleFooter(
                    eyebrow = strings.nextArticleEyebrow,
                    label = "${strings.articlePrefix} ${next.number}" +
                        (next.title?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""),
                    onClick = { onOpenArticle(next.id) },
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            NoteSection(
                notes = notes,
                onAddRequest = { noteSheetTarget = NoteSheetTarget.New },
                onEditRequest = { note -> noteSheetTarget = NoteSheetTarget.Edit(note) },
                onDelete = { note ->
                    scope.launch { repository.deleteNote(note) }
                },
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    // Bottom-sheet note editor — sits outside Scaffold so it can render above
    // the top app bar / progress indicator chrome.
    noteSheetTarget?.let { target ->
        NoteEditorSheet(
            target = target,
            onDismiss = { noteSheetTarget = null },
            onSave = { content ->
                scope.launch {
                    when (target) {
                        is NoteSheetTarget.New ->
                            repository.addNote(articleId, content = content)
                        is NoteSheetTarget.Edit ->
                            repository.updateNote(target.note.copy(content = content))
                    }
                    noteSheetTarget = null
                    snackbarHostState.showSnackbar(strings.noteSavedSnackbar)
                }
            },
        )
    }
}

/**
 * Modelled as a sealed type rather than two booleans so we can't end up in an
 * illegal "editing AND adding" state.
 */
internal sealed interface NoteSheetTarget {
    data object New : NoteSheetTarget
    data class Edit(val note: Note) : NoteSheetTarget
}

@Composable
internal fun ArticleEyebrow(paragraphsLabel: String, clausesLabel: String?) {
    val text = if (clausesLabel != null) "$paragraphsLabel · $clausesLabel" else paragraphsLabel
    Text(
        text = text.uppercase(LocalAppStrings.current.locale),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
        ),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
internal fun ArticleHeadline(title: String) {
    val scale = LocalReadingFontScale.current
    val base = MaterialTheme.typography.headlineMedium
    Text(
        text = title,
        style = base.copy(
            fontSize = base.fontSize * scale,
            lineHeight = base.lineHeight * scale,
            fontWeight = FontWeight.Medium,
        ),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
internal fun ParagraphItem(
    paragraph: Paragraph,
    onRefClick: (Int) -> Unit = {},
) {
    val scale = LocalReadingFontScale.current
    val base = MaterialTheme.typography.bodyLarge
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (paragraph.number != null) {
                Text(
                    text = "${paragraph.number}.",
                    style = base.copy(
                        fontSize = base.fontSize * scale * 0.95f,
                        lineHeight = base.lineHeight * scale,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(28.dp)
                        .padding(top = 2.dp),
                )
                Spacer(Modifier.width(14.dp))
            }
            Text(
                text = paragraph.content,
                style = base.copy(
                    fontSize = base.fontSize * scale,
                    lineHeight = base.lineHeight * scale,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (paragraph.refs.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (paragraph.number != null) 42.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (refId in paragraph.refs) {
                    CrossRefChip(refId = refId, onClick = { onRefClick(refId) })
                }
            }
        }
    }
}

/**
 * Outlined pill chip rendering one cross-reference link.
 *
 * Label uses the ref's [Article.id]; works while every article has a purely
 * numeric `number`. Articles like "120Α" would need a repository lookup so
 * the chip shows the display number, not the id.
 */
@Composable
internal fun CrossRefChip(refId: Int, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    AssistChip(
        onClick = onClick,
        label = { Text("↗ ${strings.articlePrefix} $refId") },
        border = AssistChipDefaults.assistChipBorder(enabled = true),
    )
}

@Composable
internal fun InterpretiveClauseItem(clause: InterpretiveClause) {
    val strings = LocalAppStrings.current
    val scale = LocalReadingFontScale.current
    val body = MaterialTheme.typography.bodyMedium
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = strings.interpretiveClause.uppercase(strings.locale),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = clause.content,
                style = body.copy(
                    fontSize = body.fontSize * scale * 0.95f,
                    lineHeight = body.lineHeight * scale,
                ),
                fontStyle = FontStyle.Italic,
            )
        }
    }
}

@Composable
internal fun NextArticleFooter(eyebrow: String, label: String, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eyebrow.uppercase(strings.locale),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
internal fun NoteSection(
    notes: List<Note>,
    onAddRequest: () -> Unit,
    onEditRequest: (Note) -> Unit,
    onDelete: (Note) -> Unit,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = strings.notes,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onAddRequest) {
            Icon(Icons.Default.Add, contentDescription = strings.addNote)
        }
    }

    for (note in notes) {
        NoteItem(
            note = note,
            onEdit = { onEditRequest(note) },
            onDelete = { onDelete(note) },
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun NoteItem(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTimestamp(note.updatedAt, strings),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = strings.editNote,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = strings.deleteNote,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteNoteTitle) },
            text = { Text(strings.deleteNoteBody) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(strings.deleteNote)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}

/**
 * Material 3 bottom-sheet note editor.
 *
 * Draft is `remember(target)`-keyed so switching from one note to another
 * resets it cleanly. Save is disabled until the trimmed draft is non-empty.
 * Sheet height is content-driven; the keyboard inset is handled by
 * `ModalBottomSheet` automatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteEditorSheet(
    target: NoteSheetTarget,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initial = when (target) {
        is NoteSheetTarget.New -> ""
        is NoteSheetTarget.Edit -> target.note.content
    }
    var draft by remember(target) { mutableStateOf(initial) }
    val title = if (target is NoteSheetTarget.New) strings.newNoteTitle else strings.editNoteTitle
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(target) {
        // FocusRequester throws if its target isn't yet attached — possible
        // in tests where the sheet content composes after this LaunchedEffect
        // fires. Production layout is stable by the time we reach here.
        runCatching { focusRequester.requestFocus() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text(strings.noteHint) },
                minLines = 4,
                maxLines = 10,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text(strings.cancel) }
                Spacer(Modifier.width(4.dp))
                TextButton(
                    onClick = { if (draft.isNotBlank()) onSave(draft.trim()) },
                    enabled = draft.isNotBlank(),
                ) {
                    Text(strings.save)
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long, strings: AppStrings): String {
    val sdf = SimpleDateFormat("d MMM yyyy", strings.locale)
    return sdf.format(Date(millis))
}

private fun buildArticleText(
    title: String,
    paragraphs: List<Paragraph>,
    clauses: List<InterpretiveClause>,
    strings: AppStrings,
): String = buildString {
    appendLine(title)
    appendLine()
    for (p in paragraphs) {
        if (p.number != null) append("${p.number}. ")
        appendLine(p.content)
    }
    for (c in clauses) {
        appendLine()
        appendLine(strings.interpretiveClauseHeader)
        appendLine(c.content)
    }
}
