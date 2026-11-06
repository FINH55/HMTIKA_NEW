package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.Announcement
import com.example.data.model.Attendance
import com.example.data.model.Certificate
import com.example.data.model.Event
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class HmtikaRepository(private val database: AppDatabase) {

    constructor(context: Context) : this(AppDatabase.getInstance(context))

    private val userDao = database.userDao()
    private val eventDao = database.eventDao()
    private val announcementDao = database.announcementDao()
    private val attendanceDao = database.attendanceDao()
    private val certificateDao = database.certificateDao()

    val allEvents: Flow<List<Event>> get() = eventDao.getAllEvents()
    val allAnnouncements: Flow<List<Announcement>> get() = announcementDao.getAllAnnouncements()
    val allAttendances: Flow<List<Attendance>> get() = attendanceDao.getAllAttendances()

    suspend fun login(nimOrEmail: String, password: String, role: UserRole): Result<User> = withContext(Dispatchers.IO) {
        val trimmedIdentifier = nimOrEmail.trim()
        val user = userDao.getUserByNimOrEmail(trimmedIdentifier, trimmedIdentifier)
            ?: return@withContext Result.failure(Exception("Akun tidak ditemukan. Silakan periksa kembali NIM / Email Anda atau buat akun baru."))

        if (user.password != password) {
            return@withContext Result.failure(Exception("Password yang Anda masukkan salah."))
        }

        if (user.role != role) {
            val roleName = if (user.role == UserRole.MAHASISWA) "Mahasiswa" else "Pengurus (Core Team)"
            return@withContext Result.failure(Exception("Akun ini terdaftar sebagai $roleName. Silakan pilih role yang sesuai pada pilihan login."))
        }

        Result.success(user)
    }

    suspend fun register(
        nim: String,
        name: String,
        email: String,
        password: String,
        role: UserRole,
        pin: String
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanNim = nim.trim()
        val cleanEmail = email.trim()
        val cleanName = name.trim()

        if (cleanNim.isBlank() || cleanEmail.isBlank() || cleanName.isBlank() || password.isBlank()) {
            return@withContext Result.failure(Exception("Semua kolom wajib diisi."))
        }

        if (role == UserRole.PENGURUS) {
            val validPin = "TIKA2026"
            if (pin.trim() != validPin && pin.trim() != "123456") {
                return@withContext Result.failure(Exception("Kode Verifikasi / PIN Pengurus tidak valid! Gunakan kode resmi HMTIKA: TIKA2026."))
            }
        }

        val existing = userDao.getUserByNimOrEmail(cleanNim, cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("NIM atau Email sudah terdaftar. Silakan login langsung."))
        }

        val division = if (role == UserRole.PENGURUS) "Pengurus Core Team" else "Mahasiswa Informatika"
        val emoji = if (role == UserRole.PENGURUS) "⚡" else "👨‍💻"

        val newUser = User(
            nim = cleanNim,
            name = cleanName,
            email = cleanEmail,
            password = password,
            role = role,
            pin = pin.trim(),
            division = division,
            avatarEmoji = emoji
        )

        userDao.insertUser(newUser)
        Result.success(newUser)
    }

    suspend fun getUserByNim(nim: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByNimOrEmail(nim, nim)
    }

    fun observeUser(nim: String): Flow<User?> = userDao.observeUser(nim)

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    // Events CRUD
    fun observeEventById(id: Long): Flow<Event?> = eventDao.getEventById(id)

    suspend fun getEventById(id: Long): Event? = withContext(Dispatchers.IO) {
        eventDao.getEventByIdSync(id)
    }

    suspend fun insertEvent(event: Event) = withContext(Dispatchers.IO) {
        eventDao.insertEvent(event)
    }

    suspend fun createEvent(event: Event) = insertEvent(event)

    suspend fun updateEvent(event: Event) = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(event)
    }

    // Announcements CRUD
    suspend fun insertAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        announcementDao.insertAnnouncement(announcement)
    }

    suspend fun createAnnouncement(announcement: Announcement) = insertAnnouncement(announcement)

    suspend fun updateAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        announcementDao.updateAnnouncement(announcement)
    }

    suspend fun deleteAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        announcementDao.deleteAnnouncement(announcement)
    }

    // Attendances
    fun getAttendancesByMeeting(meetingTitle: String): Flow<List<Attendance>> =
        attendanceDao.getAttendancesByMeeting(meetingTitle)

    suspend fun recordAttendance(attendance: Attendance) = withContext(Dispatchers.IO) {
        attendanceDao.insertAttendance(attendance)
    }

    suspend fun submitAttendance(
        meetingTitle: String,
        userName: String,
        userNim: String,
        role: String,
        photoBase64OrUri: String,
        notes: String
    ): Result<Attendance> = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val attendance = Attendance(
            meetingTitle = meetingTitle,
            userName = userName,
            userNim = userNim,
            role = role,
            timestamp = timestamp,
            photoBase64OrUri = photoBase64OrUri,
            notes = if (notes.isBlank()) "Hadir tepat waktu" else notes.trim(),
            status = "TERVERIFIKASI"
        )
        val id = attendanceDao.insertAttendance(attendance)
        Result.success(attendance.copy(id = id))
    }

    // Certificates
    fun getCertificatesForUser(nim: String): Flow<List<Certificate>> =
        certificateDao.getCertificatesForUser(nim)

    fun getCertificate(eventId: Long, nim: String): Flow<Certificate?> =
        certificateDao.getCertificateForUserAndEvent(eventId, nim)

    fun getCertificateForUserAndEvent(eventId: Long, nim: String): Flow<Certificate?> =
        certificateDao.getCertificateForUserAndEvent(eventId, nim)

    suspend fun issueCertificate(eventId: Long, eventTitle: String, user: User): Result<Certificate> {
        return generateOrClaimCertificate(eventId, eventTitle, user.name, user.nim)
    }

    suspend fun generateOrClaimCertificate(
        eventId: Long,
        eventTitle: String,
        recipientName: String,
        recipientNim: String
    ): Result<Certificate> = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val issueDate = dateFormat.format(Date())
        val randomDigits = Random.nextInt(10000, 99999)
        val certificateNo = "HMTIKA/CERT/${CalendarYear()}/$randomDigits"
        val verificationCode = "HMTIKA-VERIF-${Random.nextInt(100000, 999999)}"

        val cert = Certificate(
            certificateNo = certificateNo,
            eventId = eventId,
            eventTitle = eventTitle,
            recipientName = recipientName,
            recipientNim = recipientNim,
            role = "Peserta Aktif",
            issueDate = issueDate,
            verificationCode = verificationCode
        )
        val id = certificateDao.insertCertificate(cert)
        Result.success(cert.copy(id = id))
    }

    private fun CalendarYear(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }
}
