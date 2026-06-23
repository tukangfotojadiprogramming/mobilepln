package com.example.sijaga.ui.customer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.databinding.ActivityPasangBaruBinding
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.launch

class PasangBaruActivity : AppCompatActivity() {
    private lateinit var b: ActivityPasangBaruBinding
    private lateinit var session: SessionManager

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityPasangBaruBinding.inflate(layoutInflater)
        setContentView(b.root)
        session = SessionManager(this)
        b.btnBack.setOnClickListener { finish() }
        b.btnKirim.setOnClickListener { kirim() }
    }

    private fun kirim() {
        val nama   = b.etNama.text.toString().trim()
        val nik    = b.etNik.text.toString().trim()
        val tlp    = b.etTelepon.text.toString().trim()
        val alamat = b.etAlamat.text.toString().trim()
        val daya   = b.etDaya.text.toString().trim().toIntOrNull() ?: 0
        if (nama.isEmpty() || nik.isEmpty() || alamat.isEmpty()) { Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show(); return }
        b.progressBar.visibility = View.VISIBLE; b.btnKirim.isEnabled = false
        val p = PasangBaru(userId = session.getUserId(), nama = nama, nik = nik, telepon = tlp, alamat = alamat, daya = daya)
        lifecycleScope.launch {
            AppDatabase.getInstance(this@PasangBaruActivity).pasangBaruDao().insert(p)
            runOnUiThread { Toast.makeText(this@PasangBaruActivity, "✅ Pengajuan terkirim!", Toast.LENGTH_LONG).show(); finish() }
        }
    }
}
