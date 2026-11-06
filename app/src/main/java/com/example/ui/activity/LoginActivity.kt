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
import com.example.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: HmtikaRepository
    private lateinit var sessionManager: SessionManager

    private var selectedRole: UserRole = UserRole.MAHASISWA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = HmtikaRepository(this)
        sessionManager = SessionManager(this)

        setupRoleTabs()
        setupListeners()
    }

    private fun setupRoleTabs() {
        binding.tabMahasiswa.setOnClickListener {
            selectRole(UserRole.MAHASISWA)
        }

        binding.tabPengurus.setOnClickListener {
            selectRole(UserRole.PENGURUS)
        }
    }

    private fun selectRole(role: UserRole) {
        selectedRole = role
        binding.tvLoginError.visibility = View.GONE

        if (role == UserRole.MAHASISWA) {
            binding.tabMahasiswa.setBackgroundResource(R.drawable.bg_role_tab_selected)
            binding.tabMahasiswa.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabPengurus.setBackgroundResource(R.drawable.bg_role_tab_unselected)
            binding.tabPengurus.setTextColor(ContextCompat.getColor(this, R.color.hmtika_text_secondary))

            binding.tilNimEmail.placeholderText = "Contoh: 2301010042"
        } else {
            binding.tabPengurus.setBackgroundResource(R.drawable.bg_role_tab_selected)
            binding.tabPengurus.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabMahasiswa.setBackgroundResource(R.drawable.bg_role_tab_unselected)
            binding.tabMahasiswa.setTextColor(ContextCompat.getColor(this, R.color.hmtika_text_secondary))

            binding.tilNimEmail.placeholderText = "Contoh: 2201010007"
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val identifier = binding.etNimEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()

            if (identifier.isBlank()) {
                binding.tilNimEmail.error = "NIM atau Email Kampus wajib diisi"
                return@setOnClickListener
            } else {
                binding.tilNimEmail.error = null
            }

            if (password.isBlank()) {
                binding.tilPassword.error = "Password wajib diisi"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            performLogin(identifier, password)
        }

        // Demo login buttons
        binding.btnDemoMahasiswa.setOnClickListener {
            selectRole(UserRole.MAHASISWA)
            binding.etNimEmail.setText("2301010042")
            binding.etPassword.setText("password123")
            performLogin("2301010042", "password123")
        }

        binding.btnDemoPengurus.setOnClickListener {
            selectRole(UserRole.PENGURUS)
            binding.etNimEmail.setText("2201010007")
            binding.etPassword.setText("pengurus123")
            performLogin("2201010007", "pengurus123")
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(identifier: String, pass: String) {
        binding.btnLogin.isEnabled = false
        binding.pbLoginLoading.visibility = View.VISIBLE
        binding.tvLoginError.visibility = View.GONE

        lifecycleScope.launch {
            val result = repository.login(identifier, pass, selectedRole)
            binding.btnLogin.isEnabled = true
            binding.pbLoginLoading.visibility = View.GONE

            result.onSuccess { user ->
                sessionManager.saveUserSession(user.nim, user.role)
                Toast.makeText(this@LoginActivity, "Selamat datang, ${user.name}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure { exception ->
                binding.tvLoginError.text = exception.message ?: "Terjadi kesalahan saat masuk."
                binding.tvLoginError.visibility = View.VISIBLE
            }
        }
    }
}
