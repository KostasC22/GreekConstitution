package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = Part::class,
            parentColumns = ["id"],
            childColumns = ["partId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("partId")],
)
data class Section(
    @PrimaryKey val id: Int,
    val partId: Int,
    val order: Int,
    val title: String,
    val subtitle: String?,
)
