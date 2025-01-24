package com.example.room.addtasks.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "task") val task: String,
    @ColumnInfo(name = "selected") val selected: Boolean = false
)
