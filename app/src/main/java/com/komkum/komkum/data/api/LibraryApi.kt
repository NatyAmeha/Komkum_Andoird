package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.Album
import com.komkum.komkum.data.model.Library
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.Song
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

    @GET("home/chart")
    suspend fun getCharts() : Response<List<Playlist<String>>>

    @PUT("playlist/makepublic/{id}")
    suspend fun makePlaylistPublic(@Path("id") playlistId: String) : Response<Boolean>

    @GET("song/newsongs/{genre}")
    suspend fun getnewSongPlaylists(@Path("genre")genre: String? = null) : Response<List<Playlist<String>>>

    @GET("playlist")
    suspend fun getUserPlaylists() : Response<List<Playlist<String>>>

    @GET("playlist/public")
    suspend fun getpublicPlaylist(@Query("creatorId")creatorId : String) : Response<List<Playlist<String>>>

    @PUT("playlist/follow")
    suspend fun addPlaylistToFavorite(@Query("id")playlistId : String) : Response<Boolean>

    @PUT("playlist/unfollow")
    suspend fun removePlaylistFromFavorite(@Query("id")playlistId : String) : Response<Boolean>

    @PUT("playlist/addsong")
    suspend fun addSongToPlaylist(@Body playlist : Playlist<String>) : Response<Boolean>

    @PUT("playlist/checkfavorite")
    suspend fun checkPlaylistInLibrary(@Query("id") playlistId : String) : Response<Boolean>

    @PUT("playlist/removesong")
    suspend fun removeSongsFromPlaylist(@Body playlist: Playlist<String>) : Response<Boolean>

    @DELETE("playlist/delete/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId : String) : Response<Boolean>
}