package com.komkum.komkum.ui.song

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.IViewmodelFactory
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.data.repo.DownloadRepository
import com.komkum.komkum.data.repo.SongRepository
import com.komkum.komkum.util.Resource
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
        songLists.value = songResult.data!!
    }

    fun getPopularSongs() = viewModelScope.launch {
        var songResult = songRepo.getSongs("popularsong")
        songLists.value = songResult.data!!
    }

    suspend fun getPlaylistREcommendationsToAddInPlaylist()  = songRepo.getplaylistREcommendation()



    fun searchSong(query : String) = liveData{
        var result = songRepo.getSongSearchREsult(query)
        emit(result.data?.songs)
    }
}

class SongViewmodelFactory @Inject constructor(var songRepo : SongRepository , var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker) : IViewmodelFactory<SongViewModel>{
    override fun create(savedStateHandle: SavedStateHandle): SongViewModel {
        return SongViewModel(savedStateHandle , songRepo , downloadRepo , downloadTracker)
    }

}
