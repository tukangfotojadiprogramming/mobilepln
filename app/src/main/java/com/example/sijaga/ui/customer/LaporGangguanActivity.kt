package com.example.sijaga.ui.customer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
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
        val jenis   = b.etJenis.text.toString().trim()
        val desk    = b.etDeskripsi.text.toString().trim()
        val alamat  = b.etAlamat.text.toString().trim()
        val telepon = b.etTelepon.text.toString().trim()

        if (jenis.isEmpty() || desk.isEmpty() || alamat.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data yang wajib", Toast.LENGTH_SHORT).show()
            return
        }

        b.progressBar.visibility = View.VISIBLE
        b.btnKirim.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. UBAH & RESIZE FOTO KE BASE64
                val base64Foto = withContext(Dispatchers.IO) {
                    if (fotoPath.isNotEmpty()) {
                        val file = File(fotoPath)
                        if (file.exists()) {
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeFile(fotoPath, options)
                            
                            val targetSize = 800
                            var inSampleSize = 1
                            if (options.outHeight > targetSize || options.outWidth > targetSize) {
                                val halfHeight = options.outHeight / 2
                                val halfWidth = options.outWidth / 2
                                while (halfHeight / inSampleSize >= targetSize && halfWidth / inSampleSize >= targetSize) {
                                    inSampleSize *= 2
                                }
                            }

                            options.inJustDecodeBounds = false
                            options.inSampleSize = inSampleSize
                            val bitmap = BitmapFactory.decodeFile(fotoPath, options)
                            
                            val scaledBitmap = if (bitmap.width > targetSize || bitmap.height > targetSize) {
                                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                val width = if (aspectRatio > 1) targetSize else (targetSize * aspectRatio).toInt()
                                val height = if (aspectRatio > 1) (targetSize / aspectRatio).toInt() else targetSize
                                Bitmap.createScaledBitmap(bitmap, width, height, true)
                            } else {
                                bitmap
                            }

                            val outputStream = ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
                            val bytes = outputStream.toByteArray()
                            Base64.encodeToString(bytes, Base64.NO_WRAP)
                        } else ""
                    } else ""
                }

                val g = Gangguan(
                    userId = session.getUserId(),
                    namaPelapor = session.getNama(),
                    telepon = telepon,
                    jenis = jenis,
                    deskripsi = desk,
                    alamat = alamat,
                    fotoPath = base64Foto 
                )

                // 2. KIRIM KE MOCKAPI
                val response = ApiClient.instance.postGangguan(g)

                if (response.isSuccessful) {
                    val gLokal = g.copy(fotoPath = fotoPath)
                    AppDatabase.getInstance(this@LaporGangguanActivity).gangguanDao().insert(gLokal)

                    runOnUiThread {
                        b.progressBar.visibility = View.GONE
                        Toast.makeText(this@LaporGangguanActivity, "✅ Berhasil terkirim!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        b.progressBar.visibility = View.GONE
                        b.btnKirim.isEnabled = true
                        Toast.makeText(this@LaporGangguanActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    b.progressBar.visibility = View.GONE
                    b.btnKirim.isEnabled = true
                    Toast.makeText(this@LaporGangguanActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
