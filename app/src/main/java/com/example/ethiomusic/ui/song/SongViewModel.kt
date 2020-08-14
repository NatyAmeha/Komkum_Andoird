package com.example.ethiomusic.ui.song

import androidx.lifecycle.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.DownloadRepository
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class SongViewModel(var savedState : SavedStateHandle , var songRepo : SongRepository ,
                    var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker) : ViewModel() {

    var songLists  = MutableLiveData<List<Song<String,String>>>()

    fun getLikedSong() = viewModelScope.launch {
        var likedSongs  = songRepo.getLikedSongs()
        songLists.value = likedSongs.data
    }

    fun getNewSongs() = viewModelScope.launch  {
        var songResult = songRepo.getSongs("newsong")
        songLists.value = songResult.data
    }

    fun getPopularSongs() = viewModelScope.launch {
        var songResult = songRepo.getSongs("popularsong")
        songLists.value = songResult.data
    }

    suspend fun getsongToAddInPlaylist(filter : String) : List<Song<String, String>>? {
        return songRepo.getSongs(filter).data
    }

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
