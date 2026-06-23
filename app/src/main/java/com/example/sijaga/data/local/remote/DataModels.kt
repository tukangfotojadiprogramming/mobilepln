package com.example.sijaga.data.local.remote

import com.google.gson.annotations.SerializedName

data class GangguanResponse(
    @SerializedName("id") val id: String,
    @SerializedName("namaPelapor") val namaPelapor: String?,
    @SerializedName("jenis") val jenis: String?,
    @SerializedName("deskripsi") val deskripsi: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("createdAt") val createdAt: Long?
)