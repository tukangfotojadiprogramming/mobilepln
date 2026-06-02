package com.example.sijaga.ui.customer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.databinding.ActivityCustomerDashboardBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import com.example.sijaga.ui.auth.LoginActivity
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomerDashboardActivity : AppCompatActivity() {
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
        lifecycleScope.launch {
            AppDatabase.getInstance(this@CustomerDashboardActivity)
                .gangguanDao().getByUser(uid).collectLatest { list ->
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
