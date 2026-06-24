package com.example.sijaga

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.PasangBaru
import com.example.sijaga.data.local.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // sync function
        syncDataFromServer()
    }

    private fun syncDataFromServer() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { ApiClient.instance.getPasangBaru() }

                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    val db = AppDatabase.getInstance(this@MainActivity)

                    withContext(Dispatchers.IO) {
                        db.pasangBaruDao().deleteAll()

                        // Lakukan konversi (mapping) agar tipe data cocok dengan Entity Room
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}