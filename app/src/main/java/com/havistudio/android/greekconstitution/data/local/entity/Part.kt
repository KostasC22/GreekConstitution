package com.havistudio.android.greekconstitution.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parts")
data class Part(
    @PrimaryKey val id: Int,
    val order: Int,
    val title: String,
    val subtitle: String?,
)
