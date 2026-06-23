package com.example.sijaga.ui.staff

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.databinding.ActivityMonitoringBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MonitoringActivity : AppCompatActivity() {
    private lateinit var b: ActivityMonitoringBinding
    private lateinit var adapter: GangguanAdapter

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnBack.setOnClickListener { finish() }

        adapter = GangguanAdapter(emptyList()) {}
        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter

        b.swipeRefresh.setOnRefreshListener { loadData() }

        loadData()
    }

    private fun loadData() {
        b.progressBar.visibility = View.VISIBLE

        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            launch {
                db.gangguanDao().countAll().collectLatest {
                    b.tvTotal.text = it.toString()
                }
            }

            launch {
                db.gangguanDao().getByStatus("terverifikasi").collectLatest {
                    b.tvDiproses.text = it.size.toString()
                }
            }

            launch {
                db.gangguanDao().getByStatus("selesai").collectLatest {
                    b.tvSelesai.text = it.size.toString()
                }
            }

            launch {
                db.gangguanDao().getAll().collectLatest { list ->
                    b.progressBar.visibility = View.GONE
                    b.swipeRefresh.isRefreshing = false
                    adapter.update(list)
                    b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}