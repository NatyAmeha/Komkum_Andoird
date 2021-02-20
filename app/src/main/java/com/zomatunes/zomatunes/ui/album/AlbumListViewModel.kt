package com.zomatunes.zomatunes.ui.album

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.IViewmodelFactory
import com.zomatunes.zomatunes.data.api.AlbumApi
import com.zomatunes.zomatunes.data.model.Album
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.data.repo.AlbumRepository
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.LibraryRepository
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.adaper.DownloadAdapter
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.*
import javax.inject.Inject

class AlbumListViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle, var albumRepo : AlbumRepository, var downloadRepo : DownloadRepository,
                                                          var downloadTracker: DownloadTracker) : ViewModel() {
    var albumListData = MutableLiveData<List<Album<String , String>>>()

    companion object {
        const val LOAD_USER_FAV_ALBUM = 0
        const val LOAD_NEW_ALBUM = 1
        const val LOAD_POPULAR_ALBUM = 2
        const val OTHER = 3
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

    fun getnewAlbums(genre : String? = null) = viewModelScope.launch {
        albumListData.value = albumRepo.getNewAlbums(genre).data
    }


    fun downloadAlbums(albumIds : List<String> , owner : LifecycleOwner) = viewModelScope.launch {
        albumIds.forEach { id ->
            var albumResult = albumRepo.getAlbumBeta(id).data
            albumResult?.let{
                downloadRepo.downloadAlbum(it)
                downloadTracker.addDownloads(it.songs!! , owner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
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
