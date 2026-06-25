package com.example.sijaga.ui.customer

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sijaga.R
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.Gangguan
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.databinding.ActivityStatusPengajuanBinding
import com.example.sijaga.ui.adapter.GangguanAdapter
import com.example.sijaga.ui.adapter.PasangBaruAdapter
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // Inisialisasi adapter
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

        // Jalankan pemantauan lokal
        loadGangguanOffline()
        loadPasangBaruOffline()

        // Ambil data dari server
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
            loadGangguan()
        } else {
            b.tabPasangBaru.setBackgroundResource(R.drawable.bg_tab_selected)
            b.tabPasangBaru.setTextColor(getColor(R.color.sijaga_primary))
            b.tabPasangBaru.setTypeface(null, android.graphics.Typeface.BOLD)
            b.tabGangguan.setBackgroundColor(getColor(R.color.bg_card))
            b.tabGangguan.setTextColor(getColor(R.color.text_secondary))
            b.tabGangguan.setTypeface(null, android.graphics.Typeface.NORMAL)
            b.swipeGangguan.visibility  = View.GONE
            b.swipePasangBaru.visibility = View.VISIBLE
            loadPasangBaru()
        }
    }

    private fun loadGangguan() {
        b.progressBar.visibility = View.VISIBLE
        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            try {
                // Ambil data gangguan dari server Vercel
                val response = withContext(Dispatchers.IO) { ApiClient.instance.getGangguan() }

                if (response.isSuccessful) {
                    val remoteList = response.body() ?: emptyList()
                    var tempId = 1
                    val mappedList = remoteList.map { res ->
                        Gangguan(
                            id = res.id?.hashCode() ?: tempId++,
                            userId = session.getUserId(),
                            namaPelapor = res.namaPelapor ?: "Anonim",
                            telepon = "",
                            idPelanggan = "",
                            jenis = res.jenis ?: "",
                            deskripsi = res.deskripsi ?: "",
                            alamat = res.alamat ?: "",
                            fotoPath = res.fotoPath ?: "",
                            status = res.status ?: "baru",
                            createdAt = res.createdAt ?: System.currentTimeMillis()
                        )
                    }

                    // Simpan ke database lokal
                    withContext(Dispatchers.IO) {
                        db.gangguanDao().deleteAll()
                        mappedList.forEach { db.gangguanDao().insertOrUpdate(it) }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error Gangguan: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    b.progressBar.visibility = View.GONE
                    b.swipeGangguan.isRefreshing = false
                }
            }
        }
    }

    private fun loadGangguanOffline() {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            // Ambil data gangguan dari database lokal
            db.gangguanDao().getAll().collectLatest { list ->
                withContext(Dispatchers.Main) {
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
        b.progressBar.visibility = View.VISIBLE
        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            try {
                // Ambil data pasang baru dari server Vercel
                val response = withContext(Dispatchers.IO) { ApiClient.instance.getPasangBaru() }
                if (response.isSuccessful) {
                    val remoteList = response.body() ?: emptyList()
                    var tempId = 1
                    val mappedList = remoteList.map { res ->
                        PasangBaru(
                            id = res.id?.hashCode() ?: tempId++,
                            userId = session.getUserId(),
                            nama = res.nama ?: "",
                            nik = res.nik ?: "",
                            telepon = res.telepon ?: "",
                            alamat = res.alamat ?: "",
                            daya = res.daya ?: 0,
                            status = res.status ?: "baru",
                            createdAt = res.createdAt ?: System.currentTimeMillis()
                        )
                    }

                    // Simpan ke database lokal
                    withContext(Dispatchers.IO) {
                        // Pastikan di PasangBaruDao juga ada query DELETE untuk clear cache jika dibutuhkan
                        mappedList.forEach { db.pasangBaruDao().update(it) }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error Pasang Baru: ${e.message}")
            } finally {
                withContext(Dispatchers.Main) {
                    b.progressBar.visibility = View.GONE
                    b.swipePasangBaru.isRefreshing = false
                }
            }
        }
    }

    private fun loadPasangBaruOffline() {
        val db = AppDatabase.getInstance(this)
        lifecycleScope.launch {
            // Ambil data pasang baru dari database lokal
            db.pasangBaruDao().getAll().collectLatest { list ->
                withContext(Dispatchers.Main) {
                    b.progressBar.visibility = View.GONE
                    b.swipePasangBaru.isRefreshing = false
                    pasangBaruAdapter.update(list)
                    if (currentTab == 1)
                        b.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}