package com.example.ethiomusic.data.api

import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.Library
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.Song
import retrofit2.Response
import retrofit2.http.*

interface LibraryApi {

    @GET("library")
    suspend fun getLibrary() : Response<Library>

    @GET("library")
    suspend fun getFavorite(@Query("filter") filter : String? = null) : Response<List<Album<String , String>>>

    @POST("playlist/create")
    suspend fun createPlaylist(@Body playlist : Playlist<String>) : Response<Playlist<String>>

    @PUT("playlist/update")
    suspend fun updatePlaylist(@Body playlist: Playlist<String>) : Response<Boolean>

    @GET("playlist/{id}")
    suspend fun getPlaylist(@Path("id") playlist : String) : Response<Playlist<Song<String , String>>>

    @GET("playlist/featured")
    suspend fun geFeaturedPlaylists() : Response<List<Playlist<String>>>

    @GET("song/newsongs/{genre}")
    suspend fun getnewSongPlaylists(@Path("genre")genre: String? = null) : Response<List<Playlist<String>>>

    @GET("playlist")
    suspend fun getUserPlaylists() : Response<List<Playlist<String>>>

    @PUT("playlist/addsong")
    suspend fun addSongToPlaylist(@Body playlist : Playlist<String>) : Response<Boolean>

    @PUT("playlist/removesong")
    suspend fun removeSongsFromPlaylist(@Body playlist: Playlist<String>) : Response<Boolean>

    @DELETE("playlist/delete/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId : String) : Response<Boolean>
}