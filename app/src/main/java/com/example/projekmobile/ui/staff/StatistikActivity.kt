package com.example.sijaga.ui.staff

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.databinding.ActivityStatistikBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatistikActivity : AppCompatActivity() {
    private lateinit var b: ActivityStatistikBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityStatistikBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            launch { db.gangguanDao().countAll().collectLatest      { runOnUiThread { b.tvTotalGangguan.text = it.toString() } } }
            launch { db.gangguanDao().getByStatus("terverifikasi").collectLatest { runOnUiThread { b.tvDiproses.text = it.size.toString() } } }
            launch { db.gangguanDao().getByStatus("ditolak").collectLatest  { runOnUiThread { b.tvDitolak.text  = it.size.toString() } } }
        }
    }
}
