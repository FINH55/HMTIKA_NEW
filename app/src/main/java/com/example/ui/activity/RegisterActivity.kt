package com.example.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.R
import com.example.data.local.SessionManager
import com.example.data.model.UserRole
import com.example.data.repository.HmtikaRepository
import com.example.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: HmtikaRepository
    private lateinit var sessionManager: SessionManager

    private var selectedRole: UserRole = UserRole.MAHASISWA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = HmtikaRepository(this)
        sessionManager = SessionManager(this)

        setupRoleTabs()
        setupListeners()
    }

    private fun setupRoleTabs() {
        binding.tabRegMahasiswa.setOnClickListener {
            selectRole(UserRole.MAHASISWA)
        }

        binding.tabRegPengurus.setOnClickListener {
            selectRole(UserRole.PENGURUS)
        }
    }

    private fun selectRole(role: UserRole) {
        selectedRole = role
        binding.tvRegError.visibility = View.GONE

        if (role == UserRole.MAHASISWA) {
            binding.tabRegMahasiswa.setBackgroundResource(R.drawable.bg_role_tab_selected)
            binding.tabRegMahasiswa.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabRegPengurus.setBackgroundResource(R.drawable.bg_role_tab_unselected)
            binding.tabRegPengurus.setTextColor(ContextCompat.getColor(this, R.color.hmtika_text_secondary))

            binding.tilRegPin.visibility = View.GONE
        } else {
            binding.tabRegPengurus.setBackgroundResource(R.drawable.bg_role_tab_selected)
            binding.tabRegPengurus.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabRegMahasiswa.setBackgroundResource(R.drawable.bg_role_tab_unselected)
            binding.tabRegMahasiswa.setTextColor(ContextCompat.getColor(this, R.color.hmtika_text_secondary))

            binding.tilRegPin.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etRegName.text?.toString().orEmpty().trim()
            val nim = binding.etRegNim.text?.toString().orEmpty().trim()
            val email = binding.etRegEmail.text?.toString().orEmpty().trim()
            val password = binding.etRegPassword.text?.toString().orEmpty()
            val confirmPassword = binding.etRegConfirmPassword.text?.toString().orEmpty()
            val pin = binding.etRegPin.text?.toString().orEmpty().trim()

            if (name.isBlank() || nim.isBlank() || email.isBlank() || password.isBlank()) {
                showError("Semua data wajib diisi secara lengkap.")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showError("Konfirmasi password tidak cocok dengan kata sandi.")
                return@setOnClickListener
            }

            if (selectedRole == UserRole.PENGURUS && pin != "TIKA2026") {
                showError("Kode PIN Verifikasi Pengurus tidak valid. Gunakan PIN: TIKA2026")
                return@setOnClickListener
            }

            performRegister(nim, name, email, password, pin)
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun showError(msg: String) {
        binding.tvRegError.text = msg
        binding.tvRegError.visibility = View.VISIBLE
    }

    private fun performRegister(nim: String, name: String, email: String, pass: String, pin: String) {
        binding.btnRegister.isEnabled = false
        binding.pbRegLoading.visibility = View.VISIBLE
        binding.tvRegError.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.register(nim, name, email, pass, selectedRole, pin)
            binding.btnRegister.isEnabled = true
            binding.pbRegLoading.visibility = View.GONE

            result.onSuccess { user ->
                sessionManager.saveUserSession(user.nim, user.role)
                Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil! Selamat datang, ${user.name}.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { exception ->
                showError(exception.message ?: "Gagal membuat akun.")
            }
        }
    }
}
