package com.havistudio.android.greekconstitution.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.havistudio.android.greekconstitution.data.local.dao.BookmarkDao
import com.havistudio.android.greekconstitution.data.local.dao.ConstitutionDao
import com.havistudio.android.greekconstitution.data.local.dao.NoteDao
import com.havistudio.android.greekconstitution.data.local.dao.SearchDao
import com.havistudio.android.greekconstitution.data.local.entity.Article
import com.havistudio.android.greekconstitution.data.local.entity.Bookmark
import com.havistudio.android.greekconstitution.data.local.entity.Chapter
import com.havistudio.android.greekconstitution.data.local.entity.InterpretiveClause
import com.havistudio.android.greekconstitution.data.local.entity.Note
import com.havistudio.android.greekconstitution.data.local.entity.Paragraph
import com.havistudio.android.greekconstitution.data.local.entity.ParagraphFts
import com.havistudio.android.greekconstitution.data.local.entity.Part
import com.havistudio.android.greekconstitution.data.local.entity.Section

@Database(
    entities = [
        Part::class,
        Section::class,
        Chapter::class,
        Article::class,
        Paragraph::class,
        ParagraphFts::class,
        InterpretiveClause::class,
        Bookmark::class,
        Note::class,
    ],
    version = 2,
    // Schema JSON lands in app/schemas (KSP `room.schemaLocation`). v1 predates
    // the export, so only v2+ exist; future migrations get MigrationTestHelper
    // coverage from here on.
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ConstitutionDatabase : RoomDatabase() {
    abstract fun constitutionDao(): ConstitutionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun searchDao(): SearchDao

    companion object {
        private const val DB_NAME = "constitution.db"

        /**
         * v1 → v2: adds the `refs` column on `paragraphs`. Stored as a NOT NULL
         * comma-separated `TEXT` (default '') so existing rows read back as
         * empty lists via [Converters.stringToIntList].
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE paragraphs ADD COLUMN refs TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        @Volatile private var INSTANCE: ConstitutionDatabase? = null

        fun get(context: Context): ConstitutionDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): ConstitutionDatabase {
            val appContext = context.applicationContext
            return Room.databaseBuilder(appContext, ConstitutionDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Populate in a background thread after the schema is
                        // created. Room's callback runs on the DB executor.
                        ConstitutionPrepopulator(appContext).populate(db)
                    }
                })
                .build()
        }
    }
}
