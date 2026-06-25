package com.example.sijaga.ui.adapter

import android.graphics.BitmapFactory
import android.util.Log
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
        Log.d("FOTO_DEBUG", "Jenis: ${g.jenis}")
        Log.d("FOTO_DEBUG", "FotoPath: ${g.fotoPath.take(100)}")

        h.tvIkon.text    = "⚠️"
        h.tvJudul.text   = g.jenis
        h.tvLokasi.text  = "📍 ${g.alamat}"
        h.tvTanggal.text = "🕒 ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id")).format(java.util.Date(g.createdAt))}"

        // Menampilkan Foto (Base64)
        if (g.fotoPath.isNotEmpty()) {

            try {

                h.ivFoto.visibility = View.VISIBLE

                val pureBase64 = g.fotoPath.substringAfter(",")

                val imageBytes = Base64.decode(
                    pureBase64,
                    Base64.DEFAULT
                )

                val bitmap = BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.size
                )

                h.ivFoto.setImageBitmap(bitmap)

            } catch (e: Exception) {

                Log.e(
                    "FOTO_ERROR",
                    e.message ?: "Unknown Error"
                )

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
