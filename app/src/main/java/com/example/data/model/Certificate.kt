package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "certificates")
data class Certificate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val certificateNo: String,
    val eventId: Long,
    val eventTitle: String,
    val recipientName: String,
    val recipientNim: String,
    val role: String = "Peserta", // Peserta, Pemateri, Panitia
    val issueDate: String,
    val verificationCode: String,
    val signedByChairman: String = "M. Rizki Alamsyah (Ketua Himpunan)",
    val signedByHeadCommittee: String = "Bima Satria (Ketua Pelaksana)"
)
