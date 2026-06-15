package com.havistudio.android.greekconstitution.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Builds a snippet AnnotatedString centered on the first occurrence of
 * [query] inside [content], with the match span highlighted.
 *
 * Diacritic-insensitive: matches normalized-vs-normalized then maps the index
 * back into the original (display) text so the highlight sits on the real
 * Greek glyphs, not the stripped ones.
 */
object SearchHighlight {

    fun build(
        content: String,
        query: String,
        highlightBackground: Color,
        highlightForeground: Color,
        maxBefore: Int = 30,
        maxAfter: Int = 120,
    ): AnnotatedString {
        if (query.isBlank()) return AnnotatedString(content.take(maxBefore + maxAfter))

        val normQuery = GreekTextNormalizer.normalize(query.trim())
        val range = findMatchRange(content, normQuery)
            ?: return AnnotatedString(content.take(maxBefore + maxAfter))

        val (origStart, origEnd) = range.first to range.last + 1

        val windowStart = (origStart - maxBefore).coerceAtLeast(0)
        val windowEnd = (origEnd + maxAfter).coerceAtMost(content.length)

        val pre = if (windowStart > 0) "…" else ""
        val post = if (windowEnd < content.length) "…" else ""

        return buildAnnotatedString {
            append(pre)
            append(content.substring(windowStart, origStart))
            withStyle(
                SpanStyle(
                    background = highlightBackground,
                    color = highlightForeground,
                    fontWeight = FontWeight.SemiBold,
                ),
            ) {
                append(content.substring(origStart, origEnd))
            }
            append(content.substring(origEnd, windowEnd))
            append(post)
        }
    }

    private fun findMatchRange(original: String, normalizedQuery: String): IntRange? {
        if (normalizedQuery.isEmpty()) return null
        val normalized = GreekTextNormalizer.normalize(original)
        val matchIdx = normalized.indexOf(normalizedQuery)
        if (matchIdx < 0) return null

        var origIdx = 0
        var normIdx = 0
        var origStart = -1
        var origEnd = -1
        while (origIdx <= original.length) {
            if (origStart < 0 && normIdx >= matchIdx) origStart = origIdx
            if (normIdx >= matchIdx + normalizedQuery.length) {
                origEnd = origIdx
                break
            }
            if (origIdx == original.length) break
            val chNorm = GreekTextNormalizer.normalize(original[origIdx].toString())
            normIdx += chNorm.length
            origIdx++
        }
        if (origStart < 0) return null
        if (origEnd < 0) origEnd = original.length
        return origStart until origEnd
    }
}
