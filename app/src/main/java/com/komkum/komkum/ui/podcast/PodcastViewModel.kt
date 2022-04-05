package com.komkum.komkum.ui.podcast

import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.DownloadRepository
import com.komkum.komkum.data.repo.PodcastRepository
import kotlinx.coroutines.launch


class PodcastViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle , var podcastRepo : PodcastRepository ,
 var downloadTracker: DownloadTracker , var downloadRepo : DownloadRepository) : ViewModel() {
    val error = MutableLiveData<String>()

    var podcastHomeResult = MutableLiveData<PodcastHomeViewmodel?>()
    fun getError() : MediatorLiveData<String> {
        var a = MediatorLiveData<String>()
        a.addSource(podcastRepo.error){ error.value = it }
        return a
    }

    fun removeOldErrors(){
        podcastRepo.error.value = null
    }

    var podcastResult = MutableLiveData<Podcast<PodcastEpisode>>()
    var podcastLists = MutableLiveData<List<Podcast<String>>>()

    var publisherResult = MutableLiveData<PodcastPublisher<Podcast<String>>>()
    var userFavoriteEpisodes = podcastRepo.getUserEpisodes()


    fun getPodcastHome() = viewModelScope.launch {
        podcastHomeResult.value = podcastRepo.getPodcastHome().data
    }

    fun getUserFavoritePodcasts() = viewModelScope.launch {
        var result = podcastRepo.getUserPodcasts().data
        if(result != null) podcastLists.value = result!!
    }

    fun getPodcastsByCategory(category : String) = viewModelScope.launch {
        var result = podcastRepo.getPodcastByCategory(category).data
        if(result != null) podcastLists.value = result!!
    }

    suspend fun getPodcast(podcastId : String) = viewModelScope.launch{
        var result = podcastRepo.getPodcast(podcastId)
        result.data?.episods?.forEach { episode ->
            var result =  downloadRepo.isInDownload(episode._id)
            episode.isInDownloads = result
        }
        result.data?.let { podcastResult.value = it }
    }

    suspend fun getPodcastEpisode(episodeId : String) : PodcastEpisode?{
        var result = podcastRepo.getPodcastEpisode(episodeId)
        return result.data?.let{episode ->
            var result =  downloadRepo.isInDownload(episode._id)
            episode.isInDownloads = result
            return episode
        }
    }

    suspend fun getPodcastPublisher(publisherId : String) = viewModelScope.launch{
        var result = podcastRepo.getPodcastPublisher(publisherId)
        result.data?.let {
            publisherResult.value = it
        }
    }



    fun followPodcast(podcastId : String) =liveData{
        var result = podcastRepo.followPodcast(podcastId)
        emit(result.data)

    }
    fun unfollowPodcast(podcastId : String) = liveData{
        var result = podcastRepo.unfollowPodcast(podcastId)
        emit(result.data)
    }

    fun followEpisode(episodeId : String) = liveData{
        var result = podcastRepo.followEpisode(episodeId)
        emit(result.data)

    }

    fun unfollowEpisode(episodeId : String) = liveData{
        var result = podcastRepo.unfollowEpisode(episodeId)
        emit(result.data)
    }

    fun getEpisodeDownloadStatus(episodeId: String){
        var download = downloadTracker.getCurrentDownloadItem(episodeId)
        download?.state
    }



    fun downloadEpisode(episode : PodcastEpisode) = liveData{
        var result = podcastRepo.downloadEpisode(episode._id)
        if(result.data == true){
            downloadRepo.saveEpisodetoDatabase(episode)
            var path = Uri.parse(episode.filePath)
            downloadTracker.addProgressiveDownload(episode._id , path)
           return@liveData emit(true)
        }
        else return@liveData emit(result.data)
    }

    fun removeDownloadedEpisode(episodeId: String) = liveData {
        var result = downloadRepo.deleteDownloadedEpisode(episodeId)
        emit(result)
    }

    fun isEpisodeDownloaded(episodeId : String) = liveData {
        var result = downloadRepo.isInDownload(episodeId)
        emit(result)
    }

    fun commentEpisode(episodeId : String , commentINfo : EpisodeComment<String>) = liveData{
        var result = podcastRepo.addCommentToEpisode(episodeId , commentINfo)
        emit(result.data)
    }



}