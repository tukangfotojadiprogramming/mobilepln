package com.example.sijaga.ui.adapter

import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import android.widget.ImageButton; import android.widget.TextView; import androidx.recyclerview.widget.RecyclerView
import com.example.sijaga.R; import com.example.sijaga.data.local.entity.User

class UserAdapter(
    private var list: List<User>,
    private val onEdit:  (User) -> Unit,
    private val onHapus: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvInisial: TextView    = v.findViewById(R.id.tvInisial)
        val tvNama:    TextView    = v.findViewById(R.id.tvNama)
        val tvEmail:   TextView    = v.findViewById(R.id.tvEmail)
        val tvRole:    TextView    = v.findViewById(R.id.tvRole)
        val btnEdit:   ImageButton = v.findViewById(R.id.btnEdit)
        val btnHapus:  ImageButton = v.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_akun_card, p, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, i: Int) {
        val u = list[i]
        h.tvInisial.text = u.nama.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        h.tvNama.text    = u.nama
        h.tvEmail.text   = u.email
        val (roleLabel, roleColor) = when(u.role) {
            "staff_pln"   -> Pair("Staff PLN",  h.itemView.context.getColor(R.color.color_staff))
            else          -> Pair("Pelanggan",  h.itemView.context.getColor(R.color.sijaga_primary))
        }
        h.tvRole.text = roleLabel; h.tvRole.setTextColor(roleColor)
        h.btnEdit.setOnClickListener  { onEdit(u)  }
        h.btnHapus.setOnClickListener { onHapus(u) }
    }

    fun update(newList: List<User>) { list = newList; notifyDataSetChanged() }
}
