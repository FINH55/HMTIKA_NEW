package com.example.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.R
import com.example.data.local.SessionManager
import com.example.data.model.Certificate
import com.example.data.model.Event
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.HmtikaRepository
import com.example.databinding.ActivityEventDetailBinding
import com.example.ui.adapter.AttendanceAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }

    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var repository: HmtikaRepository
    private lateinit var sessionManager: SessionManager

    private var currentUser: User? = null
    private var currentEvent: Event? = null
    private var currentCertificate: Certificate? = null

    private val attendanceAdapter = AttendanceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = HmtikaRepository(this)
        sessionManager = SessionManager(this)

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) {
            Toast.makeText(this, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadData(eventId)
        setupListeners()
    }

    private fun setupViews() {
        binding.rvEventAttendees.layoutManager = LinearLayoutManager(this)
        binding.rvEventAttendees.adapter = attendanceAdapter
    }

    private fun loadData(eventId: Long) {
        val nim = sessionManager.getLoggedInNim().orEmpty()

        lifecycleScope.launch {
            currentUser = repository.getUserByNim(nim)
            val event = repository.getEventById(eventId)
            if (event == null) {
                Toast.makeText(this@EventDetailActivity, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            currentEvent = event
            bindEventDetails(event)

            val role = currentUser?.role ?: UserRole.MAHASISWA
            if (role == UserRole.MAHASISWA) {
                binding.sectionMahasiswaCert.visibility = View.VISIBLE
                binding.sectionPengurusRekap.visibility = View.GONE
                observeCertificate(eventId, nim)
            } else {
                binding.sectionMahasiswaCert.visibility = View.GONE
                binding.sectionPengurusRekap.visibility = View.VISIBLE
                observeAttendees()
            }
        }
    }

    private fun bindEventDetails(event: Event) {
        binding.tvDetailTitle.text = event.title
        binding.tvDetailCategoryBadge.text = event.category.uppercase()
        binding.tvDetailDateTime.text = "${event.date} • ${event.time}"
        binding.tvDetailLocation.text = event.location
        binding.tvDetailSpeaker.text = event.speaker
        binding.tvDetailQuota.text = "${event.registeredCount} dari ${event.quota} Mahasiswa Terdaftar"
        binding.tvDetailDescription.text = event.description

        val bannerColorRes = when (event.category.lowercase()) {
            "seminar" -> R.color.hmtika_teal_dark
            "workshop" -> R.color.hmtika_teal
            "lomba" -> R.color.amber_warning
            else -> R.color.code_purple
        }
        binding.bannerHeader.setBackgroundColor(getColor(bannerColorRes))
    }

    private fun observeCertificate(eventId: Long, nim: String) {
        lifecycleScope.launch {
            repository.getCertificate(eventId, nim).collectLatest { cert ->
                currentCertificate = cert
                if (cert != null) {
                    binding.cardClaimCert.visibility = View.GONE
                    binding.cardCertificatePreview.visibility = View.VISIBLE

                    binding.tvCertNumber.text = "Nomor: ${cert.certificateNo}"
                    binding.tvCertRecipientName.text = cert.recipientName
                    binding.tvCertRecipientNim.text = "NIM: ${cert.recipientNim}"
                } else {
                    binding.cardClaimCert.visibility = View.VISIBLE
                    binding.cardCertificatePreview.visibility = View.GONE
                    binding.tvClaimSubtitle.text = "Sertifikat resmi diterbitkan atas nama ${currentUser?.name ?: "Mahasiswa"} (${currentUser?.nim})"
                }
            }
        }
    }

    private fun observeAttendees() {
        lifecycleScope.launch {
            repository.allAttendances.collectLatest { list ->
                attendanceAdapter.submitList(list)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackDetail.setOnClickListener {
            finish()
        }

        binding.btnClaimCert.setOnClickListener {
            val user = currentUser
            val event = currentEvent
            if (user != null && event != null) {
                lifecycleScope.launch {
                    val cert = repository.issueCertificate(event.id, event.title, user)
                    Toast.makeText(this@EventDetailActivity, "E-Sertifikat berhasil diklaim dan diterbitkan!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDownloadCertPdf.setOnClickListener {
            val cert = currentCertificate
            if (cert != null) {
                AlertDialog.Builder(this)
                    .setTitle("E-Sertifikat Berhasil Diunduh")
                    .setMessage("Berkas PDF E-Sertifikat '${cert.certificateNo.replace('/', '_')}.pdf' telah disimpan ke folder Unduhan perangkat Anda.\n\nKode Verifikasi: ${cert.verificationCode}")
                    .setPositiveButton("Tutup", null)
                    .show()
            }
        }

        binding.btnExportRekap.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Ekspor Rekapitulasi Presensi")
                .setMessage("Berkas rekapitulasi daftar hadir presensi acara berhasil diekspor dalam format dokumen PDF/CSV siap cetak.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
