package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Section::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sectionId")],
)
data class Chapter(
    @PrimaryKey val id: Int,
    val sectionId: Int,
    val order: Int,
    val title: String,
    val subtitle: String?,
)
