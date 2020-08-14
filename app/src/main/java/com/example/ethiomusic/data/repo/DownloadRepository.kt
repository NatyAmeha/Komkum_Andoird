package com.example.ethiomusic.data.repo

import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.R
import com.example.ethiomusic.data.db.AppDb
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.ui.download.DownloadViewmodel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class DownloadRepository @Inject constructor(var appDb : AppDb , var downloadTracker: DownloadTracker ) {

    companion object{
        var SingleSongPlaylistId = "singlle_song_plalist_id"
    }

    fun getDownloads()  = appDb.downloadDao.getDownloads()

    suspend fun isInDownload(contentId : String) : Boolean {
        var download = appDb.downloadDao.getDownload(contentId)
        if(download != null) return true
        return false
    }


    suspend fun isDownloadedInOtherPlace(deletedFrom : Int ,songId : String) : Boolean{
        return  when(deletedFrom){
            DownloadViewmodel.DOWNLOAD_TYPE_ALBUM ->{
                //check wheather a song is downloaded in single song
                appDb.songDao.getSong(songId)?.let {
                    return true
                }
                var allPlaylistsSongs =  appDb.playlistSongJoinDao.getAllPlaylistsSongs()
                var isSongDownloadedInPlaylist = allPlaylistsSongs.map { song -> song.songId }.contains(songId)
                return isSongDownloadedInPlaylist
            }
            DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST ->{
                //check wheather a song is downloaded in single song
                appDb.songDao.getSong(songId)?.let {
                    return true
                }
                var allAlbumsSongs =  appDb.albumSongJoinDao.getAllAlbumsSongs()
                var isSongDownloadedInAlbum = allAlbumsSongs.map { song -> song.songId }.contains(songId)
                return isSongDownloadedInAlbum
            }
            DownloadViewmodel.DOWNLOAD_TYPE_SONG ->{
                var allPlaylistsSongs =  appDb.playlistSongJoinDao.getAllPlaylistsSongs()
                var isSongDownloadedInPlaylist = allPlaylistsSongs.map { song -> song.songId }.contains(songId)
                if (!isSongDownloadedInPlaylist){
                    var allAlbumsSongs =  appDb.albumSongJoinDao.getAllAlbumsSongs()
                    return allAlbumsSongs.map { song -> song.songId }.contains(songId)
                }
                return true
            }
            else -> false
        }
    }





    suspend fun downloadAlbum(album : Album<Song<String , String> , String>){
        var albuminfo = AlbumDbInfo(album._id , album.name!! , Date().toString() , album.albumCoverPath!! , album.genre!! , album.category!!)
        appDb.albumDao.saveAlbum(albuminfo)

        var songDbInfo = album.songs!!.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.thumbnailPath, song.songFilePath , song.mpdPath , null , albumName = album._id  , lyricsPath = null)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var albumSongJoinInfo = album.songs!!.map { song ->
            AlbumSongDbJoin(albuminfo._id , song._id)
        }
        appDb.albumSongJoinDao.insertAll(albumSongJoinInfo)

        var albumArtists = if(album.artists!!.size > 1){ album.artists!!.joinToString(", ") }
        else{ album.artists!![0] }

        var downloadInfo = DbDownloadInfo(null , album._id , album.name!! , DownloadViewmodel.DOWNLOAD_TYPE_ALBUM  , album.albumCoverPath!! , Date().toString() ,
            "${album.songs?.size} songs" , null , albumArtists)

        appDb.downloadDao.addDownload(downloadInfo)
    }

    suspend fun deleteDownloadedAlbum(albumId: String){
        withContext(Dispatchers.IO){
            appDb.downloadDao.getDownload(albumId)?.let {
                appDb.downloadDao.removeDownload(it)
                getDownloads()
            }

            //  delete song info
            var songs = appDb.albumSongJoinDao.getAlbumSongs(albumId)
            var albumSongDbInfo = songs.map { song ->
                AlbumSongDbJoin(albumId , song._id)
            }
            appDb.albumSongJoinDao.deleteAllAlbumsongs(albumSongDbInfo)

            //album delete code
            appDb.albumDao.getAlbum(albumId)?.let {
                appDb.albumDao.deleteAlbum(it)
            }
            //check if the song is downloaded other place and remove songfile
            var songIds = songs.map { song -> song._id }
            songIds.forEach { songId ->
                if(!isDownloadedInOtherPlace(DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST, songId)) downloadTracker.removeDownload(songId)
            }
        }
    }

    suspend fun downloadPlaylist(playlist: Playlist<Song<String,String>>){
        if (playlist._id.isNullOrBlank()) playlist._id =  playlist.songs!!.shuffled().take(3).map { song -> song._id }.joinToString(",")
        var playlistInfo = PlaylistDbInfo(playlist._id!! , playlist.name!! , Date().toString())
        appDb.playlistDao.savePlaylist(playlistInfo)

        var songDbInfo = playlist.songs!!.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.thumbnailPath, song.songFilePath , song.mpdPath , null , playlistName = playlist.name  , lyricsPath = null)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var playlistSongDbInfo = playlist.songs!!.map { song ->
            PlaylistSongDbJoin(playlistInfo._id , song._id)
        }
        appDb.playlistSongJoinDao.insertAll(playlistSongDbInfo)

        var downloadInfo = DbDownloadInfo(null , playlist._id!! , playlist.name!! , DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST  , R.drawable.backimage.toString(), Date().toString() ,
            "${playlist.songs?.size} songs" , null , null)
        appDb.downloadDao.addDownload(downloadInfo)
    }

    suspend fun deletePlaylist(playlistId: String){
        withContext(Dispatchers.IO){
            appDb.downloadDao.getDownload(playlistId)?.let {
                appDb.downloadDao.removeDownload(it)
                getDownloads()
            }

            var songs = appDb.playlistSongJoinDao.getPlaylistSongs(playlistId)

            var playlistSongDbInfo = songs.map { song ->
                PlaylistSongDbJoin(playlistId , song._id)
            }
            appDb.playlistSongJoinDao.deletePlaylistsongLists(playlistSongDbInfo)

            //playlist delete code
            appDb.playlistDao.getPlaylist(playlistId).let {
                appDb.playlistDao.deletePlaylist(it)
            }
            var songIds = songs.map { song -> song._id }
            songIds.forEach { songId ->
                if(!isDownloadedInOtherPlace(DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST, songId)) downloadTracker.removeDownload(songId)
            }
        }
    }

    suspend fun downloadSongs(songs : List<Song<String , String>>){

        var playlist = PlaylistDbInfo(_id = SingleSongPlaylistId , name = "Downloaded Songs" , dateCreated = Date().toString())
        appDb.playlistDao.savePlaylist(playlist)

        var songDbInfo = songs.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.thumbnailPath , song.songFilePath , song.mpdPath , null , null , null , lyricsPath = null)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var playlistSongDbInfo = songs.map { song ->
            PlaylistSongDbJoin(playlist._id , song._id)
        }
        appDb.playlistSongJoinDao.insertAll(playlistSongDbInfo)

