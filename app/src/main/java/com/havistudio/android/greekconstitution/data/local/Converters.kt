package com.havistudio.android.greekconstitution.data.local

import androidx.room.TypeConverter

/**
 * Room converters shared across the database.
 *
 * Currently only handles `List<Int>` ↔ `String` for cross-reference fields
 * like [com.havistudio.android.greekconstitution.data.local.entity.Paragraph.refs].
 *
 * Format: comma-separated, no spaces, no surrounding brackets — e.g. "13,6,120".
 * An empty list is stored as the empty string (NOT NULL columns simplify
 * SQL and FTS rebuilds; null would force every reader to disambiguate
 * "no refs" from "missing data").
 */
class Converters {

    @TypeConverter
    fun intListToString(value: List<Int>): String =
        if (value.isEmpty()) "" else value.joinToString(",")

    @TypeConverter
    fun stringToIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(',').mapNotNull { it.trim().toIntOrNull() }
    }
}
