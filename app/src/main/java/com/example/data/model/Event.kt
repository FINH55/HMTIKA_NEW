package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // Seminar, Workshop, Lomba, Gathering, Bootcamp
    val date: String,
    val time: String,
    val location: String, // e.g. "Ruang Teater Gd. B", "Lab Informatika 3", "Zoom Meeting"
    val speaker: String,
    val description: String,
    val quota: Int = 100,
    val registeredCount: Int = 45,
    val bannerTheme: Int = 0, // color gradient index
    val certificateAvailable: Boolean = true,
    val isCompleted: Boolean = false
)
