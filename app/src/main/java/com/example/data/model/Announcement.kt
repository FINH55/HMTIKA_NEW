package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val author: String, // e.g. "Biro Kominfo HMTIKA"
    val date: String,
    val priority: String = "PENTING", // "PENTING", "INFO", "AKADEMIK"
    val tag: String = "Pengurus Core Team"
)
