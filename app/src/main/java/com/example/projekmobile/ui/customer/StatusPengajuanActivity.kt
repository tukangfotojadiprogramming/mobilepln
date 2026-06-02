package com.example.sijaga.ui.customer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.R
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.databinding.ActivityStatusPengajuanBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import com.example.sijaga.ui.adapter.PasangBaruAdapter
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatusPengajuanActivity : AppCompatActivity() {
    private lateinit var b: ActivityStatusPengajuanBinding
    private lateinit var session: SessionManager
    private lateinit var gangguanAdapter: GangguanAdapter
    private lateinit var pasangBaruAdapter: PasangBaruAdapter
    private var currentTab = 0

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityStatusPengajuanBinding.inflate(layoutInflater)
        setContentView(b.root)
        session = SessionManager(this)
        b.btnBack.setOnClickListener { finish() }

        gangguanAdapter  = GangguanAdapter(emptyList()) {}
        pasangBaruAdapter = PasangBaruAdapter(emptyList()) {}

        b.rvGangguan.layoutManager  = LinearLayoutManager(this)
        b.rvGangguan.adapter        = gangguanAdapter
        b.rvPasangBaru.layoutManager = LinearLayoutManager(this)
        b.rvPasangBaru.adapter       = pasangBaruAdapter

        b.swipeGangguan.setOnRefreshListener  { loadGangguan() }
        b.swipePasangBaru.setOnRefreshListener { loadPasangBaru() }

        b.tabGangguan.setOnClickListener  { switchTab(0) }
        b.tabPasangBaru.setOnClickListener { switchTab(1) }

        loadGangguan()
        loadPasangBaru()
    }

    private fun switchTab(tab: Int) {
        currentTab = tab
        if (tab == 0) {
            b.tabGangguan.setBackgroundResource(R.drawable.bg_tab_selected)
            b.tabGangguan.setTextColor(getColor(R.color.sijaga_primary))
            b.tabGangguan.setTypeface(null, android.graphics.Typeface.BOLD)
            b.tabPasangBaru.setBackgroundColor(getColor(R.color.bg_card))
            b.tabPasangBaru.setTextColor(getColor(R.color.text_secondary))
            b.tabPasangBaru.setTypeface(null, android.graphics.Typeface.NORMAL)
            b.swipeGangguan.visibility  = View.VISIBLE
            b.swipePasangBaru.visibility = View.GONE
        } else {
            b.tabPasangBaru.setBackgroundResource(R.drawable.bg_tab_selected)
            b.tabPasangBaru.setTextColor(getColor(R.color.sijaga_primary))
            b.tabPasangBaru.setTypeface(null, android.graphics.Typeface.BOLD)
            b.tabGangguan.setBackgroundColor(getColor(R.color.bg_card))
            b.tabGangguan.setTextColor(getColor(R.color.text_secondary))
            b.tabGangguan.setTypeface(null, android.graphics.Typeface.NORMAL)
            b.swipeGangguan.visibility  = View.GONE
            b.swipePasangBaru.visibility = View.VISIBLE
        }
    }

    private fun loadGangguan() {
        b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            AppDatabase.getInstance(this@StatusPengajuanActivity)
                .gangguanDao().getByUser(session.getUserId()).collectLatest { list ->
                    runOnUiThread {
                        b.progressBar.visibility = View.GONE
                        b.swipeGangguan.isRefreshing = false
                        gangguanAdapter.update(list)
                        if (currentTab == 0)
                            b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
        }
    }

    private fun loadPasangBaru() {
        lifecycleScope.launch {
            AppDatabase.getInstance(this@StatusPengajuanActivity)
                .pasangBaruDao().getByUser(session.getUserId()).collectLatest { list ->
                    runOnUiThread {
                        b.swipePasangBaru.isRefreshing = false
                        pasangBaruAdapter.update(list)
                        if (currentTab == 1)
                            b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
        }
    }
}
