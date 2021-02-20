package com.zomatunes.zomatunes.data.api

import com.zomatunes.zomatunes.data.model.Artist
import com.zomatunes.zomatunes.data.model.ArtistMetaData
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.Search
import retrofit2.Response
import retrofit2.http.*

interface ArtistApi {

    @GET("artist/{id}")
    suspend fun getArtist(@Path("id") artistId : String) : Response<ArtistMetaData>

    @GET("artist/popular")
    suspend fun getPopularArtist() : Response<List<Artist<String , String>>>

    @GET("artist/new")
    suspend fun getnewArtists() : Response<List<Artist<String , String>>>

    @GET("artist/genre/{genre}")
    suspend fun getArtistByGenre(@Path("genre") genre : String) :  Response<List<Artist<String , String>>>

    @GET("artist/favorite")
    suspend fun getFavoriteArtist() :  Response<List<Artist<String , String>>>

    @GET("artist/favorite/{id}")
    suspend fun getFavoriteArtist(@Path("id")userId : String) :  Response<List<Artist<String , String>>>

    @POST("artist/favorite")
    suspend fun addToFavoriteArtist(@Body artists : List<String>) : Response<Any>

    // above and below method must be merged

    @PUT("library/followartist")
    suspend fun followArtist(@Body artistsId : List<String>) : Response<Boolean>

    @PUT("library/unfollowartist")
    suspend fun unfollowArtist(@Body artistsId : List<String>) : Response<Boolean>

    @PUT("library/checkartistinfavorite")
    suspend fun checkArtistInFavorite(@Query("artistid") artistId: String) : Response<Boolean>

    @GET("search/artist")
    suspend fun getArtistSearch(@Query("query")query : String) : Response<Search>


}