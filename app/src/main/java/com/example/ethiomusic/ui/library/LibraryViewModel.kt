package com.example.ethiomusic.ui.library

import androidx.lifecycle.*
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.data.repo.AlbumRepository
import com.example.ethiomusic.data.repo.LibraryRepository
import com.example.ethiomusic.data.repo.PlaylistRepository
import com.example.ethiomusic.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

class LibraryViewModel(var savedStateHandle: SavedStateHandle, var libraryRepo: LibraryRepository,
    var albumRepo: AlbumRepository , var playlistRepo : PlaylistRepository) : ViewModel() {

    var playlistData  = MutableLiveData<List<Playlist<String>>>()
    val error = libraryRepo.error


    fun getUserPlaylist() = viewModelScope.launch {
       playlistData.value =  playlistRepo.getUserPlaylist().data
    }


    var librarySummery = libraryRepo.getLibrarySummery()


    fun createPlaylist(playlist : Playlist<String>) = playlistRepo.createPlaylist(playlist)


//    var favoriteAlbums = libraryRepo.libraryResponse.switchMap { library ->
//        liveData {
//            library?.let {
//                var result = it.likedAlbums?.let {
//                    it.map { libraryInfo -> albumRepo.getAlbum(libraryInfo.vId!!).value!!.data }
//                }
//                emit(Resource.Success(result))
//            }
//            emit(Resource.Loading<Any>())
//        }
//    }
}


class LibraryViewmodelFacotry @Inject constructor(var libraryRepo: LibraryRepository, var albumRepo: AlbumRepository, var playlistRepo : PlaylistRepository)
    : IViewmodelFactory<LibraryViewModel> {
    override fun create(savedStateHandle: SavedStateHandle): LibraryViewModel {
        return LibraryViewModel(savedStateHandle, libraryRepo, albumRepo , playlistRepo)
    }
}
