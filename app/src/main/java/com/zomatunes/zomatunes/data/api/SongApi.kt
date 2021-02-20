package com.zomatunes.zomatunes.data.api

import com.zomatunes.zomatunes.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SongApi {

    @GET("home")   // for fetching new or pouplar songs
    suspend fun getSongs(@Query("filter") filter : String) : Response<List<Song<String , String>>>

    @PUT("songs")
    suspend fun getSongsBeta(@Body songIds : List<String>) : Response<List<Song<String,String>>>

    //recommeded playlist to add song to created playlist
    @GET("playlists/recommendation")
    suspend fun getplaylistRecommendations() : Response<List<Playlist<Song<String , String>>>>


    @GET("library")
    suspend fun getLikedSongs(@Query("filter") filter : String? = null) : Response<Playlist<Song<String,String>>>

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
    suspend fun getBrowseItems(@Path("category") category : String) : Response<List<MusicBrowse>>
}