package com.zomatunes.zomatunes.ui.song

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.IViewmodelFactory
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class SongViewModel @ViewModelInject constructor(@Assisted var savedState : SavedStateHandle, var songRepo : SongRepository,
                                                     var downloadRepo : DownloadRepository, var downloadTracker: DownloadTracker) : ViewModel() {

    var songLists  = MutableLiveData<List<Song<String,String>>>()

//    fun getLikedSong() = viewModelScope.launch {
//        var likedSongs  = songRepo.getLikedSongs()
//        songLists.value = likedSongs.data
//    }

    fun getNewSongs() = viewModelScope.launch  {
        var songResult = songRepo.getSongs("newsong")
        songLists.value = songResult.data
    }

    fun getPopularSongs() = viewModelScope.launch {
        var songResult = songRepo.getSongs("popularsong")
        songLists.value = songResult.data
    }

    suspend fun getPlaylistREcommendationsToAddInPlaylist()  = songRepo.getplaylistREcommendation()



    fun searchSong(query : String) = liveData{
        var result = songRepo.getSongSearchREsult(query)
        emit(result.data?.songs)
    }

    fun downloadSongs(songs : List<Song<String,String>> , lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        downloadRepo.downloadSongs(songs)
        downloadTracker.addDownloads(songs , lifecycleOwner)
    }
}

class SongViewmodelFactory @Inject constructor(var songRepo : SongRepository , var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker) : IViewmodelFactory<SongViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): SongViewModel {
        return SongViewModel(savedStateHandle , songRepo , downloadRepo , downloadTracker)
    }

}
