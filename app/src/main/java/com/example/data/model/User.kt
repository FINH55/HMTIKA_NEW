package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    MAHASISWA,
    PENGURUS
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val nim: String,
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val pin: String = "",
    val division: String = "Anggota",
    val avatarEmoji: String = "👨‍💻",
    val semester: Int = 4
)
