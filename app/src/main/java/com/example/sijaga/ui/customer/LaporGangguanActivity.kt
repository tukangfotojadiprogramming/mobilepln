package com.example.sijaga.ui.customer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.Gangguan
import com.example.sijaga.data.local.remote.ApiClient
import com.example.sijaga.data.local.remote.GangguanRequest
import com.example.sijaga.databinding.ActivityLaporGangguanBinding
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LaporGangguanActivity : AppCompatActivity() {
    private lateinit var b: ActivityLaporGangguanBinding
    private lateinit var session: SessionManager
    private var fotoUri: Uri? = null
    private var fotoPath: String = ""

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && fotoUri != null) {
            b.ivFoto.setImageURI(fotoUri)
            b.ivFoto.visibility = View.VISIBLE
            b.layoutFotoPlaceholder.visibility = View.GONE
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) bukaKamera() else Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityLaporGangguanBinding.inflate(layoutInflater)
        setContentView(b.root)
        session = SessionManager(this)
        b.btnBack.setOnClickListener { finish() }

        b.layoutFotoPlaceholder.setOnClickListener { requestCamera() }
        b.btnFoto.setOnClickListener { requestCamera() }
        b.ivFoto.setOnClickListener  { requestCamera() }

        b.btnKirim.setOnClickListener { kirim() }
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bukaKamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun bukaKamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fotoFile  = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SIJAGA_$timestamp.jpg")
        fotoPath = fotoFile.absolutePath
        fotoUri  = FileProvider.getUriForFile(this, "${packageName}.provider", fotoFile)
        cameraLauncher.launch(fotoUri!!)
    }

    private fun kirim() {
        val jenis       = b.etJenis.text.toString().trim()
        val desk        = b.etDeskripsi.text.toString().trim()
        val alamat      = b.etAlamat.text.toString().trim()
        val telepon     = b.etTelepon.text.toString().trim()
        val idPelanggan = b.etIdPelanggan.text.toString().trim()


        if (jenis.isEmpty() || desk.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        b.progressBar.visibility = View.VISIBLE
        b.btnKirim.isEnabled = false

        lifecycleScope.launch {
            try {
                // Kompresi foto ke Base64
                val base64Foto = withContext(Dispatchers.IO) {
                    if (fotoPath.isNotEmpty()) {
                        val file = File(fotoPath)
                        if (file.exists()) {
                            val options = BitmapFactory.Options().apply { inSampleSize = 8 }
                            val bitmap = BitmapFactory.decodeFile(fotoPath, options)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
                            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                        } else null
                    } else null
                }

                val request = GangguanRequest(
                    userId = session.getUserId(),
                    namaPelapor = session.getNama(),
                    telepon = telepon,
                    idPelanggan = idPelanggan,
                    jenis = jenis,
                    deskripsi = desk,
                    alamat = alamat,
                    fotoPath = base64Foto
                )

                // Kirim ke server
                val response = withContext(Dispatchers.IO) { ApiClient.instance.postGangguan(request) }

                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null) {
                        // Simpan ke database lokal
                        val gLokal = Gangguan(
                            id = res.id?.hashCode() ?: 0, // Gunakan hash server agar konsisten
                            userId = session.getUserId(),
                            namaPelapor = res.namaPelapor ?: session.getNama(),
                            telepon = telepon,
                            idPelanggan = idPelanggan,
                            jenis = res.jenis ?: jenis,
                            deskripsi = res.deskripsi ?: desk,
                            alamat = res.alamat ?: alamat,
                            fotoPath = base64Foto ?: "",
                            status = res.status ?: "baru",
                            createdAt = System.currentTimeMillis()
                        )
                        withContext(Dispatchers.IO) {
                            AppDatabase.getInstance(this@LaporGangguanActivity).gangguanDao().insertOrUpdate(gLokal)
                        }
                    }

                    Toast.makeText(this@LaporGangguanActivity, "✅ Laporan terkirim!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@LaporGangguanActivity, "Gagal kirim: ${response.code()}", Toast.LENGTH_SHORT).show()
                    b.btnKirim.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@LaporGangguanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                b.btnKirim.isEnabled = true
            } finally {
                b.progressBar.visibility = View.GONE
            }
        }
    }
}