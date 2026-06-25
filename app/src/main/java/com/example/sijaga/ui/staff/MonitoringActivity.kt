package com.example.sijaga.ui.staff

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.Gangguan
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.databinding.ActivityMonitoringBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        loadDataOffline()
        loadData()
    }

    private fun loadData() {
        b.progressBar.visibility = View.VISIBLE
        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { ApiClient.instance.getGangguan() }

                if (response.isSuccessful) {
                    val remoteList = response.body() ?: emptyList()

                    // LANGSUNG GUNAKAN ID DARI SERVER (res.id), jangan pakai tempId++
                    val mappedList = remoteList.map { res ->
                        Gangguan(
                            id = res.id?.hashCode() ?: 0, // Gunakan hash ID server sebagai ID lokal
                            userId = 0,
                            namaPelapor = res.namaPelapor ?: "Anonim",
                            telepon = "",
                            idPelanggan = "",
                            jenis = res.jenis ?: "Lainnya",
                            deskripsi = res.deskripsi ?: "",
                            alamat = res.alamat ?: "",
                            fotoPath = res.fotoPath ?: "",
                            status = res.status ?: "baru",
                            createdAt = res.createdAt ?: System.currentTimeMillis()
                        )
                    }

                    withContext(Dispatchers.IO) {
                        // Gunakan transaksi untuk menghapus dan mengisi agar data selalu fresh
                        db.gangguanDao().deleteAll()
                        db.gangguanDao().insertAll(mappedList)
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Monitoring Error: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    b.progressBar.visibility = View.GONE
                    b.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun loadDataOffline() {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            // Ambil data dari database lokal
            db.gangguanDao().getAll().collectLatest { list ->
                withContext(Dispatchers.Main) {
                    b.progressBar.visibility = View.GONE
                    b.swipeRefresh.isRefreshing = false

                    adapter.update(list)
                    b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

                    // Hitung statistik angka
                    b.tvTotal.text = list.size.toString()
                    b.tvDiproses.text = list.count {
                        it.status.equals("diproses", ignoreCase = true) ||
                                it.status.equals("terverifikasi", ignoreCase = true)
                    }.toString()
                    b.tvSelesai.text = list.count { it.status.equals("selesai", ignoreCase = true) }.toString()
                }
            }
        }
    }
}