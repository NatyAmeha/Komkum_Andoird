package com.example.ethiomusic.data.api

import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.MusicBrowse
import com.example.ethiomusic.data.model.Search
import com.example.ethiomusic.data.model.Song
import retrofit2.Response
import retrofit2.http.*

interface SongApi {

    @GET("home")   // for fetching new or pouplar songs
    suspend fun getSongs(@Query("filter") filter : String) : Response<List<Song<String , String>>>

    @PUT("songs")
    suspend fun getSongsBeta(@Body songIds : List<String>) : Response<List<Song<String,String>>>


    @GET("library")
    suspend fun getLikedSongs(@Query("filter") filter : String? = null) : Response<List<Song<String , String>>>

    @PUT("library/likesong")
    suspend fun likeSong(@Body songIds : List<String>) : Response<Boolean>

    @PUT("library/removelikesong")
    suspend fun unlikeSong(@Body songIds: List<String>) : Response<Boolean>

    @PUT("library/checksongfavorite")
    suspend fun checkSongInFavorite(@Query("songid") songId : String) : Response<Boolean>

    @PUT("song/{songId}")
    suspend fun updateStreamcount(@Path("songId")songId : String) : Response<Boolean>

    @PUT("song/download/{songId}")
    suspend fun downloadSong(@Path("songId")songId : String) : Response<Boolean>

    @GET("search/song")
    suspend fun getSongSearchResult(@Query("query")query : String) : Response<Search>

    @GET("browse/music/{category}")
    suspend fun getMusicBrowsingString(@Path("category") category : String) : Response<List<MusicBrowse>>
}