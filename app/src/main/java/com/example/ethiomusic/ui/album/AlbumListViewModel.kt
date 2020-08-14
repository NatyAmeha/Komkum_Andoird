package com.example.ethiomusic.ui.album

import androidx.lifecycle.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.api.AlbumApi
import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.AlbumRepository
import com.example.ethiomusic.data.repo.DownloadRepository
import com.example.ethiomusic.data.repo.LibraryRepository
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.*
import javax.inject.Inject

class AlbumListViewModel(var savedStateHandle: SavedStateHandle , var albumRepo : AlbumRepository , var downloadRepo : DownloadRepository ,
                         var downloadTracker: DownloadTracker) : ViewModel() {
    var albumListData = MutableLiveData<List<Album<String , String>>>()

    companion object {
        fun newInstance() = AlbumListFragment()
        const val LOAD_USER_FAV_ALBUM = 0
        const val LOAD_POPULR_GOSPEL_ALBUM = 1
        const val LOAD_POPULAR_SECULAR_ALBUM = 2
    }

    val stateManager : RecyclerviewStateManager<BaseModel> by lazy{
        RecyclerviewStateManager<BaseModel>()
    }

    fun userfavoriteAlbums() = viewModelScope.launch{
       albumListData.value = albumRepo.getFavoriteAlbums().data
    }

     fun getPopularAlbums(filter : String) = viewModelScope.launch{
         albumListData.value = albumRepo.getPopularAlbumsBeta(filter).data
     }


    fun downloadAlbums(albumIds : List<String> , owner : LifecycleOwner) = viewModelScope.launch {
        albumIds.forEach { id ->
            var albumResult = albumRepo.getAlbumBeta(id).data
            albumResult?.let{
                downloadRepo.downloadAlbum(it)
                downloadTracker.addDownloads(it.songs!! , owner)
            }
        }
    }


}

class AlbumListViewmodelFactory @Inject constructor(var albumRepo : AlbumRepository, var downloadRepo : DownloadRepository ,
                                                    var downloadTracker: DownloadTracker) : IViewmodelFactory<AlbumListViewModel> {
    override fun create(savedStateHandle: SavedStateHandle): AlbumListViewModel {
        return AlbumListViewModel(savedStateHandle , albumRepo , downloadRepo , downloadTracker)
    }
}
