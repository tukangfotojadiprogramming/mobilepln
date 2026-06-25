package com.example.sijaga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val email: String,
    val password: String,
    val role: String, // masyarakat | staff_pln
    val telepon: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gangguan")
data class Gangguan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val namaPelapor: String,
    val telepon: String,
    val idPelanggan: String,
    val jenis: String,
    val deskripsi: String,
    val alamat: String,
    val fotoPath: String = "",
    val status: String = "baru", // baru | diproses | selesai | ditolak
    val catatanStaff: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pasang_baru")
data class PasangBaru(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val serverId: String = "",

    val userId: Int,
    val nama: String,
    val nik: String,
    val telepon: String,
    val alamat: String,
    val daya: Int,
    val status: String = "baru",
    val catatanStaff: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
