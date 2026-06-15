package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * External-content-free FTS4 shadow of [Paragraph.searchContent].
 *
 * We manage `rowid` ourselves so it matches the parent `Paragraph.id`, which
 * lets search results JOIN back to `paragraphs` cheaply. unicode61 on
 * Android's bundled SQLite (API 26+) folds case and Latin diacritics only —
 * Greek folding happens at insert time via [com.havistudio.android.greekconstitution.util.GreekTextNormalizer].
 */
@Entity(tableName = "paragraph_fts")
@Fts4(tokenizer = "unicode61")
data class ParagraphFts(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Int,
    val searchContent: String,
)
