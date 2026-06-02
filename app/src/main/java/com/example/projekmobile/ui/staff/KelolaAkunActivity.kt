package com.example.sijaga.ui.staff

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.R
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.User
import com.example.sijaga.databinding.ActivityKelolaAkunBinding
import com.example.sijaga.ui.adapter.UserAdapter
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KelolaAkunActivity : AppCompatActivity() {
    private lateinit var b: ActivityKelolaAkunBinding
    private lateinit var adapter: UserAdapter
    private var allUsers: List<User> = emptyList()
    private var filterRole = ""

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityKelolaAkunBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
        b.btnTambah.setOnClickListener { showDialog(null) }

        adapter = UserAdapter(emptyList(),
            onEdit  = { u -> showDialog(u) },
            onHapus = { u -> confirmHapus(u) }
        )
        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter

        b.tabSemua.setOnClickListener     { switchTab(b.tabSemua,     "") }
        b.tabStaff.setOnClickListener     { switchTab(b.tabStaff,     "staff_pln") }
        b.tabPelanggan.setOnClickListener { switchTab(b.tabPelanggan, "masyarakat") }

        b.etCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(e: Editable?) { applyFilter(e?.toString() ?: "") }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        loadData()
    }

    private fun switchTab(selected: TextView, role: String) {
        filterRole = role
        listOf(b.tabSemua, b.tabStaff, b.tabPelanggan).forEach { tab ->
            tab.setBackgroundColor(getColor(R.color.bg_card))
            tab.setTextColor(getColor(R.color.text_secondary))
            tab.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        selected.setBackgroundResource(R.drawable.bg_tab_selected_green)
        selected.setTextColor(getColor(R.color.color_staff))
        selected.setTypeface(null, android.graphics.Typeface.BOLD)
        applyFilter(b.etCari.text?.toString() ?: "")
    }

    private fun applyFilter(query: String) {
        val filtered = allUsers.filter { u ->
            (filterRole.isEmpty() || u.role == filterRole) &&
            (query.isEmpty() || u.nama.contains(query, true) || u.email.contains(query, true))
        }
        adapter.update(filtered)
        b.tvKosong.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadData() {
        b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            AppDatabase.getInstance(this@KelolaAkunActivity).userDao().getAll()
                .collectLatest { list ->
                    runOnUiThread {
                        b.progressBar.visibility = View.GONE
                        allUsers = list
                        applyFilter(b.etCari.text?.toString() ?: "")
                    }
                }
        }
    }

    private fun showDialog(existing: User?) {
        val view   = LayoutInflater.from(this).inflate(R.layout.dialog_tambah_user, null)
        val etNama = view.findViewById<TextInputEditText>(R.id.etNama)
        val etEmail= view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPass = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etTlp  = view.findViewById<TextInputEditText>(R.id.etTelepon)
        val rgRole = view.findViewById<RadioGroup>(R.id.rgRole)

        existing?.let {
            etNama.setText(it.nama); etEmail.setText(it.email); etTlp.setText(it.telepon)
            when (it.role) {
                "staff_pln" -> rgRole.check(R.id.rbStaff)
                else        -> rgRole.check(R.id.rbPelanggan)
            }
        }

        AlertDialog.Builder(this).setView(view).create().also { dialog ->
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBatal)
                .setOnClickListener { dialog.dismiss() }
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSimpan)
                .setOnClickListener {
                    val nama  = etNama.text?.toString()?.trim() ?: ""
                    val email = etEmail.text?.toString()?.trim() ?: ""
                    val pass  = etPass.text?.toString()?.trim() ?: ""
                    val tlp   = etTlp.text?.toString()?.trim() ?: ""
                    if (nama.isEmpty() || email.isEmpty()) {
                        Toast.makeText(this, "Nama dan email wajib diisi", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val role = when (rgRole.checkedRadioButtonId) {
                        R.id.rbStaff  -> "staff_pln"
                        else          -> "masyarakat"
                    }
                    lifecycleScope.launch {
                        val db = AppDatabase.getInstance(this@KelolaAkunActivity)
                        if (existing == null) {
                            if (pass.isEmpty()) { runOnUiThread { Toast.makeText(this@KelolaAkunActivity, "Password wajib diisi", Toast.LENGTH_SHORT).show() }; return@launch }
                            db.userDao().insert(User(nama=nama, email=email, password=pass, role=role, telepon=tlp))
                            runOnUiThread { Toast.makeText(this@KelolaAkunActivity, "✅ Akun ditambahkan", Toast.LENGTH_SHORT).show() }
                        } else {
                            db.userDao().update(existing.copy(nama=nama, email=email,
                                password=if (pass.isNotEmpty()) pass else existing.password,
                                role=role, telepon=tlp))
                            runOnUiThread { Toast.makeText(this@KelolaAkunActivity, "✅ Akun diperbarui", Toast.LENGTH_SHORT).show() }
                        }
                        runOnUiThread { dialog.dismiss() }
                    }
                }
            dialog.show()
        }
    }

    private fun confirmHapus(u: User) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun")
            .setMessage("Hapus akun ${u.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.getInstance(this@KelolaAkunActivity).userDao().delete(u)
                    runOnUiThread { Toast.makeText(this@KelolaAkunActivity, "Akun dihapus", Toast.LENGTH_SHORT).show() }
                }
            }
            .setNegativeButton("Batal", null).show()
    }
}
