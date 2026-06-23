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
import com.example.sijaga.data.local.entity.Gangguan
import com.example.sijaga.databinding.ActivityValidasiListBinding
import com.example.sijaga.ui.adapter.ValidasiGangguanAdapter
import com.example.sijaga.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ValidasiGangguanActivity : AppCompatActivity() {
    private lateinit var b: ActivityValidasiListBinding
    private lateinit var adapter: ValidasiGangguanAdapter
    private var currentFilter = ""

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityValidasiListBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.tvTitle.text = "Validasi Gangguan"
        b.btnBack.setOnClickListener { finish() }

        adapter = ValidasiGangguanAdapter(emptyList(),
            onSetujui = { g -> updateStatus(g, Constants.STATUS_TERVERIFIKASI) },
            onTolak   = { g -> updateStatus(g, Constants.STATUS_DITOLAK)       }
        )
        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { loadData(currentFilter) }

        b.tabSemua.setOnClickListener        { switchTab(b.tabSemua, "") }
        b.tabBaru.setOnClickListener         { switchTab(b.tabBaru, Constants.STATUS_BARU) }
        b.tabDiproses.setOnClickListener     { switchTab(b.tabDiproses, Constants.STATUS_TERVERIFIKASI) }
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
            val db = AppDatabase.getInstance(this@ValidasiGangguanActivity)
            val flow = if (filter.isEmpty()) db.gangguanDao().getAll()
                       else db.gangguanDao().getByStatus(filter)
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

    private fun updateStatus(g: Gangguan, status: String) {
        lifecycleScope.launch {
            AppDatabase.getInstance(this@ValidasiGangguanActivity)
                .gangguanDao().update(g.copy(status = status, updatedAt = System.currentTimeMillis()))
            runOnUiThread {
                Toast.makeText(this@ValidasiGangguanActivity,
                    if (status == Constants.STATUS_TERVERIFIKASI) "✅ Laporan terverifikasi" else "❌ Laporan ditolak",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
