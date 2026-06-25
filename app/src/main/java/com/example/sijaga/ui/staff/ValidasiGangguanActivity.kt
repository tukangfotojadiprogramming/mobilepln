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
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.databinding.ActivityValidasiListBinding
import com.example.sijaga.ui.adapter.ValidasiGangguanAdapter
import com.example.sijaga.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ValidasiGangguanActivity : AppCompatActivity() {
    private lateinit var b: ActivityValidasiListBinding
    private lateinit var adapter: ValidasiGangguanAdapter
    private var currentFilter = ""

    // Map untuk memetakan ID lokal (Int) ke ID MongoDB (String)
    private val remoteIdMap = HashMap<Int, String>()

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityValidasiListBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.tvTitle.text = "Validasi Gangguan"
        b.btnBack.setOnClickListener { finish() }

        // Setup Adapter
        adapter = ValidasiGangguanAdapter(emptyList(),
            onSetujui = { g -> updateStatusOnline(g, Constants.STATUS_TERVERIFIKASI) },
            onTolak   = { g -> updateStatusOnline(g, Constants.STATUS_DITOLAK)       }
        )

        b.rvData.layoutManager = LinearLayoutManager(this)
        b.rvData.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { loadData(currentFilter) }

        // Setup Tab navigasi
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
            try {
                // Ambil data dari server
                val response = ApiClient.instance.getGangguan()
                if (response.isSuccessful) {
                    val remoteList = response.body() ?: emptyList()
                    remoteIdMap.clear()

                    var tempLocalId = 1
                    val mappedList = remoteList.map { res ->
                        val mongoId = res.id ?: ""
                        remoteIdMap[tempLocalId] = mongoId

                        val entity = Gangguan(
                            id = tempLocalId++,
                            userId = 0,
                            namaPelapor = res.namaPelapor ?: "",
                            telepon = "",
                            idPelanggan = "",
                            jenis = res.jenis ?: "",
                            deskripsi = res.deskripsi ?: "",
                            alamat = res.alamat ?: "",
                            fotoPath = res.fotoPath ?: "",
                            status = res.status ?: "baru",
                            createdAt = res.createdAt ?: System.currentTimeMillis()
                        )
                        // Simpan ke DB lokal
                        AppDatabase.getInstance(this@ValidasiGangguanActivity)
                            .gangguanDao().insertOrUpdate(entity)
                        entity
                    }

                    val filteredList = if (filter.isEmpty()) mappedList else mappedList.filter { it.status == filter }
                    runOnUiThread {
                        b.progressBar.visibility = View.GONE
                        b.swipeRefresh.isRefreshing = false
                        adapter.update(filteredList)
                        b.tvKosong.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                    }
                } else {
                    loadDataOffline(filter)
                }
            } catch (e: Exception) {
                loadDataOffline(filter)
            }
        }
    }

    private fun loadDataOffline(filter: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ValidasiGangguanActivity)
            val flow = if (filter.isEmpty()) db.gangguanDao().getAll() else db.gangguanDao().getByStatus(filter)

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

    private fun updateStatusOnline(g: Gangguan, status: String) {
        val mongoId = remoteIdMap[g.id]
        if (mongoId.isNullOrEmpty()) {
            Toast.makeText(this, "Data ID tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Update status di server
                val response = ApiClient.instance.updateStatus(mongoId, mapOf("status" to status))
                if (response.isSuccessful) {
                    // Update status di DB lokal
                    AppDatabase.getInstance(this@ValidasiGangguanActivity)
                        .gangguanDao().update(g.copy(status = status, updatedAt = System.currentTimeMillis()))

                    Toast.makeText(this@ValidasiGangguanActivity, "Status diperbarui", Toast.LENGTH_SHORT).show()
                    loadData(currentFilter)
                } else {
                    Toast.makeText(this@ValidasiGangguanActivity, "Gagal update server", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ValidasiGangguanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                b.progressBar.visibility = View.GONE
            }
        }
    }
}