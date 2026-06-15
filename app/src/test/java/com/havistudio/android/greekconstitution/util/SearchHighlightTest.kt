package com.havistudio.android.greekconstitution.util

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHighlightTest {

    private val bg = Color(0xFFFFFF00)
    private val fg = Color(0xFF000000)

    private fun build(content: String, query: String) =
        SearchHighlight.build(content, query, bg, fg)

    @Test
    fun `empty query returns leading slice unhighlighted`() {
        val result = build("σύνταγμα της ελλάδας", "")
        assertEquals("σύνταγμα της ελλάδας", result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `query not in content returns leading slice unhighlighted`() {
        val content = "καθένας έχει δικαίωμα να αναπτύσσει ελεύθερα την προσωπικότητά του"
        val result = build(content, "ξενοτεκλο")
        assertEquals(content, result.text)
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `precomposed Greek query matches and is highlighted`() {
        val content = "καθένας έχει δικαίωμα να αναπτύσσει ελεύθερα την προσωπικότητά του"
        val result = build(content, "δικαίωμα")
        assertTrue(result.text.contains("δικαίωμα"))
        assertEquals(1, result.spanStyles.size)
        val span = result.spanStyles.first()
        assertEquals("δικαίωμα", result.text.substring(span.start, span.end))
    }

    @Test
    fun `diacritic-insensitive match maps back to original glyphs`() {
        // query "εθνος" (no diacritics) matches "Έθνος" in content; the
        // highlight must land on the original glyphs, not the normalized ones.
        val content = "Το Έθνος είναι κυρίαρχο"
        val result = build(content, "εθνος")
        assertEquals(1, result.spanStyles.size)
        val span = result.spanStyles.first()
        assertEquals("Έθνος", result.text.substring(span.start, span.end))
    }

    @Test
    fun `case-insensitive match works`() {
        val content = "Καθένας έχει δικαίωμα"
        val result = build(content, "καθενας")
        assertEquals(1, result.spanStyles.size)
        val span = result.spanStyles.first()
        assertEquals("Καθένας", result.text.substring(span.start, span.end))
    }

    @Test
    fun `final sigma normalization matches medial sigma queries`() {
        val content = "Η Βουλή των Ελλήνων"
        val result = build(content, "βουλη")
        assertEquals(1, result.spanStyles.size)
        val span = result.spanStyles.first()
        assertEquals("Βουλή", result.text.substring(span.start, span.end))
    }

    @Test
    fun `match at start emits no leading ellipsis`() {
        val content = "δικαίωμα να αναπτύσσει ελεύθερα την προσωπικότητά του"
        val result = build(content, "δικαίωμα")
        assertFalse("Should not start with ellipsis", result.text.startsWith("…"))
    }

    @Test
    fun `match deep in long content emits leading ellipsis`() {
        // pad ~80 chars before the match so it falls outside the 30-char window.
        val prefix = "λάμδα ".repeat(20)
        val content = "${prefix}δικαίωμα στην ελευθερία"
        val result = build(content, "δικαίωμα")
        assertTrue("Should start with ellipsis: ${result.text}", result.text.startsWith("…"))
    }

    @Test
    fun `match near end emits no trailing ellipsis`() {
        val content = "ελεύθερη ανάπτυξη δικαίωμα"
        val result = build(content, "δικαίωμα")
        assertFalse(
            "Should not end with ellipsis: ${result.text}",
            result.text.endsWith("…"),
        )
    }

    @Test
    fun `match in middle of long content emits both ellipses`() {
        val prefix = "λάμδα ".repeat(20)
        val suffix = " απόφαση".repeat(40)
        val content = "${prefix}δικαίωμα$suffix"
        val result = build(content, "δικαίωμα")
        assertTrue("Should start with ellipsis", result.text.startsWith("…"))
        assertTrue("Should end with ellipsis", result.text.endsWith("…"))
    }

    @Test
    fun `whitespace-only query returns leading slice unhighlighted`() {
        val content = "καθένας έχει δικαίωμα"
        val result = build(content, "   ")
        assertTrue(result.spanStyles.isEmpty())
    }

    @Test
    fun `query gets trimmed before matching`() {
        val content = "καθένας έχει δικαίωμα"
        val result = build(content, "  δικαίωμα  ")
        assertEquals(1, result.spanStyles.size)
        val span = result.spanStyles.first()
        assertEquals("δικαίωμα", result.text.substring(span.start, span.end))
    }
}
