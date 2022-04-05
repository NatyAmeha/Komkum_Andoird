package com.komkum.komkum.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.repo.AlbumRepository
import com.komkum.komkum.data.repo.PlaylistRepository
import com.komkum.komkum.data.repo.SongRepository
import javax.inject.Inject

class LibraryService @Inject constructor(
    var playlistRepo: PlaylistRepository,
    var albumRepo: AlbumRepository,
    var songRepo: SongRepository
) {
    var error = MutableLiveData<String>()

    companion object {
        const val CONTENT_TYPE_SONG = 0
        const val CONTENT_TYPE_ALBUM = 1
        const val CONTENT_TYPE_ARTIST = 2
    }


    suspend fun addToFavorite(type: Int, data: List<String>) = when (type) {
        CONTENT_TYPE_SONG -> {
            error = songRepo.error
            songRepo.likeSong(data).data
        }
        CONTENT_TYPE_ALBUM -> {
            error = albumRepo.error
            albumRepo.addTofavorite(data).data
        }
        else -> false
    }

    suspend fun removeFromFavorite(type: Int, data: List<String>) = when (type) {
        CONTENT_TYPE_SONG -> songRepo.unlikeSong(data).data
        CONTENT_TYPE_ALBUM -> albumRepo.removeFavAlbum(data).data
        else -> false
    }

    fun addToPlaylist(playlistId: String, songIds: List<String>): LiveData<Resource<Boolean>> {
        var playlistInfo = Playlist(_id = playlistId, songs = songIds)
        return playlistRepo.addSongstoPlaylist(playlistInfo)
    }

    suspend fun checkSongInFavorite(songId: String) = songRepo.isSongInFavorite(songId)

}