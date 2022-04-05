package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.data.model.Trivia
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdApi {

    @PUT("ad/{id}/update_impression")
    suspend fun updateAdImpression(@Path("id")adsId : String) : Response<Boolean>

    @PUT("ad/{id}/update_click")
    suspend fun updateAdClick(@Path("id")adsId : String) : Response<Boolean>

    @GET("games")
    suspend fun getGameAds(@Query("limit") limit : Int) : Response<List<Ads>>

    @GET("trivias")
    suspend fun getQuestion(@Query("ad") adId : String) : Response<List<Trivia>>
}