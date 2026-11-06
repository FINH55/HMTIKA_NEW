package com.example.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.R
import com.example.data.local.SessionManager
import com.example.data.model.Announcement
import com.example.data.model.Event
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.HmtikaRepository
import com.example.databinding.ActivityMainBinding
import com.example.databinding.DialogAddEventBinding
import com.example.ui.adapter.AnnouncementAdapter
import com.example.ui.adapter.EventAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: HmtikaRepository
    private lateinit var sessionManager: SessionManager

    private var currentUser: User? = null
    private var allEventsList: List<Event> = emptyList()
    private var eventAdapter: EventAdapter? = null
    private var announcementAdapter: AnnouncementAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = HmtikaRepository(this)
        sessionManager = SessionManager(this)

        val loggedInNim = sessionManager.getLoggedInNim()
        if (loggedInNim == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserData(loggedInNim)
        setupRecyclerViews()
        setupListeners()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        // Keep bottom nav on Home
        binding.bottomNav.selectedItemId = R.id.nav_home
    }

    private fun loadUserData(nim: String) {
        lifecycleScope.launch {
            val user = repository.getUserByNim(nim)
            if (user == null) {
                sessionManager.clearSession()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            currentUser = user
            bindHeaderAndRoleViews(user)
        }
    }

    private fun bindHeaderAndRoleViews(user: User) {
        val firstName = user.name.split(" ").firstOrNull() ?: user.name
        binding.tvGreeting.text = "Halo, $firstName 👋"
        binding.tvUserNim.text = "NIM: ${user.nim}"
        binding.tvHeaderAvatarEmoji.text = user.avatarEmoji

        if (user.role == UserRole.PENGURUS) {
            binding.tvRoleBadge.text = "⚡ CORE TEAM"
            binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_amber)
            binding.tvRoleBadge.setTextColor(getColor(R.color.amber_warning))

            binding.cardAbsenRapatCta.visibility = View.VISIBLE
            binding.layoutPengurusActions.visibility = View.VISIBLE
            binding.cardMahasiswaStats.visibility = View.GONE
        } else {
            binding.tvRoleBadge.text = "🎓 MAHASISWA"
            binding.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_teal)
            binding.tvRoleBadge.setTextColor(getColor(R.color.hmtika_teal))

            binding.cardAbsenRapatCta.visibility = View.GONE
            binding.layoutPengurusActions.visibility = View.GONE
            binding.cardMahasiswaStats.visibility = View.VISIBLE
        }

        // Re-initialize event adapter with role-specific permissions
        eventAdapter = EventAdapter(
            currentUserRole = user.role,
            onItemClick = { event ->
                val intent = Intent(this@MainActivity, EventDetailActivity::class.java)
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.id)
                startActivity(intent)
            },
            onEditClick = { event ->
                showEditEventDialog(event)
            },
            onDeleteClick = { event ->
                confirmDeleteEvent(event)
            }
        )
        binding.rvEvents.adapter = eventAdapter

        announcementAdapter = AnnouncementAdapter(
            currentUserRole = user.role,
            onDeleteClick = { announcement ->
                confirmDeleteAnnouncement(announcement)
            }
        )
        binding.rvAnnouncements.adapter = announcementAdapter

        filterEvents(binding.etSearchEvent.text?.toString().orEmpty())
    }

    private fun setupRecyclerViews() {
        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.cardHeaderAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnAbsenRapatMain.setOnClickListener {
            startActivity(Intent(this, AttendanceCameraActivity::class.java))
        }

        binding.btnAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        binding.btnAddAnnouncement.setOnClickListener {
            showAddAnnouncementDialog()
        }

        binding.etSearchEvent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                filterEvents(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on Home
                    true
                }
                R.id.nav_attendance -> {
                    if (currentUser?.role == UserRole.PENGURUS) {
                        startActivity(Intent(this, AttendanceCameraActivity::class.java))
                    } else {
                        Toast.makeText(this, "Fitur Presensi Rapat Kamera khusus untuk Core Team Pengurus HMTIKA.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repository.allEvents.collectLatest { events ->
                allEventsList = events
                binding.tvEventCountBadge.text = "${events.size} Kegiatan"
                binding.tvStatUpcomingEvents.text = events.filter { !it.isCompleted }.size.toString()
                filterEvents(binding.etSearchEvent.text?.toString().orEmpty())
            }
        }

        lifecycleScope.launch {
            repository.allAnnouncements.collectLatest { announcements ->
                binding.tvAnnouncementCountBadge.text = "${announcements.size} Info"
                announcementAdapter?.submitList(announcements)
            }
        }

        val nim = sessionManager.getLoggedInNim().orEmpty()
        lifecycleScope.launch {
            repository.getCertificatesForUser(nim).collectLatest { certs ->
                binding.tvStatCertificates.text = certs.size.toString()
            }
        }
    }

    private fun filterEvents(query: String) {
        val filtered = if (query.isBlank()) {
            allEventsList
        } else {
            allEventsList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true) ||
                it.speaker.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
        eventAdapter?.submitList(filtered)
    }

    private fun showAddEventDialog() {
        val dialogBinding = DialogAddEventBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvDialogTitle.text = "Tambah Event Baru"

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Simpan") { _, _ ->
                val title = dialogBinding.etDialogEventTitle.text?.toString().orEmpty().trim()
                val category = dialogBinding.etDialogEventCategory.text?.toString().orEmpty().trim()
                val date = dialogBinding.etDialogEventDate.text?.toString().orEmpty().trim()
                val time = dialogBinding.etDialogEventTime.text?.toString().orEmpty().trim()
                val location = dialogBinding.etDialogEventLocation.text?.toString().orEmpty().trim()
                val speaker = dialogBinding.etDialogEventSpeaker.text?.toString().orEmpty().trim()
                val quota = dialogBinding.etDialogEventQuota.text?.toString()?.toIntOrNull() ?: 50
                val desc = dialogBinding.etDialogEventDesc.text?.toString().orEmpty().trim()

                if (title.isBlank()) {
                    Toast.makeText(this, "Judul event tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val newEvent = Event(
                        title = title,
                        category = category.ifBlank { "Workshop" },
                        date = date.ifBlank { "15 November 2026" },
                        time = time.ifBlank { "09.00 - 15.00 WIB" },
                        location = location.ifBlank { "Lab Komputer STIMIK" },
                        speaker = speaker.ifBlank { "Tim Litbang HMTIKA" },
                        description = desc.ifBlank { "Kegiatan resmi himpunan." },
                        quota = quota,
                        registeredCount = 0,
                        bannerTheme = 1,
                        certificateAvailable = true
                    )
                    repository.createEvent(newEvent)
                    Toast.makeText(this@MainActivity, "Event '$title' berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditEventDialog(event: Event) {
        val dialogBinding = DialogAddEventBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tvDialogTitle.text = "Edit Event"
        dialogBinding.etDialogEventTitle.setText(event.title)
        dialogBinding.etDialogEventCategory.setText(event.category)
        dialogBinding.etDialogEventDate.setText(event.date)
        dialogBinding.etDialogEventTime.setText(event.time)
        dialogBinding.etDialogEventLocation.setText(event.location)
        dialogBinding.etDialogEventSpeaker.setText(event.speaker)
        dialogBinding.etDialogEventQuota.setText(event.quota.toString())
        dialogBinding.etDialogEventDesc.setText(event.description)

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Perbarui") { _, _ ->
                val title = dialogBinding.etDialogEventTitle.text?.toString().orEmpty().trim()
                val category = dialogBinding.etDialogEventCategory.text?.toString().orEmpty().trim()
                val date = dialogBinding.etDialogEventDate.text?.toString().orEmpty().trim()
                val time = dialogBinding.etDialogEventTime.text?.toString().orEmpty().trim()
                val location = dialogBinding.etDialogEventLocation.text?.toString().orEmpty().trim()
                val speaker = dialogBinding.etDialogEventSpeaker.text?.toString().orEmpty().trim()
                val quota = dialogBinding.etDialogEventQuota.text?.toString()?.toIntOrNull() ?: event.quota
                val desc = dialogBinding.etDialogEventDesc.text?.toString().orEmpty().trim()

                lifecycleScope.launch {
                    val updated = event.copy(
                        title = title.ifBlank { event.title },
                        category = category.ifBlank { event.category },
                        date = date.ifBlank { event.date },
                        time = time.ifBlank { event.time },
                        location = location.ifBlank { event.location },
                        speaker = speaker.ifBlank { event.speaker },
                        quota = quota,
                        description = desc.ifBlank { event.description }
                    )
                    repository.updateEvent(updated)
                    Toast.makeText(this@MainActivity, "Event berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteEvent(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Event")
            .setMessage("Apakah Anda yakin ingin menghapus '${event.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteEvent(event)
                    Toast.makeText(this@MainActivity, "Event dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAddAnnouncementDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "Tulis isi pengumuman/informasi pengurus..."
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Informasi Baru")
            .setView(input)
            .setPositiveButton("Publikasikan") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotBlank()) {
                    lifecycleScope.launch {
                        val ann = Announcement(
                            title = "Info Core Team: ${text.take(30)}...",
                            content = text,
                            author = currentUser?.name ?: "BPH HMTIKA",
                            date = "Baru saja",
                            priority = "INFO",
                            tag = "Pengurus"
                        )
                        repository.createAnnouncement(ann)
                        Toast.makeText(this@MainActivity, "Pengumuman berhasil dipublikasikan!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteAnnouncement(announcement: Announcement) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Informasi")
            .setMessage("Hapus pengumuman '${announcement.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    repository.deleteAnnouncement(announcement)
                    Toast.makeText(this@MainActivity, "Pengumuman dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
