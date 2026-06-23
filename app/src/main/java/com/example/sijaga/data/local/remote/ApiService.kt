package com.example.sijaga.data.local.remote

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("gangguan")
    suspend fun getGangguan(): Response<List<GangguanResponse>>
}