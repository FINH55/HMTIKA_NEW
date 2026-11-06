package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Announcement
import com.example.data.model.Attendance
import com.example.data.model.Certificate
import com.example.data.model.Event
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Event::class,
        Announcement::class,
        Attendance::class,
        Certificate::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun certificateDao(): CertificateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hmtika_mobile_v2.db"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val appDb = getInstance(context)
                seedDatabase(appDb)
            }
        }
    }
}

suspend fun seedDatabase(database: AppDatabase) {
    val userDao = database.userDao()
    val eventDao = database.eventDao()
    val announcementDao = database.announcementDao()
    val attendanceDao = database.attendanceDao()
    val certificateDao = database.certificateDao()

    if (userDao.count() == 0) {
        userDao.insertUser(
            User(
                nim = "2301010042",
                name = "Bagus Prasetyo",
                email = "bagus.prasetyo@kampus.ac.id",
                password = "password123",
                role = UserRole.MAHASISWA,
                division = "Mahasiswa Informatika",
                avatarEmoji = "🎓",
                semester = 4
            )
        )
        userDao.insertUser(
            User(
                nim = "2201010007",
                name = "Rian Pratama",
                email = "rian.core@hmtika.org",
                password = "pengurus123",
                role = UserRole.PENGURUS,
                pin = "TIKA2026",
                division = "Ketua Divisi Litbang",
                avatarEmoji = "⚡",
                semester = 6
            )
        )
    }

    if (eventDao.count() == 0) {
        eventDao.insertAll(
            listOf(
                Event(
                    id = 1,
                    title = "Tech Talk: Artificial Intelligence & Gemini in Android",
                    category = "Seminar",
                    date = "28 Oktober 2026",
                    time = "09.00 - 12.00 WIB",
                    location = "Ruang Teater Gedung B & Zoom Live",
                    speaker = "Aris Setiawan (Google Developer Expert)",
                    description = "Bedah tuntas implementasi arsitektur modern Android Jetpack Compose dan integrasi On-Device AI Gemini. Free snack, sertifikat ber-SKPI, dan networking eksklusif sesama tech enthusiast.",
                    quota = 150,
                    registeredCount = 124,
                    bannerTheme = 0,
                    certificateAvailable = true,
                    isCompleted = false
                ),
                Event(
                    id = 2,
                    title = "Competitive Programming & Algoritma Hackathon 2026",
                    category = "Lomba",
                    date = "05 November 2026",
                    time = "08.00 - 17.00 WIB",
                    location = "Lab Rekayasa Perangkat Lunak 3",
                    speaker = "Dewan Juri Nasional ICPC Indonesia",
                    description = "Asah kemampuan problem solving, logika, dan struktur data di ajang coding tahunan HMTIKA. Menangkan total hadiah 15 Juta Rupiah dan trofi bergilir Ketua Himpunan.",
                    quota = 60,
                    registeredCount = 48,
                    bannerTheme = 1,
                    certificateAvailable = true,
                    isCompleted = false
                ),
                Event(
                    id = 3,
                    title = "Bootcamp Fullstack: Modern Jetpack Compose & Clean Architecture",
                    category = "Workshop",
                    date = "12 November 2026",
                    time = "13.00 - 16.30 WIB",
                    location = "Lab Multimedia & Jaringan Gedung C",
                    speaker = "Tim Litbang HMTIKA",
                    description = "Hands-on coding membangun aplikasi mobile modern dari nol menggunakan Kotlin DSL, Room persistence, Coroutines Flow, dan M3 Material Design.",
                    quota = 40,
                    registeredCount = 39,
                    bannerTheme = 2,
                    certificateAvailable = true,
                    isCompleted = false
                ),
                Event(
                    id = 4,
                    title = "Webinar Karir: Menembus Big Tech & Persiapan Magang MSIB",
                    category = "Seminar",
                    date = "15 September 2026",
                    time = "19.30 - 21.30 WIB",
                    location = "Online via Zoom & YouTube HMTIKA",
                    speaker = "Dimas Surya (Senior SWE at Unicorn)",
                    description = "Tips lolos CV screening ATS, technical interview, live coding challenges, dan strategi portofolio GitHub untuk mahasiswa Informatika.",
                    quota = 300,
                    registeredCount = 285,
                    bannerTheme = 3,
                    certificateAvailable = true,
                    isCompleted = true
                )
            )
        )
    }

    if (announcementDao.count() == 0) {
        announcementDao.insertAll(
            listOf(
                Announcement(
                    id = 1,
                    title = "Wajib: Rapat Pleno Core Team HMTIKA Evaluasi Program Kerja",
                    content = "Seluruh jajaran pengurus Core Team HMTIKA (BPH, Litbang, Kominfo, Kaderisasi, dan Danus) diwajibkan hadir pada Rapat Pleno di Sekretariat HMTIKA. Presensi menggunakan foto live kamera aplikasi HMTIKA Mobile.",
                    author = "Sekretariat Jenderal HMTIKA",
                    date = "Hari Ini, 16.00 WIB",
                    priority = "PENTING",
                    tag = "Core Team Only"
                ),
                Announcement(
                    id = 2,
                    title = "Distribusi Sertifikat Webinar Karir Big Tech Sudah Terbuka",
                    content = "Bagi mahasiswa yang telah mengisi presensi kehadiran dan evaluasi Webinar Karir, e-sertifikat resmi ber-barcode verifikasi sudah dapat diunduh langsung di menu Detail Event & Sertifikat.",
                    author = "Divisi Kominfo",
                    date = "Kemarin, 14.15 WIB",
                    priority = "INFO",
                    tag = "Mahasiswa"
                ),
                Announcement(
                    id = 3,
                    title = "Pengambilan Atribut Jas Laboratorium Informatika TA 2026/2027",
                    content = "Bagi mahasiswa angkatan 2025 dan 2026 yang telah memesan jas lab, pengambilan dapat dilakukan di Sekre HMTIKA setiap hari kerja jam 10.00 - 15.00 WIB.",
                    author = "Divisi Dana Usaha",
                    date = "18 Oktober 2026",
                    priority = "AKADEMIK",
                    tag = "Semua Mahasiswa"
                )
            )
        )
    }

    if (attendanceDao.count() == 0) {
        attendanceDao.insertAll(
            listOf(
                Attendance(
                    id = 1,
                    meetingTitle = "Rapat Koordinasi Dies Natalis HMTIKA",
                    userName = "Rian Pratama",
                    userNim = "2201010007",
                    role = "Ketua Divisi Litbang",
                    timestamp = "2026-10-20 16:04:12",
                    photoBase64OrUri = "",
                    notes = "Hadir tepat waktu di Sekretariat",
                    status = "TERVERIFIKASI"
                ),
                Attendance(
                    id = 2,
                    meetingTitle = "Rapat Pleno BPH & Litbang",
                    userName = "M. Rizki Alamsyah",
                    userNim = "2201010001",
                    role = "Ketua Himpunan",
                    timestamp = "2026-10-18 19:30:05",
                    photoBase64OrUri = "",
                    notes = "Memimpin jalannya rapat pleno",
                    status = "TERVERIFIKASI"
                )
            )
        )
    }

    if (certificateDao.count() == 0) {
        certificateDao.insertAll(
            listOf(
                Certificate(
                    id = 1,
                    certificateNo = "HMTIKA/CERT/2026/042",
                    eventId = 4,
                    eventTitle = "Webinar Karir: Menembus Big Tech & Persiapan Magang MSIB",
                    recipientName = "Bagus Prasetyo",
                    recipientNim = "2301010042",
                    role = "Peserta Aktif",
                    issueDate = "15 September 2026",
                    verificationCode = "HMTIKA-VERIF-99214"
                )
            )
        )
    }
}
