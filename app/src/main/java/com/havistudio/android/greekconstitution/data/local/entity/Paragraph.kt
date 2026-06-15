package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "paragraphs",
    foreignKeys = [
        ForeignKey(
            entity = Article::class,
            parentColumns = ["id"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("articleId")],
)
data class Paragraph(
    @PrimaryKey val id: Int,
    val articleId: Int,
    val number: String?,
    val order: Int,
    val content: String,
    /**
     * Diacritic-stripped + lowercased copy of [content] used by the FTS index.
     * Android's bundled SQLite on API 26 only supports unicode61 with Latin
     * diacritic folding, so we normalize Greek ourselves and index that.
     */
    val searchContent: String,
    /**
     * IDs of other articles this paragraph cross-references. Empty when the
     * paragraph stands alone. Rendered as tappable "↗ Άρθρο N" chips in the
     * Article Detail screen.
     *
     * Stored as a comma-separated string via
     * [com.havistudio.android.greekconstitution.data.local.Converters]. Defaults to
     * empty so older rows (pre-v2 schema) read back cleanly after the v1→v2
     * ALTER migration, and raw SQL inserts that omit the column still work.
     */
    @ColumnInfo(defaultValue = "")
    val refs: List<Int> = emptyList(),
)
