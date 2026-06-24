package com.example.sijaga.data.local.remote

import com.example.sijaga.data.local.entity.Gangguan
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("gangguan")
    suspend fun postGangguan(@Body gangguan: Gangguan): Response<Gangguan>

    @GET("gangguan")
    suspend fun getGangguan(): Response<List<GangguanResponse>>
}
