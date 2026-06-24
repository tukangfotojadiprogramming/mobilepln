package com.example.sijaga.ui.customer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.data.local.remote.PasangBaruRequest
import com.example.sijaga.databinding.ActivityPasangBaruBinding
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        if (nama.isEmpty() || nik.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        b.progressBar.visibility = View.VISIBLE
        b.btnKirim.isEnabled = false

        val request = PasangBaruRequest(
            userId = session.getUserId(),
            nama = nama,
            nik = nik,
            telepon = tlp,
            alamat = alamat,
            daya = daya
        )

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { ApiClient.instance.postPasangBaru(request) }

                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null) {
                        // Tipe data didefinisikan eksplisit untuk menghindari error infer
                        val pLokal = PasangBaru(
                            id = res.id?.hashCode() ?: 0,
                            userId = res.userId ?: session.getUserId(),
                            nama = res.nama ?: nama,
                            nik = res.nik ?: nik,
                            telepon = res.telepon ?: tlp,
                            alamat = res.alamat ?: alamat,
                            daya = res.daya ?: daya,
                            status = res.status ?: "baru"
                        )

                        val db = AppDatabase.getInstance(this@PasangBaruActivity)
                        withContext(Dispatchers.IO) {
                            db.pasangBaruDao().insertOrUpdate(pLokal)
                        }
                    }

                    Toast.makeText(this@PasangBaruActivity, "✅ Pengajuan berhasil!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val msg = "Server Error (${response.code()})"
                    Log.e("API_ERROR", msg)
                    Toast.makeText(this@PasangBaruActivity, msg, Toast.LENGTH_SHORT).show()
                    b.btnKirim.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
                Toast.makeText(this@PasangBaruActivity, "Koneksi Bermasalah", Toast.LENGTH_SHORT).show()
                b.btnKirim.isEnabled = true
            } finally {
                b.progressBar.visibility = View.GONE
            }
        }
    }
}