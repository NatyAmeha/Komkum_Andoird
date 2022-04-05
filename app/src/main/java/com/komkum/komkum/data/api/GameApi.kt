package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.Trivia
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GameApi {
    @GET("trivias")
    suspend fun getQuestion(@Query("ad") adId : String) : Response<List<Trivia>>
}