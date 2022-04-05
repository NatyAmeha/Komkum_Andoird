package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.Radio
import com.komkum.komkum.data.model.RadioLikeInfo
import com.komkum.komkum.data.model.UserRadioInfo
import retrofit2.Response
import retrofit2.http.*


interface RadioApi {
    @GET("radio/featured")
    suspend fun getFeaturedRadio() : Response<List<Radio>>

    @GET("radio/browse/{category}")
    suspend fun getRadiobyCategory(@Path("category")cateory : String) : Response<List<Radio>>

    @GET("radio/popular")
    suspend fun getPopularRadio() : Response<List<Radio>>

    @GET("radio/{id}")
    suspend fun getRadioStation(@Path("id")radioId : String) : Response<Radio>

    @GET("radio/c/{genre}")
    suspend fun getRadioByGenre(@Path("genre")  genre : String) : Response<List<Radio>>

    @GET("radio/browse")
    suspend fun getBrowseString() : Response<List<String>>

    @PUT("radio/like")
    suspend fun likeRadio(@Body radioLikeInfo: RadioLikeInfo) : Response<String>

    @PUT("radio/unlike/{id}")
    suspend fun unlikeRadio(@Path("id") radioId : String) : Response<Boolean>

    @PUT("radio/isliked/{id}")
    suspend fun isradioLIked(@Path("id") radioId : String) : Response<Boolean>

    @PUT("radio/update")
    suspend fun updateRadio(@Body radioData: UserRadioInfo) : Response<Boolean>

    @GET("radio/song/{id}")
    suspend fun getRadioFromSong(@Path("id")songId : String) : Response<Radio>

    @GET("radio/artist/{id}")
    suspend fun getRadioFromArtist(@Path("id")artistId : String) : Response<Radio>

    @GET("radio/me")
    suspend fun getUserRadio() : Response<List<Radio>>

    @POST("radio/create")
    suspend fun createRadio(@Body radioData : Radio) : Response<Radio>
}