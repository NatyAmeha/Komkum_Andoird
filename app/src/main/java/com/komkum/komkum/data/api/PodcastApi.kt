package com.komkum.komkum.data.api

import com.google.firebase.events.Publisher
import com.komkum.komkum.data.model.*
import com.komkum.komkum.ui.podcast.PodcastViewModel
import retrofit2.Response
import retrofit2.http.*

interface PodcastApi {
    @GET("podcast/home")
    suspend fun getPodcastHome() : Response<PodcastHomeViewmodel>

    @GET("podcast/{id}")
    suspend fun getPodcast(@Path("id") podcastId : String) : Response<Podcast<PodcastEpisode>>

    @GET("episode/{id}")
    suspend fun getPodcastEpisode(@Path("id") episodeId : String) : Response<PodcastEpisode>

    @GET("podcast/publisher/{id}")
    suspend fun getPodcastPublisher(@Path("id") publisherId : String) : Response<PodcastPublisher<Podcast<String>>>

    @GET("podcast/isfavorite")
    suspend fun isPodcastInFavorite(@Query("id") podcastId: String) : Response<Boolean>

    @GET("podcast/me")
    suspend fun getUserPodcasts() : Response<List<Podcast<String>>>

    @GET("podcasts")
    suspend fun podcastByCategory(@Query("category") category : String) : Response<List<Podcast<String>>>

    @GET("episode/me")
    suspend fun getUserEpisode() : Response<List<PodcastEpisode>>

    @PUT("podcast/follow")
    suspend fun followPodcast(@Query("id") podcastId : String) : Response<Boolean>

    @PUT("podcast/unfollow")
    suspend fun unfollowPodcast(@Query("id") podcastId : String) : Response<Boolean>

    @PUT("episode/like")
    suspend fun followEpisode(@Query("id") episodeId : String) : Response<Boolean>

    @PUT("episode/unlike")
    suspend fun unfollowEpisode(@Query("id") episodeId : String) : Response<Boolean>

    @PUT("episode/download")
    suspend fun downloadEpisode(@Query("id") episodeId : String) : Response<Boolean>

    @PUT("episode/addcomment")
    suspend fun commentEpisode(@Query("id") episodeId : String , @Body commentInfo : EpisodeComment<String>) : Response<Boolean>

    @GET("episode/isfavorite")
    suspend fun isEpisodeInFav(@Query("id") episodeId: String) : Response<Boolean>
}