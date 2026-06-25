package com.example.sijaga.ui.customer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.databinding.ActivityCustomerDashboardBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import com.example.sijaga.ui.auth.LoginActivity
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class  CustomerDashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityCustomerDashboardBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: GangguanAdapter

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityCustomerDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
        session = SessionManager(this)
        b.tvNama.text = session.getNama()

        adapter = GangguanAdapter(emptyList()) { _ ->
            startActivity(Intent(this, StatusPengajuanActivity::class.java))
        }
        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter

        loadStats()

        b.menuLaporGangguan.setOnClickListener { startActivity(Intent(this, LaporGangguanActivity::class.java)) }
        b.menuPasangBaru.setOnClickListener    { startActivity(Intent(this, PasangBaruActivity::class.java))    }
        b.tvLihatSemua.setOnClickListener      { startActivity(Intent(this, StatusPengajuanActivity::class.java)) }

        b.navStatus.setOnClickListener  { startActivity(Intent(this, StatusPengajuanActivity::class.java)) }
        b.navLogout.setOnClickListener  { logout() }
    }

    private fun loadStats() {
        val uid = session.getUserId()
        val db = AppDatabase.getInstance(this@CustomerDashboardActivity)

        lifecycleScope.launch {
            // Update data dari server ke database lokal
            try {
                val response = ApiClient.instance.getPasangBaru()
                if (response.isSuccessful) {
                    // Saring data berdasarkan ID user
                    val list = response.body() ?: emptyList()
                    val userList = list.filter { it.userId == uid }

                    // Hapus data lama dan simpan data baru hasil konversi
                    db.pasangBaruDao().deleteAll()
                    userList.forEach { item ->
                        val entity = PasangBaru(
                            id = item.id.toIntOrNull() ?: 0,
                            userId = item.userId ?: 0,
                            nama = item.nama ?: "",
                            nik = item.nik ?: "",
                            telepon = item.telepon ?: "",
                            alamat = item.alamat ?: "",
                            daya = item.daya ?: 0,
                            status = item.status ?: "baru"
                        )
                        db.pasangBaruDao().insert(entity)
                    }
                }
            } catch (e: Exception) {
                // Abaikan jika gagal; tetap tampilkan data lokal
            }

            // Tampilkan statistik gangguan di dashboard
            db.gangguanDao().getByUser(uid).collectLatest { list ->
                runOnUiThread {
                    b.tvLaporanAktif.text = list.count { it.status != "selesai" && it.status != "ditolak" }.toString()
                    b.tvSelesai.text      = list.count { it.status == "selesai" }.toString()
                    b.tvDiproses.text     = list.count { it.status == "terverifikasi" }.toString()
                    adapter.update(list.take(5))
                    b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun logout() {
        session.logout()
        startActivity(Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
