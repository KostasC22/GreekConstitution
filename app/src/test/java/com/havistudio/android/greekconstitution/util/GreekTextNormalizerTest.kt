package com.havistudio.android.greekconstitution.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GreekTextNormalizerTest {

    @Test
    fun `monotonic diacritics are stripped`() {
        assertEquals("αρθρο", GreekTextNormalizer.normalize("άρθρο"))
    }

    @Test
    fun `polytonic diacritics are stripped`() {
        assertEquals("αρθρο", GreekTextNormalizer.normalize("ἄρθρο"))
    }

    @Test
    fun `final sigma is normalized to medial sigma`() {
        assertEquals("βουλησ", GreekTextNormalizer.normalize("Βουλής"))
    }

    @Test
    fun `mixed diacritics and final sigma`() {
        assertEquals("ελευθεριασ", GreekTextNormalizer.normalize("Ελευθερίας"))
    }

    @Test
    fun `uppercase Greek is lowercased`() {
        assertEquals("προεδροσ", GreekTextNormalizer.normalize("ΠΡΌΕΔΡΟΣ"))
    }

    @Test
    fun `already normalized input is unchanged`() {
        assertEquals("αρθρο", GreekTextNormalizer.normalize("αρθρο"))
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", GreekTextNormalizer.normalize(""))
    }

    @Test
    fun `whitespace is preserved`() {
        assertEquals("  ", GreekTextNormalizer.normalize("  "))
    }

    @Test
    fun `latin characters pass through unchanged`() {
        assertEquals("article", GreekTextNormalizer.normalize("article"))
    }

    @Test
    fun `mixed Greek and Latin`() {
        assertEquals("test αρθρο 5", GreekTextNormalizer.normalize("test Άρθρο 5"))
    }

    @Test
    fun `multiple words with diacritics`() {
        assertEquals(
            "η βουλη των ελληνων",
            GreekTextNormalizer.normalize("Η Βουλή των Ελλήνων"),
        )
    }

    @Test
    fun `iota subscript is stripped`() {
        assertEquals("α", GreekTextNormalizer.normalize("ᾳ"))
    }
}
