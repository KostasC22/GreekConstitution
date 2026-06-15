package com.havistudio.android.greekconstitution.ui.strings

import com.havistudio.android.greekconstitution.data.local.UiLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import kotlin.reflect.full.memberProperties

class AppStringsTest {

    @Test
    fun forLanguage_Greek_returns_greek_table() {
        val s = AppStrings.forLanguage(UiLanguage.Greek)

        assertEquals(UiLanguage.Greek, s.language)
        assertEquals("Σύνταγμα της Ελλάδας", s.appTitle)
        assertEquals("Αρχική", s.home)
        assertEquals("Αναζήτηση", s.search)
        assertEquals("Σελιδοδείκτες", s.bookmarks)
        assertEquals("Ρυθμίσεις", s.settingsNav)
        assertEquals("Άρθρο", s.articlePrefix)
    }

    @Test
    fun forLanguage_English_returns_english_table() {
        val s = AppStrings.forLanguage(UiLanguage.English)

        assertEquals(UiLanguage.English, s.language)
        assertEquals("Constitution of Greece", s.appTitle)
        assertEquals("Home", s.home)
        assertEquals("Search", s.search)
        assertEquals("Bookmarks", s.bookmarks)
        assertEquals("Settings", s.settingsNav)
        assertEquals("Article", s.articlePrefix)
    }

    @Test
    fun greek_and_english_tables_differ() {
        val greek = AppStrings.forLanguage(UiLanguage.Greek)
        val english = AppStrings.forLanguage(UiLanguage.English)

        assertNotEquals(greek.appTitle, english.appTitle)
        assertNotEquals(greek.home, english.home)
        assertNotEquals(greek.settingsTitle, english.settingsTitle)
    }

    @Test
    fun locale_for_Greek_is_el() {
        val s = AppStrings.forLanguage(UiLanguage.Greek)

        assertEquals("el", s.locale.language)
    }

    @Test
    fun locale_for_English_is_ENGLISH() {
        val s = AppStrings.forLanguage(UiLanguage.English)

        assertEquals(Locale.ENGLISH, s.locale)
    }

    @Test
    fun every_string_field_non_blank_in_both_tables() {
        for (lang in UiLanguage.values()) {
            val table = AppStrings.forLanguage(lang)
            for (prop in AppStrings::class.memberProperties) {
                val value = prop.get(table)
                if (value is String) {
                    assertTrue(
                        "Field ${prop.name} blank for $lang",
                        value.isNotBlank(),
                    )
                }
            }
        }
    }

    @Test
    fun forLanguage_is_idempotent() {
        assertEquals(AppStrings.forLanguage(UiLanguage.Greek), AppStrings.forLanguage(UiLanguage.Greek))
        assertEquals(AppStrings.forLanguage(UiLanguage.English), AppStrings.forLanguage(UiLanguage.English))
    }
}
