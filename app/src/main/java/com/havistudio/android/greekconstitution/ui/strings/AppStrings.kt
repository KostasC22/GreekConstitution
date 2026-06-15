package com.havistudio.android.greekconstitution.ui.strings

import androidx.compose.runtime.compositionLocalOf
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import java.util.Locale

/**
 * App-wide localized chrome strings. Constitution body content (paragraphs,
 * interpretive clauses, article titles persisted in the DB) always stays in
 * Greek — only UI surfaces translate.
 */
data class AppStrings(
    val language: UiLanguage,

    // App / Disclaimer
    val appTitle: String,
    val disclaimerBody: String,
    val accept: String,

    // Bottom nav
    val home: String,
    val search: String,
    val bookmarks: String,
    val settingsNav: String,

    // Home
    val expand: String,
    val collapse: String,

    // Search
    val searchPlaceholder: String,
    val clear: String,
    val noResults: String,
    val searchSuggestEyebrow: String,
    val searchEmptyForQuery: (String) -> String,
    val searchEmptyHint: String,
    val searchResultsCount: (Int) -> String,
    val recentSearches: String,
    val remove: String,

    // Bookmarks
    val bookmarksEmpty: String,
    val bookmarksEmptyHint: String,
    val bookmarkRemovedSnackbar: (String) -> String,
    val undo: String,
    val bookmarkAddedSnackbar: String,
    val bookmarkRemovedShortSnackbar: String,

    // Article detail
    val articlePrefix: String,
    val back: String,
    val addBookmark: String,
    val removeBookmark: String,
    val share: String,
    val interpretiveClause: String,
    val interpretiveClauseHeader: String,
    val nextArticleEyebrow: String,
    val paragraphsCount: (Int) -> String,
    val clausesCount: (Int) -> String,
    val notes: String,
    val addNote: String,
    val editNote: String,
    val deleteNote: String,
    val deleteNoteTitle: String,
    val deleteNoteBody: String,
    val newNoteTitle: String,
    val editNoteTitle: String,
    val noteSavedSnackbar: String,
    val cancel: String,
    val save: String,
    val noteHint: String,

    // Settings — full table
    val settingsTitle: String,
    val appearance: String,
    val theme: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val dynamicColor: String,
    val dynamicColorSub: String,
    val fontSize: String,
    val fontSmall: String,
    val fontMedium: String,
    val fontLarge: String,
    val fontXLarge: String,
    val previewLabel: String,
    val languageSection: String,
    val languageNote: String,
    val about: String,
    val source: String,
    val sourceSub: String,
    val privacy: String,
    val privacySub: String,
    val privacyDialogBody: String,
    val showDisclaimer: String,
    val showDisclaimerBody: String,
    val version: String,
    val confirm: String,
) {
    /** Locale for date / number formatting that should follow the UI language. */
    val locale: Locale
        get() = when (language) {
            UiLanguage.Greek -> Locale.forLanguageTag("el")
            UiLanguage.English -> Locale.ENGLISH
        }

    companion object {
        fun forLanguage(language: UiLanguage): AppStrings = when (language) {
            UiLanguage.Greek -> Greek
            UiLanguage.English -> English
        }

        private val Greek = AppStrings(
            language               = UiLanguage.Greek,
            appTitle               = "Σύνταγμα της Ελλάδας",
            disclaimerBody         = "Το κείμενο του Συντάγματος που περιέχεται στην εφαρμογή είναι " +
                "ανεπίσημο και παρέχεται αποκλειστικά για ενημερωτικούς σκοπούς. " +
                "Για το επίσημο κείμενο, ανατρέξτε στην ιστοσελίδα της Βουλής " +
                "των Ελλήνων: hellenicparliament.gr",
            accept                 = "Αποδοχή",

            home                   = "Αρχική",
            search                 = "Αναζήτηση",
            bookmarks              = "Σελιδοδείκτες",
            settingsNav            = "Ρυθμίσεις",

            expand                 = "Ανάπτυξη",
            collapse               = "Σύμπτυξη",

            searchPlaceholder      = "Αναζήτηση στο Σύνταγμα…",
            clear                  = "Καθαρισμός",
            noResults              = "Δεν βρέθηκαν αποτελέσματα",
            searchSuggestEyebrow   = "Δοκιμάστε",
            searchEmptyForQuery    = { q -> "Δεν βρέθηκαν αποτελέσματα για «$q»" },
            searchEmptyHint        = "Δοκιμάστε άλλη λέξη ή ελέγξτε την ορθογραφία.",
            searchResultsCount     = { n -> if (n == 1) "1 αποτέλεσμα" else "$n αποτελέσματα" },
            recentSearches         = "Πρόσφατες αναζητήσεις",
            remove                 = "Αφαίρεση",

            bookmarksEmpty         = "Δεν υπάρχουν σελιδοδείκτες",
            bookmarksEmptyHint     = "Πατήστε το εικονίδιο σελιδοδείκτη σε ένα άρθρο για να το αποθηκεύσετε εδώ.",
            bookmarkRemovedSnackbar = { num -> "Αφαιρέθηκε «Άρθρο $num»" },
            undo                   = "Αναίρεση",
            bookmarkAddedSnackbar  = "Προστέθηκε στους σελιδοδείκτες",
            bookmarkRemovedShortSnackbar = "Αφαιρέθηκε από τους σελιδοδείκτες",

            articlePrefix          = "Άρθρο",
            back                   = "Πίσω",
            addBookmark            = "Προσθήκη σελιδοδείκτη",
            removeBookmark         = "Αφαίρεση σελιδοδείκτη",
            share                  = "Κοινοποίηση",
            interpretiveClause     = "Ερμηνευτική δήλωση",
            interpretiveClauseHeader = "Ερμηνευτική δήλωση:",
            nextArticleEyebrow     = "Επόμενο",
            paragraphsCount        = { n -> if (n == 1) "1 παράγραφος" else "$n παράγραφοι" },
            clausesCount           = { n -> if (n == 1) "1 ερμηνευτική δήλωση" else "$n ερμηνευτικές δηλώσεις" },
            notes                  = "Σημειώσεις",
            addNote                = "Προσθήκη σημείωσης",
            editNote               = "Επεξεργασία",
            deleteNote             = "Διαγραφή",
            deleteNoteTitle        = "Διαγραφή σημείωσης;",
            deleteNoteBody         = "Η σημείωση θα διαγραφεί οριστικά.",
            newNoteTitle           = "Νέα σημείωση",
            editNoteTitle          = "Επεξεργασία σημείωσης",
            noteSavedSnackbar      = "Η σημείωση αποθηκεύτηκε",
            cancel                 = "Ακύρωση",
            save                   = "Αποθήκευση",
            noteHint               = "Γράψτε μια σημείωση…",

            settingsTitle          = "Ρυθμίσεις",
            appearance             = "Εμφάνιση",
            theme                  = "Θέμα",
            themeSystem            = "Σύστημα",
            themeLight             = "Ανοιχτό",
            themeDark              = "Σκοτεινό",
            dynamicColor           = "Δυναμικά χρώματα",
            dynamicColorSub        = "Χρήση παλέτας από την ταπετσαρία (Material You)",
            fontSize               = "Μέγεθος γραμματοσειράς",
            fontSmall              = "Μικρό",
            fontMedium             = "Κανονικό",
            fontLarge              = "Μεγάλο",
            fontXLarge             = "Πολύ μεγάλο",
            previewLabel           = "ΑΡΘΡΟ 5 §1 · ΠΡΟΕΠΙΣΚΟΠΗΣΗ",
            languageSection        = "Γλώσσα διεπαφής",
            languageNote           = "Το κείμενο του Συντάγματος παραμένει στα Ελληνικά",
            about                  = "Σχετικά",
            source                 = "Πηγή κειμένου",
            sourceSub              = "Βουλή των Ελλήνων · 1975, αναθ. 2008",
            privacy                = "Πολιτική απορρήτου",
            privacySub             = "Δεν συλλέγονται δεδομένα",
            privacyDialogBody      = "Η εφαρμογή δεν συλλέγει, δεν αποθηκεύει και δεν " +
                "διαβιβάζει κανένα προσωπικό δεδομένο. Όλα τα δεδομένα " +
                "(σελιδοδείκτες, σημειώσεις, προτιμήσεις) παραμένουν " +
                "αποκλειστικά στη συσκευή σας.",
            showDisclaimer         = "Προβολή αποποίησης ευθυνών",
            showDisclaimerBody     = "Η αποποίηση θα εμφανιστεί ξανά κατά την επόμενη εκκίνηση της εφαρμογής.",
            version                = "Έκδοση 1.0.0",
            confirm                = "ΟΚ",
        )

        private val English = AppStrings(
            language               = UiLanguage.English,
            appTitle               = "Constitution of Greece",
            disclaimerBody         = "The text of the Constitution included in this app is " +
                "unofficial and provided for informational purposes only. " +
                "For the official text, refer to the Hellenic Parliament website: " +
                "hellenicparliament.gr",
            accept                 = "Accept",

            home                   = "Home",
            search                 = "Search",
            bookmarks              = "Bookmarks",
            settingsNav            = "Settings",

            expand                 = "Expand",
            collapse               = "Collapse",

            searchPlaceholder      = "Search the Constitution…",
            clear                  = "Clear",
            noResults              = "No results found",
            searchSuggestEyebrow   = "Try",
            searchEmptyForQuery    = { q -> "No results for «$q»" },
            searchEmptyHint        = "Try a different word or check your spelling.",
            searchResultsCount     = { n -> if (n == 1) "1 result" else "$n results" },
            recentSearches         = "Recent searches",
            remove                 = "Remove",

            bookmarksEmpty         = "No bookmarks yet",
            bookmarksEmptyHint     = "Tap the bookmark icon on an article to save it here.",
            bookmarkRemovedSnackbar = { num -> "Removed «Article $num»" },
            undo                   = "Undo",
            bookmarkAddedSnackbar  = "Added to bookmarks",
            bookmarkRemovedShortSnackbar = "Removed from bookmarks",

            articlePrefix          = "Article",
            back                   = "Back",
            addBookmark            = "Add bookmark",
            removeBookmark         = "Remove bookmark",
            share                  = "Share",
            interpretiveClause     = "Interpretive clause",
            interpretiveClauseHeader = "Interpretive clause:",
            nextArticleEyebrow     = "Next",
            paragraphsCount        = { n -> if (n == 1) "1 paragraph" else "$n paragraphs" },
            clausesCount           = { n -> if (n == 1) "1 interpretive clause" else "$n interpretive clauses" },
            notes                  = "Notes",
            addNote                = "Add note",
            editNote               = "Edit",
            deleteNote             = "Delete",
            deleteNoteTitle        = "Delete note?",
            deleteNoteBody         = "The note will be permanently deleted.",
            newNoteTitle           = "New note",
            editNoteTitle          = "Edit note",
            noteSavedSnackbar      = "Note saved",
            cancel                 = "Cancel",
            save                   = "Save",
            noteHint               = "Write a note…",

            settingsTitle          = "Settings",
            appearance             = "Appearance",
            theme                  = "Theme",
            themeSystem            = "System",
            themeLight             = "Light",
            themeDark              = "Dark",
            dynamicColor           = "Dynamic color",
            dynamicColorSub        = "Use colors from your wallpaper (Material You)",
            fontSize               = "Font size",
            fontSmall              = "Small",
            fontMedium             = "Default",
            fontLarge              = "Large",
            fontXLarge             = "Extra large",
            previewLabel           = "ARTICLE 5 §1 · PREVIEW",
            languageSection        = "Interface language",
            languageNote           = "The constitution text always stays in Greek",
            about                  = "About",
            source                 = "Text source",
            sourceSub              = "Hellenic Parliament · 1975, rev. 2008",
            privacy                = "Privacy policy",
            privacySub             = "No data is collected",
            privacyDialogBody      = "This app does not collect, store, or transmit any " +
                "personal data. All data (bookmarks, notes, preferences) " +
                "stays exclusively on your device.",
            showDisclaimer         = "Show disclaimer again",
            showDisclaimerBody     = "The disclaimer will reappear the next time you open the app.",
            version                = "Version 1.0.0",
            confirm                = "OK",
        )
    }
}

val LocalAppStrings = compositionLocalOf { AppStrings.forLanguage(UiLanguage.Greek) }

/**
 * Reading-pane font scale (1.0 = default). Article body text multiplies its
 * fontSize by this. Chrome typography stays at system size.
 */
val LocalReadingFontScale = compositionLocalOf { 1.0f }
