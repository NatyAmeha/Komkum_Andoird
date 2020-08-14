package com.example.ethiomusic.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.ethiomusic.data.api.AlbumApi
import com.example.ethiomusic.data.api.LibraryApi
import com.example.ethiomusic.data.api.SongApi
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.repo.AlbumRepository
import com.example.ethiomusic.data.repo.PlaylistRepository
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.util.extensions.exhaustive
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import javax.inject.Inject

class LibraryService @Inject constructor(
    var playlistRepo: PlaylistRepository,
    var albumRepo: AlbumRepository,
    var songRepo: SongRepository
) {

    companion object {
        const val CONTENT_TYPE_SONG = 0
        const val CONTENT_TYPE_ALBUM = 1
        const val CONTENT_TYPE_ARTIST = 2
    }



   suspend fun addToFavorite(type: Int, data: List<String>) = when (type) {
        CONTENT_TYPE_SONG -> songRepo.likeSong(data).data
        CONTENT_TYPE_ALBUM -> albumRepo.addTofavorite(data).data
        else -> false
    }

    suspend fun removeFromFavorite(type : Int , data : List<String>) = when(type){
        CONTENT_TYPE_SONG -> songRepo.unlikeSong(data).data
        CONTENT_TYPE_ALBUM -> albumRepo.removeFavAlbum(data).data
        else -> false
    }

    fun addToPlaylist(playlistId : String , songIds : List<String>): LiveData<Resource<Boolean>> {
        var playlistInfo = Playlist(_id = playlistId , songs = songIds)
       return  playlistRepo.addSongstoPlaylist(playlistInfo)
    }

    suspend fun checkSongInFavorite(songId : String) = songRepo.isSongInFavorite(songId)

}