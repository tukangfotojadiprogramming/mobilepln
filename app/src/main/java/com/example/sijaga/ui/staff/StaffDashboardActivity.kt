package com.example.sijaga.ui.staff

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.databinding.ActivityStaffDashboardBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import com.example.sijaga.ui.auth.LoginActivity
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StaffDashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityStaffDashboardBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: GangguanAdapter

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
        session = SessionManager(this)
        b.tvNama.text = session.getNama()

        adapter = GangguanAdapter(emptyList()) {}
        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter

        loadStats()

        b.menuValidasiGangguan.setOnClickListener   { startActivity(Intent(this, ValidasiGangguanActivity::class.java))   }
        b.menuValidasiPasangBaru.setOnClickListener { startActivity(Intent(this, ValidasiPasangBaruActivity::class.java))  }
        b.menuStatistik.setOnClickListener          { startActivity(Intent(this, StatistikActivity::class.java))           }
        b.menuKelolaAkun.setOnClickListener         { startActivity(Intent(this, KelolaAkunActivity::class.java))          }
        b.tvLihatSemua.setOnClickListener           { startActivity(Intent(this, MonitoringActivity::class.java))          }
        b.navMonitoring.setOnClickListener          { startActivity(Intent(this, MonitoringActivity::class.java))          }
        b.navLogout.setOnClickListener              { logout() }
    }

    private fun loadStats() {
        val db = AppDatabase.getInstance(this)

        // Tarik data terbaru dari server saat dashboard dibuka
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getPasangBaru()
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    db.pasangBaruDao().deleteAll()
                    list.forEach { item ->
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
                // Gagal sync? Tenang, tetap pakai data lokal
            }
        }

        // Tampilkan statistik (kode asli kamu)
        lifecycleScope.launch {
            launch { db.gangguanDao().countBaru().collectLatest { n -> runOnUiThread {
                b.tvStatValidasi.text = n.toString()
                b.badgeGangguan.text = n.toString()
                b.badgeGangguan.visibility = if (n > 0) View.VISIBLE else View.GONE
            }}}
            launch { db.pasangBaruDao().countBaru().collectLatest { n -> runOnUiThread {
                b.badgePasangBaru.text = n.toString()
                b.badgePasangBaru.visibility = if (n > 0) View.VISIBLE else View.GONE
            }}}
            launch { db.gangguanDao().getByStatus("terverifikasi").collectLatest { list -> runOnUiThread { b.tvStatDiproses.text = list.size.toString() }}}
            launch { db.gangguanDao().getByStatus("selesai").collectLatest  { list -> runOnUiThread { b.tvStatSelesai.text  = list.size.toString() }}}
            launch { db.gangguanDao().getAll().collectLatest { list -> runOnUiThread {
                adapter.update(list.take(5))
                b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }}}
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
