package com.example.sijaga.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.databinding.ActivityLoginBinding
import com.example.sijaga.ui.customer.CustomerDashboardActivity
import com.example.sijaga.ui.staff.StaffDashboardActivity
import com.example.sijaga.utils.Constants
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        session = SessionManager(this)

        lifecycleScope.launch {
            AppDatabase.seedIfEmpty(AppDatabase.getInstance(this@LoginActivity))
        }

        if (session.isLoggedIn()) {
            navigateByRole(session.getRole())
            return
        }

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.text?.toString()?.trim() ?: ""
            val pass  = b.etPassword.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) { Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (pass.isEmpty())  { Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            doLogin(email, pass)
        }
    }

    private fun doLogin(email: String, pass: String) {
        b.progressBar.visibility = View.VISIBLE
        b.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val db   = AppDatabase.getInstance(this@LoginActivity)
            val user = db.userDao().login(email, pass)
            runOnUiThread {
                b.progressBar.visibility = View.GONE
                b.btnLogin.isEnabled = true
                if (user != null) {
                    session.saveSession(user.id, user.nama, user.email, user.role)
                    navigateByRole(user.role)
                } else {
                    Toast.makeText(this@LoginActivity,
                        "Login gagal!\n\nAkun demo:\npelanggan@sijaga.com / sijaga123\nstaff@sijaga.com / sijaga123",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateByRole(role: String) {
        val dest = when (role) {
            Constants.ROLE_CUSTOMER -> CustomerDashboardActivity::class.java
            else                    -> StaffDashboardActivity::class.java
        }
        startActivity(Intent(this, dest).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
