package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.ProductHomepageViewmodel
import retrofit2.Response
import retrofit2.http.*

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

    @GET("search/recommendation")
    suspend fun getSearchRecommendation() : Response<Search>

    @PUT("browse")
    suspend fun browseResult(@Body browseInfo : MusicBrowse) : Response<MusicBrowseContent>

    @GET("products/home")
    suspend fun getProductHomepage(@Query("long") long : Double? = null , @Query("lat")lat : Double? = null) : Response<ProductHomepageViewmodel>

    @GET("donations")
    suspend fun getDonations(@Query("creator") creatorId : String) : Response<List<Donation>>

    @GET("donations/topsupporters")
    suspend fun getTopDonators(@Query("category") category : Int , @Query("size")size : Int) : Response<List<DonationLeaderBoard>>

    @GET("donations/topcreators")
    suspend fun getMostDonatedCreators(@Query("category") category : Int , @Query("size")size : Int) : Response<List<DonationLeaderBoard>>

}