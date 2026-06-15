package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    foreignKeys = [
        ForeignKey(
            entity = Part::class,
            parentColumns = ["id"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Section::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("partId"), Index("sectionId"), Index("chapterId")],
)
data class Article(
    @PrimaryKey val id: Int,
    val number: String,
    val partId: Int,
    val sectionId: Int?,
    val chapterId: Int?,
    val order: Int,
    val title: String?,
)
