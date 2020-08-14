package com.example.ethiomusic.data.api

import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.AlbumIdObject
import com.example.ethiomusic.data.model.QueueResponse
import com.example.ethiomusic.data.model.Song
import retrofit2.Response
import retrofit2.http.*

interface AlbumApi {
    @GET("album/{id}")
    suspend fun getAbum(@Path("id") albumId : String) : Response<Album<Song<String , String> , String>>

    @GET("albums/new/{genre}")
    suspend fun getNewalbums(@Path("genre")genre : String? = null) : Response<List<Album<String , String>>>

    @GET("albums/popular/{genre}")
    suspend fun getPopularalbums(@Path("genre") genre: String? = null) : Response<List<Album<String , String>>>

    @GET("albums/recommended")
    suspend fun getRecommendedalbums() : Response<List<Album<String , String>>>

    @GET("home")
    suspend fun getPopularAlbums(@Query("filter") filter : String) : Response<List<Album<String , String>>>

    @PUT("library/likealbum")
    suspend fun addTofavorite(@Body albumIds: List<String>) : Response<Boolean>

    @PUT("library/removefavalbum")
    suspend fun removeFromFavorite(@Body albumIds: List<String>) : Response<Boolean>


    @POST("library/checkalbuminfavorite")
    suspend fun checkAlbumInFavorites(@Body albumId: Album<String , String>) : Response<Boolean>
}