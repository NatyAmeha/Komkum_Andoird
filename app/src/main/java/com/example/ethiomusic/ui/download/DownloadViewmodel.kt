package com.example.ethiomusic.ui.download

import androidx.lifecycle.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.IViewmodelFactory
import com.example.ethiomusic.data.db.AppDb
import com.example.ethiomusic.data.model.AlbumSongDbJoin
import com.example.ethiomusic.data.model.DbDownloadInfo
import com.example.ethiomusic.data.model.PlaylistSongDbJoin
import com.example.ethiomusic.data.repo.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DownloadViewmodel(var savedStateHandle: SavedStateHandle , var downloadRepo : DownloadRepository ,  var appDb: AppDb , var downloadTracker: DownloadTracker) : ViewModel() {

    companion object{
        const val DOWNLOAD_TYPE_SONG = 0
        const val DOWNLOAD_TYPE_ALBUM = 1
        const val DOWNLOAD_TYPE_PLAYLIST = 2
    }

    fun getDownloads()  = downloadRepo.getDownloads()

    fun getSong(songId : String) = liveData {
        withContext(Dispatchers.IO){
            var result = appDb.songDao.getSongs(songId)
            emit(result)
        }
    }



    fun removeDownloadedAlbum(albumId : String) = viewModelScope.launch {
        downloadRepo.deleteDownloadedAlbum(albumId)
    }

    fun deleteAlbumSongs(songIds : List<String> , albumId: String) = viewModelScope.launch {
        downloadRepo.removeAlbumSongsFromDownload(songIds , albumId)
    }

    fun removeDownloadedPlaylist(playlistId : String) = viewModelScope.launch {
       downloadRepo.deletePlaylist(playlistId)
    }

    fun removePlaylistSongs(songIds : List<String> , playlistId : String) = viewModelScope.launch {
        downloadRepo.removePlaylistSongsFromDownload(songIds , playlistId)
    }


    fun pauseAlbumDownload(albumId: String , downloadInfo : DbDownloadInfo) = viewModelScope.launch{
        withContext(Dispatchers.IO){
            downloadInfo.state = "PAUSED"
            appDb.downloadDao.updateDownloadState(downloadInfo)
            var songs = appDb.songDao.getSongsByAlbumId(albumId)
            downloadTracker.pauseDownloads(songs.map { song -> song._id })
        }
    }

    fun pauseSongDownload(songId: String , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            downloadInfo.state = "PAUSED"
            appDb.downloadDao.updateDownloadState(downloadInfo)
            var song = appDb.songDao.getSong(songId)
            downloadTracker.pauseDownload(song!!._id)
        }
    }


    fun resumeAllDownloads(downloadList : List<DbDownloadInfo> ) = viewModelScope.launch{
        downloadList.forEach { download -> download.state = null}
        appDb.downloadDao.updateDownloadsState(downloadList)
        downloadTracker.resumeAllDownload()
    }

    fun resumeAlbumDownload(albumId: String , downloadInfo: DbDownloadInfo) = viewModelScope.launch{
        withContext(Dispatchers.IO){
            downloadInfo.state = null
            appDb.downloadDao.updateDownloadState(downloadInfo)
            var songs = appDb.songDao.getSongsByAlbumId(albumId)
            downloadTracker.resumeDownloads(songs.map { song -> song._id })
        }
    }

    fun resumeSongDownload(songId: String , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            downloadInfo.state = null
            appDb.downloadDao.updateDownloadState(downloadInfo)
            var song = appDb.songDao.getSong(songId)
            downloadTracker.resumeDownloads(listOf(song!!._id))
        }
    }

    fun removeDownloadedSong(songId : String) = viewModelScope.launch {
         appDb.downloadDao.getDownload(songId)?.let {
            appDb.downloadDao.removeDownload(it)
            getDownloads()
        }
        appDb.songDao.getSong(songId)?.let {
            downloadTracker.removeDownload(songId)
            appDb.songDao.deleteSong(it)
        }

    }

    fun deleteAllDownloads(downloadList : List<DbDownloadInfo>) = viewModelScope.launch {
        appDb.downloadDao.removeDownloads(downloadList)
        appDb.albumSongJoinDao.deleteAll()
        appDb.playlistSongJoinDao.deleteAll()
        appDb.songDao.deleteAll()
        appDb.albumDao.deleteAll()
        appDb.playlistDao.deleteAll()

        downloadRepo.getDownloads()
    }
}

class DownloadViewmodelFactory @Inject constructor(var appDb: AppDb , var downloadRepo : DownloadRepository , var downloadTracker: DownloadTracker) :
    IViewmodelFactory<DownloadViewmodel> {
    override fun create(savedStateHandle: SavedStateHandle): DownloadViewmodel {
        return DownloadViewmodel(savedStateHandle , downloadRepo ,  appDb  , downloadTracker)
    }
}
