package com.example.sijaga.data.local.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // KELOMPOK GANGGUAN

    @POST("api/gangguan")
    suspend fun postGangguan(
        @Body request: GangguanRequest
    ): Response<GangguanResponse>

    @GET("api/gangguan")
    suspend fun getGangguan(): Response<List<GangguanResponse>>

    @PUT("api/gangguan/{id}")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): Response<GangguanResponse>

    // KELOMPOK PASANG BARU

    @POST("api/pasang-baru")
    suspend fun postPasangBaru(
        @Body request: PasangBaruRequest
    ): Response<PasangBaruResponse>

    @GET("api/pasang-baru")
    suspend fun getPasangBaru(): Response<List<PasangBaruResponse>>

    @PUT("api/pasang-baru/{id}")
    suspend fun updateStatusPasangBaru(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): Response<PasangBaruResponse>
}