package com.havistudio.android.greekconstitution.util

import java.text.Normalizer

/**
 * Greek-aware text normalization for FTS indexing and search.
 *
 * Android's bundled SQLite unicode61 tokenizer (API 26+) folds case and Latin
 * diacritics but not Greek ones. We fold Greek ourselves by:
 *   1. NFD decomposing (separates base letters from combining marks),
 *   2. dropping all combining marks (U+0300–U+036F),
 *   3. mapping final sigma (`ς`) to medial (`σ`),
 *   4. lowercasing.
 *
 * Apply the same transform to query strings before running an FTS MATCH so
 * `ελευθερια` matches the indexed form of `ελευθερία` / `Ελευθερία` / etc.
 */
object GreekTextNormalizer {
    private val combiningMarks = Regex("\\p{InCombiningDiacriticalMarks}+")

    fun normalize(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
        val stripped = combiningMarks.replace(decomposed, "")
        return stripped.lowercase().replace('ς', 'σ')
    }
}
