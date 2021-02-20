package com.zomatunes.zomatunes.data.api

import com.zomatunes.zomatunes.data.model.*
import retrofit2.Response
import retrofit2.http.*


interface BookApi {
    @GET("book/home/{genre}")
    suspend fun getBookHome(@Path("genre")genre : String) : Response<BookHomeModel>

    @GET("audiobook/recommended/{id}")
    suspend fun getAudiobookSuggestion(@Path("id") id : String) : Response<BookSuggestion>

    @GET("book/recommended/{id}")
    suspend fun getEbookSuggestion(@Path("id") id : String) : Response<BookSuggestion>

    @GET("ebook/{id}")
    suspend fun getEbook(@Path("id") id : String) : Response<EBook<Author<String,String>>>

    @GET("audiobook/{id}")
    suspend fun getAudiobook(@Path("id") id : String) : Response<Audiobook<Chapter , Author<String , String>>>



    @POST("audiobook/rate")
    suspend fun rateAudiobook(@Body ratingInfo : Rating) : Response<Boolean>

    @POST("book/rate")
    suspend fun rateEbook(@Body ratingInfo : Rating) : Response<Boolean>

    @PUT("audiobook/download/{id}")
    suspend fun saveAudiobookDownloadInfo(@Path("id") bookId : String) : Response<Boolean>

    @PUT("book/download/{id}")
    suspend fun saveEbookDownloadInfo(@Path("id") bookId : String) : Response<Boolean>

    @PUT("book/friends")
    suspend fun getFacebookFriendsBook(@Body friendsId : List<String>) : Response<List<Audiobook<String , String>>>
}