//        var downloadInfos = songs.map { song ->
//            var artists = if(song.artists!!.size > 1){ song.artists!!.joinToString(", ") }
//            else{ song.artists!![0].toString() }
//            DbDownloadInfo(null , song._id , song.tittle!! , DownloadViewmodel.DOWNLOAD_TYPE_SONG , song.thumbnailPath!! , Date().toString() , null , null , artists )
//        }

        var downloadInfo = DbDownloadInfo(0 , playlist._id , playlist.name , DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST  , R.drawable.backimage.toString(), Date().toString() ,
            "${songs.size} songs" , null , null)
        appDb.downloadDao.addDownload(downloadInfo)
    }

    suspend fun removeAlbumSongsFromDownload(selectedSongsId : List<String> , albumId : String){
        withContext(Dispatchers.IO){
            var albumSongjoinINfo = selectedSongsId.map { songId ->
                AlbumSongDbJoin(albumId , songId)
            }
           appDb.albumSongJoinDao.deleteAllAlbumsongs(albumSongjoinINfo)
            selectedSongsId.forEach { songId ->
                if(!isDownloadedInOtherPlace(DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST, songId)) downloadTracker.removeDownload(songId)
            }
        }
    }

    suspend fun removePlaylistSongsFromDownload(selectedSongsId : List<String> , playlistId : String){
        withContext(Dispatchers.IO){
            var playlistSongjoinInfo = selectedSongsId.map { songId ->
                PlaylistSongDbJoin(playlistId , songId)
            }
            appDb.playlistSongJoinDao.deletePlaylistsongLists(playlistSongjoinInfo)
            selectedSongsId.forEach { songId ->
                if(!isDownloadedInOtherPlace(DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST, songId)) downloadTracker.removeDownload(songId)
            }
        }
    }


}