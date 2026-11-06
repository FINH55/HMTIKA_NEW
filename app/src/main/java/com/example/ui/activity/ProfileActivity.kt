package com.example.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.R
import com.example.data.local.SessionManager
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.HmtikaRepository
import com.example.databinding.ActivityProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var repository: HmtikaRepository
    private lateinit var sessionManager: SessionManager

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = HmtikaRepository(this)
        sessionManager = SessionManager(this)

        val nim = sessionManager.getLoggedInNim()
        if (nim == null) {
            finish()
            return
        }

        loadUserData(nim)
        setupListeners()
    }

    private fun loadUserData(nim: String) {
        lifecycleScope.launch {
            val user = repository.getUserByNim(nim)
            if (user == null) {
                finish()
                return@launch
            }
            currentUser = user
            bindUserProfile(user)
        }

        lifecycleScope.launch {
            repository.getCertificatesForUser(nim).collectLatest { certs ->
                binding.tvStatCertCount.text = certs.size.toString()
                if (currentUser?.role == UserRole.MAHASISWA) {
                    binding.tvStatAttendCount.text = (certs.size + 2).toString()
                }
            }
        }

        lifecycleScope.launch {
            repository.allAttendances.collectLatest { list ->
                if (currentUser?.role == UserRole.PENGURUS) {
                    binding.tvStatAttendCount.text = list.size.toString()
                }
            }
        }
    }

    private fun bindUserProfile(user: User) {
        binding.tvProfileAvatarEmoji.text = user.avatarEmoji
        binding.tvProfileName.text = user.name
        binding.tvProfileDivision.text = user.division

        if (user.role == UserRole.PENGURUS) {
            binding.tvProfileRoleBadge.text = "⚡ PENGURUS CORE TEAM"
            binding.tvProfileRoleBadge.setBackgroundResource(R.drawable.bg_badge_amber)
            binding.tvProfileRoleBadge.setTextColor(getColor(R.color.amber_warning))
            binding.tvStatAttendLabel.text = "Rapat"
        } else {
            binding.tvProfileRoleBadge.text = "🎓 MAHASISWA AKTIF"
            binding.tvProfileRoleBadge.setBackgroundResource(R.drawable.bg_badge_teal)
            binding.tvProfileRoleBadge.setTextColor(getColor(R.color.hmtika_teal))
            binding.tvStatAttendLabel.text = "Kegiatan"
        }

        binding.tvStatSemester.text = user.semester.toString()
        binding.tvInfoNim.text = user.nim
        binding.tvInfoEmail.text = user.email
    }

    private fun setupListeners() {
        binding.btnBackProfile.setOnClickListener {
            finish()
        }

        binding.cardProfileAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showAvatarSelectionDialog() {
        val emojis = arrayOf("👨‍💻", "👩‍💻", "⚡", "🎓", "🚀", "💡", "🤖", "🔥")
        AlertDialog.Builder(this)
            .setTitle("Pilih Avatar Emoji")
            .setItems(emojis) { _, which ->
                val selected = emojis[which]
                binding.tvProfileAvatarEmoji.text = selected
                val user = currentUser
                if (user != null) {
                    val updated = user.copy(avatarEmoji = selected)
                    currentUser = updated
                    lifecycleScope.launch {
                        repository.updateUser(updated)
                        Toast.makeText(this@ProfileActivity, "Avatar berhasil diubah!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_confirm_title))
            .setMessage(getString(R.string.logout_confirm_message))
            .setPositiveButton("Keluar") { _, _ ->
                sessionManager.clearSession()
                Toast.makeText(this, "Anda telah keluar.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
