package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.Album
import com.komkum.komkum.data.model.Song
import retrofit2.Response
import retrofit2.http.*

interface AlbumApi {
    @GET("album/{id}")
    suspend fun getAbum(@Path("id") albumId : String) : Response<Album<Song<String , String> , String>>

    @GET("albums/new")
    suspend fun getNewalbums(@Query("genre") genre : String? = null) : Response<List<Album<String , String>>>

    @GET("albums/popular")
    suspend fun getPopularalbums(@Query("genre") genre: String? = null) : Response<List<Album<String , String>>>

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