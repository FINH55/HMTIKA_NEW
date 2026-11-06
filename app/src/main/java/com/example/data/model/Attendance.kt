package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meetingTitle: String,
    val userName: String,
    val userNim: String,
    val role: String = "Pengurus Core Team",
    val timestamp: String,
    val photoBase64OrUri: String = "",
    val notes: String = "Hadir tepat waktu",
    val status: String = "TERVERIFIKASI"
)
