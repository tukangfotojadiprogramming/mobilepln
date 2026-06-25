package com.example.sijaga.ui.adapter

import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import android.widget.TextView; import androidx.recyclerview.widget.RecyclerView
import com.example.sijaga.R; import com.example.sijaga.data.local.entity.PasangBaru
import java.text.SimpleDateFormat; import java.util.*

class ValidasiPasangBaruAdapter(
    private var list: List<PasangBaru>,
    private val onSetujui: (PasangBaru) -> Unit,
    private val onTolak:   (PasangBaru) -> Unit
) : RecyclerView.Adapter<ValidasiPasangBaruAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvJudul:   TextView = v.findViewById(R.id.tvJudul)
        val tvPelapor: TextView = v.findViewById(R.id.tvPelapor)
        val tvStatus:  TextView = v.findViewById(R.id.tvStatus)
        val tvLokasi:  TextView = v.findViewById(R.id.tvLokasi)
        val tvTanggal: TextView = v.findViewById(R.id.tvTanggal)
        val btnSetujui: com.google.android.material.button.MaterialButton = v.findViewById(R.id.btnSetujui)
        val btnTolak:   com.google.android.material.button.MaterialButton = v.findViewById(R.id.btnTolak)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_validasi_card, p, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val p = list[i]

        h.tvJudul.text   = "Pasang Baru – ${p.daya} VA"
        h.tvPelapor.text = "Oleh: ${p.nama}"
        h.tvLokasi.text  = "📍 ${p.alamat}"
        h.tvTanggal.text = "🕒 ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")).format(Date(p.createdAt))}"

        val (label, color, bg) = when(p.status) {
            "baru" -> Triple(
                "Baru",
                R.color.status_baru,
                R.color.status_baru_bg
            )

            "terverifikasi" -> Triple(
                "Terverifikasi",
                R.color.status_terverifikasi,
                R.color.status_terverifikasi_bg
            )

            "ditolak" -> Triple(
                "Ditolak",
                R.color.status_ditolak,
                R.color.status_ditolak_bg
            )

            else -> Triple(
                p.status.replaceFirstChar { it.uppercase() },
                R.color.text_secondary,
                R.color.divider
            )
        }

        h.tvStatus.text = label
        h.tvStatus.setTextColor(h.itemView.context.getColor(color))
        h.tvStatus.setBackgroundColor(h.itemView.context.getColor(bg))

        // Tampilkan tombol hanya untuk status BARU
        if (p.status == "baru") {
            h.btnSetujui.visibility = View.VISIBLE
            h.btnTolak.visibility = View.VISIBLE

            h.btnSetujui.setOnClickListener {
                onSetujui(p)
            }

            h.btnTolak.setOnClickListener {
                onTolak(p)
            }
        } else {
            h.btnSetujui.visibility = View.GONE
            h.btnTolak.visibility = View.GONE
        }
    }

    fun update(newList: List<PasangBaru>) { list = newList; notifyDataSetChanged() }
}
