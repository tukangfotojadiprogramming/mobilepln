package com.example.sijaga.data.local.remote

import com.google.gson.annotations.SerializedName

data class GangguanResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: Int?,
    @SerializedName("namaPelapor") val namaPelapor: String?,
    @SerializedName("telepon") val telepon: String?,
    @SerializedName("jenis") val jenis: String?,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("fotoPath") val fotoPath: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: Long?
)

data class PasangBaruResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: Int?,
    @SerializedName("nama") val nama: String?,
    @SerializedName("nik") val nik: String?,
    @SerializedName("telepon") val telepon: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("daya") val daya: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: Long?
)

data class GangguanRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("namaPelapor") val namaPelapor: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("idPelanggan") val idPelanggan: String,
    @SerializedName("jenis") val jenis: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("alamat") val alamat: String,
    @SerializedName("fotoPath") val fotoPath: String?,
    @SerializedName("status") val status: String = "baru"
)

data class PasangBaruRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("nik") val nik: String,
    @SerializedName("telepon") val telepon: String,
    @SerializedName("alamat") val alamat: String,
    @SerializedName("daya") val daya: Int,
    @SerializedName("status") val status: String = "baru"
)
