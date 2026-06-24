package com.example.sijaga.ui.staff

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.R
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.databinding.ActivityValidasiListBinding
import com.example.sijaga.ui.adapter.ValidasiPasangBaruAdapter
import com.example.sijaga.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ValidasiPasangBaruActivity : AppCompatActivity() {
    private lateinit var b: ActivityValidasiListBinding
    private lateinit var adapter: ValidasiPasangBaruAdapter
    private var currentFilter = ""

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityValidasiListBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.tvTitle.text = "Validasi Pasang Baru"
        b.btnBack.setOnClickListener { finish() }

        adapter = ValidasiPasangBaruAdapter(emptyList(),
            onSetujui = { p -> updateStatus(p, Constants.STATUS_TERVERIFIKASI) },
            onTolak   = { p -> updateStatus(p, Constants.STATUS_DITOLAK)       }
        )
        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { loadData(currentFilter) }

        b.tabSemua.setOnClickListener    { switchTab(b.tabSemua, "") }
        b.tabBaru.setOnClickListener     { switchTab(b.tabBaru, Constants.STATUS_BARU) }
        b.tabDiproses.setOnClickListener { switchTab(b.tabDiproses, Constants.STATUS_TERVERIFIKASI) }
        b.tabDiproses.text = "Terverifikasi"
        loadData("")
    }

    private fun switchTab(selected: TextView, filter: String) {
        currentFilter = filter
        listOf(b.tabSemua, b.tabBaru, b.tabDiproses).forEach { tab ->
            tab.setBackgroundColor(getColor(R.color.bg_card))
            tab.setTextColor(getColor(R.color.text_secondary))
            tab.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        selected.setBackgroundResource(R.drawable.bg_tab_selected_green)
        selected.setTextColor(getColor(R.color.color_staff))
        selected.setTypeface(null, android.graphics.Typeface.BOLD)
        loadData(filter)
    }

    private fun loadData(filter: String) {
        b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ValidasiPasangBaruActivity)
            val flow = if (filter.isEmpty()) db.pasangBaruDao().getAll()
                       else db.pasangBaruDao().getByStatus(filter)
            flow.collectLatest { list ->
                runOnUiThread {
                    b.progressBar.visibility = View.GONE
                    b.swipeRefresh.isRefreshing = false
                    adapter.update(list)
                    b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun updateStatus(p: PasangBaru, status: String) {
        b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // 1. Kirim update ke server
                val response = ApiClient.instance.updateStatusPasangBaru(p.id.toString(), mapOf("status" to status))

                if (response.isSuccessful) {
                    // 2. Jika sukses di server, baru update di database lokal
                    AppDatabase.getInstance(this@ValidasiPasangBaruActivity)
                        .pasangBaruDao().update(p.copy(status = status, updatedAt = System.currentTimeMillis()))

                    runOnUiThread {
                        Toast.makeText(this@ValidasiPasangBaruActivity, "✅ Status berhasil diupdate", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    throw Exception("Server merespon: ${response.code()}")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ValidasiPasangBaruActivity, "❌ Gagal update: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                b.progressBar.visibility = View.GONE
            }
        }
    }
}
