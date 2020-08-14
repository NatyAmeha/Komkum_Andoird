package com.example.ethiomusic.data.api

import com.example.ethiomusic.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface HomeApi {
    @GET("home")
    suspend fun getHome(@Query("filter")filter : String? = null) : Response<Home>

    @GET("homenew")
    suspend fun getHomeBeta() : Response<HomeBeta>

    @GET("recommendedbyartist")
    suspend fun getSongRecommendationByArtist() : Response<Playlist<Song<String , String>>>

    @GET("recommendedbytags")
    suspend fun getSongsByTags() : Response<List<Playlist<String>>>

    @GET("search")
    suspend fun getSearch(@Query("query")query : String) : Response<Search>

    @PUT("browse")
    suspend fun browseResult(@Body browseInfo : MusicBrowse) : Response<MusicBrowseContent>

}