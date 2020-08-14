package com.example.ethiomusic.data.api

import com.example.ethiomusic.data.model.Radio
import retrofit2.Response
import retrofit2.http.*


interface RadioApi {
    @GET("radio/featured")
    suspend fun getFeaturedRadio() : Response<List<Radio>>

    @GET("radio/popular")
    suspend fun getPopularRadio() : Response<List<Radio>>

    @GET("radio/{id}")
    suspend fun getRadioStation(@Path("id")radioId : String) : Response<Radio>

    @GET("radio/c/{genre}")
    suspend fun getRadioByGenre(@Path("genre")  genre : String) : Response<List<Radio>>

    @GET("radio/browse")
    suspend fun getBrowseString() : Response<List<String>>

    @PUT("radio/like/{id}")
    suspend fun likeRadio(@Path("id") radioId : String) : Response<Boolean>

    @PUT("radio/unlike/{id}")
    suspend fun unlikeRadio(@Path("id") radioId : String) : Response<Boolean>

    @PUT("radio/isliked/{id}")
    suspend fun isradioLIked(@Path("id") radioId : String) : Response<Boolean>

    @PUT("radio/update")
    suspend fun updateRadio(@Body radioData: Radio) : Response<Boolean>



    @GET("radio/me")
    suspend fun getUserRadio() : Response<List<Radio>>

    @POST("radio/create")
    suspend fun createRadio(@Body radioData : Radio) : Response<Radio>
}