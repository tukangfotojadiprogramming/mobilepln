package com.example.sijaga.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.sijaga.R
import com.example.sijaga.data.local.entity.PasangBaru

class PasangBaruAdapter(
    private var list: List<PasangBaru>,
    private val onClick: (PasangBaru) -> Unit
) : RecyclerView.Adapter<PasangBaruAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: CardView     = v.findViewById(R.id.cardRoot)
        val tvIkon: TextView   = v.findViewById(R.id.tvIkon)
        val tvJudul: TextView  = v.findViewById(R.id.tvJudul)
        val tvLokasi: TextView = v.findViewById(R.id.tvLokasi)
        val tvTanggal: TextView= v.findViewById(R.id.tvTanggal)
        val tvStatus: TextView = v.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_laporan_card, p, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val p = list[i]
        h.tvIkon.text    = "🔌"
        h.tvJudul.text   = "Pasang Baru – ${p.daya} VA"
        h.tvLokasi.text  = "📍 ${p.alamat}"
        h.tvTanggal.text = "🕒 ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id")).format(java.util.Date(p.createdAt))}"
        val (label, color, bg) = when(p.status) {
            "baru"          -> Triple("Baru",          h.itemView.context.getColor(R.color.status_baru),          h.itemView.context.getColor(R.color.status_baru_bg))
            "terverifikasi" -> Triple("Terverifikasi", h.itemView.context.getColor(R.color.status_terverifikasi), h.itemView.context.getColor(R.color.status_terverifikasi_bg))
            "selesai"       -> Triple("Selesai",       h.itemView.context.getColor(R.color.status_selesai),       h.itemView.context.getColor(R.color.status_selesai_bg))
            "ditolak"       -> Triple("Ditolak",       h.itemView.context.getColor(R.color.status_ditolak),       h.itemView.context.getColor(R.color.status_ditolak_bg))
            else            -> Triple(p.status,        h.itemView.context.getColor(R.color.text_secondary),       h.itemView.context.getColor(R.color.divider))
        }
        h.tvStatus.text = label; h.tvStatus.setTextColor(color); h.tvStatus.setBackgroundColor(bg)
        h.card.setOnClickListener { onClick(p) }
    }

    fun update(newList: List<PasangBaru>) { list = newList; notifyDataSetChanged() }
}
