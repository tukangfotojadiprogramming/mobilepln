package com.example.sijaga.ui.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.sijaga.R
import com.example.sijaga.data.local.entity.Gangguan
import java.io.File

class GangguanAdapter(
    private var list: List<Gangguan>,
    private val onClick: (Gangguan) -> Unit
) : RecyclerView.Adapter<GangguanAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: CardView     = v.findViewById(R.id.cardRoot)
        val tvIkon: TextView   = v.findViewById(R.id.tvIkon)
        val tvJudul: TextView  = v.findViewById(R.id.tvJudul)
        val tvLokasi: TextView = v.findViewById(R.id.tvLokasi)
        val tvTanggal: TextView= v.findViewById(R.id.tvTanggal)
        val tvStatus: TextView = v.findViewById(R.id.tvStatus)
        val ivFoto: ImageView  = v.findViewById(R.id.ivFoto)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_laporan_card, p, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val g = list[i]
        h.tvIkon.text    = "⚠️"
        h.tvJudul.text   = g.jenis
        h.tvLokasi.text  = "📍 ${g.alamat}"
        h.tvTanggal.text = "🕒 ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id")).format(java.util.Date(g.createdAt))}"
        
        // Menampilkan Foto (Base64 atau Local Path)
        if (g.fotoPath.isNotEmpty()) {
            h.ivFoto.visibility = View.VISIBLE
            try {
                if (g.fotoPath.startsWith("/")) {
                    // Path Lokal (HP Pengirim)
                    h.ivFoto.setImageURI(Uri.fromFile(File(g.fotoPath)))
                } else {
                    // Base64 String (HP Penerima dari Server)
                    val imageBytes = Base64.decode(g.fotoPath, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    h.ivFoto.setImageBitmap(decodedImage)
                }
            } catch (e: Exception) {
                h.ivFoto.visibility = View.GONE
            }
        } else {
            h.ivFoto.visibility = View.GONE
        }

        val (label, color, bg) = statusStyle(g.status, h.itemView.context)
        h.tvStatus.text = label
        h.tvStatus.setTextColor(color)
        h.tvStatus.setBackgroundColor(bg)
        h.card.setOnClickListener { onClick(g) }
    }

    fun update(newList: List<Gangguan>) { list = newList; notifyDataSetChanged() }

    private fun statusStyle(s: String, ctx: android.content.Context): Triple<String, Int, Int> {
        val res = ctx.resources
        return when(s) {
            "baru"          -> Triple("Baru",          res.getColor(R.color.status_baru,          null), res.getColor(R.color.status_baru_bg,          null))
            "terverifikasi" -> Triple("Terverifikasi", res.getColor(R.color.status_terverifikasi, null), res.getColor(R.color.status_terverifikasi_bg, null))
            "selesai"       -> Triple("Selesai",       res.getColor(R.color.status_selesai,       null), res.getColor(R.color.status_selesai_bg,       null))
            "ditolak"       -> Triple("Ditolak",       res.getColor(R.color.status_ditolak,       null), res.getColor(R.color.status_ditolak_bg,       null))
            else            -> Triple(s,               res.getColor(R.color.text_secondary,       null), res.getColor(R.color.divider,                 null))
        }
    }
}
