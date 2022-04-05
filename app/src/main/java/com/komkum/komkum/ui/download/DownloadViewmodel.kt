package com.komkum.komkum.ui.download

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.DbDownloadInfo
import com.komkum.komkum.data.repo.DownloadRepository
import com.novoda.downloadmanager.DownloadBatchIdCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DownloadViewmodel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle, var downloadRepo : DownloadRepository,
                                                     var appDb: AppDb, var downloadTracker: DownloadTracker) : ViewModel() {

    companion object{
        const val DOWNLOAD_TYPE_SONG = 0
        const val DOWNLOAD_TYPE_ALBUM = 1
        const val DOWNLOAD_TYPE_PLAYLIST = 2
        const val DOWNLOAD_TYPE_AUDIOBOOK = 3
        const val DOWNLOAD_TYPE_EBOOK = 4
        const val DOWNLOAD_TYPE_EPISODE = 5
    }

    var allDownloads = MutableLiveData<List<DbDownloadInfo>>()

//    fun getDownloads()  = viewModelScope.launch {
//        allDownloads = downloadRepo.getDownloads()
//    }

    fun getDownloadsBeta() = viewModelScope.launch {
        allDownloads.value = downloadRepo.getDownloadsBeta()
    }

    fun getSong(songId : String) = liveData {
        withContext(Dispatchers.IO){
            var result = appDb.songDao.getSongs(songId)
            emit(result)
        }
    }

    fun getDownloadedEbook(ebooKId : String) = liveData {
        var result = withContext(Dispatchers.IO){
            return@withContext appDb.ebookDao.getBook(ebooKId)
        }
        emit(result)
    }


    fun removeDownloadedAlbum(albumId : String) = viewModelScope.launch {
        downloadRepo.deleteDownloadedAlbum(albumId)
    }

    fun removeDownloadedPlaylist(playlistId : String) = viewModelScope.launch {
       downloadRepo.deletePlaylist(playlistId)
    }

    fun removeDownloadedAudioBook(audiobookId : String) = viewModelScope.launch {
        downloadRepo.deleteDownloadedAudioBook(audiobookId)
    }

    fun removeDownloadedEbook(ebookId :  String) = viewModelScope.launch {
        downloadRepo.deleteEbook(ebookId)
    }

    fun deleteAlbumSongs(songIds : List<String> , albumId: String) = viewModelScope.launch {
        downloadRepo.removeAlbumSongsFromDownload(songIds , albumId)
    }

    fun removePlaylistSongs(songIds : List<String> , playlistId : String) = viewModelScope.launch {
        downloadRepo.removePlaylistSongsFromDownload(songIds , playlistId)
    }

    fun getEpisodeFromDatabase(episodeId: String) = liveData {
        var result = downloadRepo.getEpisode(episodeId)
        emit(result)
    }



    fun pauseAlbumDownload(albumId: String) = viewModelScope.launch{
        withContext(Dispatchers.IO){
            var songs = appDb.albumSongJoinDao.getAlbumSongs(albumId)
            songs?.map { song -> song._id }?.let { downloadTracker.pauseDownloads(it) }
        }
    }

    fun pausePlaylistDownload(playlistId: String) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            var songs = appDb.playlistSongJoinDao.getPlaylistSongs(playlistId)
            downloadTracker.pauseDownloads(songs.map { song -> song._id })
        }
    }

    fun pauseAudioBookDownload(audiobookId: String) = viewModelScope.launch {
        var chapters = appDb.chapterDao.getBookChapters(audiobookId)
        downloadTracker.pauseDownloads(chapters.map { chapterDbInfo -> chapterDbInfo._id })
    }

    fun pausePodcastEpisodeDownload(episodeId: String) = viewModelScope.launch {
        downloadTracker.pauseDownloads(listOf(episodeId))
    }


    fun pauseEbookDownload(bookId : String){
        downloadTracker.getNovodaDownloadManager().pause(DownloadBatchIdCreator.createSanitizedFrom(bookId))
    }

    fun resumeAudiobookDownload(audiobookId: String) = viewModelScope.launch {
        var chapters = appDb.chapterDao.getBookChapters(audiobookId)
        downloadTracker.resumeDownloads(chapters.map { chapterDbInfo -> chapterDbInfo._id })
    }

    fun resumePodcastEpisodeDownload(episodeId: String) = viewModelScope.launch {
        downloadTracker.resumeDownloads(listOf(episodeId))
    }

    fun resumeEbookDownload(bookId : String){
        downloadTracker.getNovodaDownloadManager().resume(DownloadBatchIdCreator.createSanitizedFrom(bookId))
    }



    fun pauseSongDownload(songId: String , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
        withContext(Dispatchers.IO){
//            downloadInfo.state = "PAUSED"
//            appDb.downloadDao.updateDownloadState(downloadInfo)
            var song = appDb.songDao.getSong(songId)
            downloadTracker.pauseDownload(song!!._id)
        }
    }


    fun resumeAllDownloads(downloadList : List<DbDownloadInfo> ) = viewModelScope.launch{
        downloadTracker.resumeAllDownload()
    }

    fun resumeAlbumDownload(albumId: String) = viewModelScope.launch{
        withContext(Dispatchers.IO){
            var songs = appDb.albumSongJoinDao.getAlbumSongs(albumId)
            songs?.map { song -> song._id }?.let { downloadTracker.resumeDownloads(it) }
        }
    }

    fun resumePlaylistDownload(playlistId: String) = viewModelScope.launch{
        withContext(Dispatchers.IO){
            var songs = appDb.playlistSongJoinDao.getPlaylistSongs(playlistId)
            downloadTracker.resumeDownloads(songs.map { song -> song._id })
        }
    }

//    fun resumeSongDownload(songId: String , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
//        withContext(Dispatchers.IO){
//            downloadInfo.state = null
//            appDb.downloadDao.updateDownloadState(downloadInfo)
//            var song = appDb.songDao.getSong(songId)
//            downloadTracker.resumeDownloads(listOf(song!!._id))
//        }
//    }

    fun removeDownloadedSong(songId : String) = viewModelScope.launch {
         appDb.downloadDao.getDownload(songId)?.let {
            appDb.downloadDao.removeDownload(it)
        }
        appDb.songDao.getSong(songId)?.let {
            downloadTracker.removeDownload(songId)
            appDb.songDao.deleteSong(it)
        }

    }

    fun deleteAllDownloads(downloadList : List<DbDownloadInfo>) = viewModelScope.launch {
        downloadRepo.deleteAllDownload(downloadList)
    }

    fun getDownloads(state : Int) = downloadTracker.getDownloads(state)

}


