package com.example.sijaga.ui.customer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.sijaga.data.local.AppDatabase
import com.example.sijaga.data.local.entity.Gangguan
import com.example.sijaga.databinding.ActivityLaporGangguanBinding
import com.example.sijaga.utils.SessionManager
import kotlinx.coroutines.launch
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

        // Tap foto area atau tombol → langsung buka kamera
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
        cameraLauncher.launch(fotoUri)
    }

    private fun kirim() {
        val jenis   = b.etJenis.text.toString().trim()
        val desk    = b.etDeskripsi.text.toString().trim()
        val alamat  = b.etAlamat.text.toString().trim()
        val telepon = b.etTelepon.text.toString().trim()
        if (jenis.isEmpty() || desk.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data yang wajib", Toast.LENGTH_SHORT).show(); return
        }
        b.progressBar.visibility = View.VISIBLE
        b.btnKirim.isEnabled = false
        val g = Gangguan(
            userId = session.getUserId(), namaPelapor = session.getNama(),
            telepon = telepon, jenis = jenis, deskripsi = desk,
            alamat = alamat, fotoPath = fotoPath
        )
        lifecycleScope.launch {
            AppDatabase.getInstance(this@LaporGangguanActivity).gangguanDao().insert(g)
            runOnUiThread {
                Toast.makeText(this@LaporGangguanActivity, "✅ Laporan berhasil dikirim!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
