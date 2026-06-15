package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
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
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Int,
    val paragraphId: Int?,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
