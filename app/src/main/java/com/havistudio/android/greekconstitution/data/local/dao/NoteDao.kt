package com.havistudio.android.greekconstitution.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.havistudio.android.greekconstitution.data.local.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE articleId = :articleId ORDER BY updatedAt DESC")
    fun observeNotesForArticle(articleId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<Note>>

    @Query(
        """
        SELECT * FROM notes
        WHERE id IN (
            SELECT id FROM notes n1
            WHERE n1.updatedAt = (
                SELECT MAX(n2.updatedAt) FROM notes n2 WHERE n2.articleId = n1.articleId
            )
        )
        ORDER BY articleId
        """,
    )
    fun observeLatestNotePerArticle(): Flow<List<Note>>
}
