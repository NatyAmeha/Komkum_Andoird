package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.PodcastApi
import com.komkum.komkum.data.model.EpisodeComment
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.PodcastEpisode
import com.komkum.komkum.data.model.PodcastHomeViewmodel
import com.komkum.komkum.ui.podcast.PodcastViewModel
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import javax.inject.Inject

class PodcastRepository @Inject constructor(var podcastApi : PodcastApi) {
    var error = MutableLiveData<String>()

    suspend fun getPodcastHome() = dataRequestHelper<Any , PodcastHomeViewmodel>( errorHandler = fun(message : String){error.postValue(message)}){
        podcastApi.getPodcastHome()
    }

    suspend fun getPodcast(podcastId : String) = dataRequestHelper(podcastId , fun(message : String){error.postValue(message)}){
        podcastApi.getPodcast(it!!)
    }

    suspend fun getPodcastEpisode(episodeId : String) = dataRequestHelper(episodeId , fun(message : String){error.postValue(message)}){
        podcastApi.getPodcastEpisode(it!!)
    }

    suspend fun getPodcastPublisher(publisherId : String) = dataRequestHelper(publisherId , fun(message : String){error.postValue(message)}){
        podcastApi.getPodcastPublisher(it!!)
    }

    suspend fun isPocastFollowedByUser(podcastId : String) = dataRequestHelper(podcastId , fun(message : String){error.postValue(message)}){
        podcastApi.isPodcastInFavorite(it!!)
    }


    suspend fun getUserPodcasts() = dataRequestHelper<Any , List<Podcast<String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        podcastApi.getUserPodcasts()
    }

    suspend fun getPodcastByCategory(category : String) = dataRequestHelper<Any , List<Podcast<String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        podcastApi.podcastByCategory(category)
    }

    fun getUserEpisodes() = apiDataRequestHelper<Any , List<PodcastEpisode>>(errorHandler = fun(message : String){error.postValue(message)}){
        podcastApi.getUserEpisode()
    }

    suspend fun followPodcast(podcastId : String) = dataRequestHelper(podcastId , fun(message : String){error.postValue(message)}){
        podcastApi.followPodcast(it!!)
    }

    suspend fun followEpisode(episodeIdId : String) = dataRequestHelper(episodeIdId , fun(message : String){error.postValue(message)}){
        podcastApi.followEpisode(it!!)
    }

    suspend fun unfollowPodcast(podcastId : String) = dataRequestHelper(podcastId , fun(message : String){error.postValue(message)}){
        podcastApi.unfollowPodcast(it!!)
    }

    suspend fun unfollowEpisode(episodeId : String) = dataRequestHelper(episodeId , fun(message : String){error.postValue(message)}){
        podcastApi.unfollowEpisode(it!!)
    }

    suspend fun downloadEpisode(episodeId : String) = dataRequestHelper(episodeId , fun(message : String){error.postValue(message)}){
        podcastApi.downloadEpisode(it!!)
    }

    suspend fun addCommentToEpisode(episodeId : String , commentInfo : EpisodeComment<String>) = dataRequestHelper(episodeId , fun(message : String){error.postValue(message)}){
        podcastApi.commentEpisode(episodeId , commentInfo)
    }

    suspend fun checkEpisodeFavorite(episodeId : String) = dataRequestHelper(episodeId , fun(message : String){error.postValue(message)}){
        podcastApi.isEpisodeInFav(episodeId)
    }

}
