package com.komkum.komkum.data.repo

import android.util.Log
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.R
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.*
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class DownloadRepository @Inject constructor(var appDb : AppDb , var downloadTracker: DownloadTracker ) {

    companion object{
        var SingleSongPlaylistId = "singlle_song_plalist_id"
    }

    fun getDownloads()  = appDb.downloadDao.getDownloads()

    suspend fun getDownloadsBeta() = appDb.downloadDao.getDownloadsBeta()

    suspend fun isInDownload(contentId : String) : Boolean {
        var download = appDb.downloadDao.getDownload(contentId)
        if(download != null) return true
        return false
    }


    suspend fun isDownloadedInOtherPlace(songId : String) : Boolean{
        var allPlaylistsSongs =  appDb.playlistSongJoinDao.getAllPlaylistsSongs()
        var isSongDownloadedInOtherPlaylist = allPlaylistsSongs.map { song -> song.songId }.contains(songId)
        return if(isSongDownloadedInOtherPlaylist) true
        else{
            var allAlbumsSongs =  appDb.albumSongJoinDao.getAllAlbumsSongs()
            var isSongDownloadedInOtherAlbum = allAlbumsSongs.map { song -> song.songId }.contains(songId)
            isSongDownloadedInOtherAlbum
        }
    }



    suspend fun downloadAlbum(album : Album<Song<String , String> , String>){
        var albuminfo = AlbumDbInfo(album._id , album.name!! , Date().toString() , album.albumCoverPath!! , album.genre!!
            , album.category!! , artistsName = album.artistsName?.joinToString(", ") ?: "")
        appDb.albumDao.saveAlbum(albuminfo)

        var songDbInfo = album.songs!!.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.thumbnailPath, song.songFilePath , song.mpdPath , song.artistsName?.joinToString(", ") , albumName = album.name  , lyricsPath = song.lyrics)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var albumSongJoinInfo = album.songs!!.map { song ->
            AlbumSongDbJoin(albuminfo._id , song._id)
        }
        appDb.albumSongJoinDao.insertAll(albumSongJoinInfo)

        var albumArtists = if(album.artists!!.size > 1){ album.artistsName?.joinToString(", ") }
        else{ album.genre }

        var downloadInfo = DbDownloadInfo(null , album._id , album.name!! , DownloadViewmodel.DOWNLOAD_TYPE_ALBUM , Download.DOWNLOAD_CATEGORY_MUSIC  , album.albumCoverPath!! , Date().toString() ,
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
            var albumSongDbInfo = songs?.map { song ->
                AlbumSongDbJoin(albumId , song._id)
            }
            albumSongDbInfo?.let { appDb.albumSongJoinDao.deleteAllAlbumsongs(it) }

            //album delete code
            appDb.albumDao.getAlbum(albumId)?.let {
                appDb.albumDao.deleteAlbum(it)
            }
            //check if the song is downloaded other place and remove songfile
            var songIds = songs?.map { song -> song._id }
            songIds?.forEach { songId ->
                if(!isDownloadedInOtherPlace(songId)){
                    downloadTracker.removeDownload(songId)
                    songs?.find { song -> song._id ==songId }?.let {
                        appDb.songDao.deleteSong(it)
                    }
                }
            }
        }
    }



    suspend fun downloadPlaylist(playlist: Playlist<Song<String,String>>){
        if (playlist._id.isNullOrBlank()) playlist._id =  playlist.songs!!.shuffled().take(3).map { song -> song._id }.joinToString(",")
        var playlistInfo = PlaylistDbInfo(playlist._id!! , playlist.name!! , Date().toString())
        appDb.playlistDao.savePlaylist(playlistInfo)

        var songDbInfo = playlist.songs!!.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.thumbnailPath, song.songFilePath , song.mpdPath , song.artistsName?.joinToString(", ") , playlistName = playlist.name  , lyricsPath = song.lyrics)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var playlistSongDbInfo = playlist.songs!!.map { song ->
            PlaylistSongDbJoin(playlistInfo._id , song._id)
        }
        appDb.playlistSongJoinDao.insertAll(playlistSongDbInfo)

        var downloadInfo = DbDownloadInfo(null , playlist._id!! , playlist.name!! , DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST , Download.DOWNLOAD_CATEGORY_MUSIC  , R.drawable.music_placeholder.toString(), Date().toString() ,
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
            appDb.playlistDao.getPlaylist(playlistId).let { appDb.playlistDao.deletePlaylist(it) }
            var songIds = songs.map { song -> song._id }
            songIds.forEach { songId ->
                if(!isDownloadedInOtherPlace(songId)){
                    downloadTracker.removeDownload(songId)
                    songs.find { song -> song._id ==songId }?.let {
                        appDb.songDao.deleteSong(it)
                    }
                }
            }
        }
    }

    suspend fun deleteDownloadedEpisode(episodeId: String) : Boolean{
        appDb.downloadDao.getDownload(episodeId)?.let {
            appDb.downloadDao.removeDownload(it)
        }
        appDb.episodeDao.getEpisode(episodeId)?.let {
            appDb.episodeDao.deleteEpisode(it)
            downloadTracker.removeDownload(it._id)
            return true
        }

        return false
    }

    suspend fun deleteAllDownload(downloadList : List<DbDownloadInfo>){
        withContext(Dispatchers.IO){
            var allIds = appDb.albumSongJoinDao.getAllAlbumsSongs().map { albumSongDbJoin -> albumSongDbJoin.songId }.toMutableList()
            var allPlaylistSongsId = appDb.playlistSongJoinDao.getAllPlaylistsSongs().map { playlistSongDbJoin -> playlistSongDbJoin.songId }
            allIds.addAll(allPlaylistSongsId)



            downloadList.filter { dn -> dn.type == DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK }
                .map { download -> download.contentId }
                .forEach { deleteDownloadedAudioBook(it) }

            downloadList.filter { dn -> dn.type == DownloadViewmodel.DOWNLOAD_TYPE_EBOOK }
                .map { download -> download.contentId }
                .forEach { deleteEbook(it) }

            var episodeIds = appDb.episodeDao.getEpisodes().map { episode -> episode._id }
            allIds.addAll(episodeIds)

            //delete remaining download info from db
            appDb.downloadDao.removeDownloads(downloadList)
            getDownloads()

            appDb.albumSongJoinDao.deleteAll()
            appDb.playlistSongJoinDao.deleteAll()
            appDb.songDao.deleteAll()
            appDb.albumDao.deleteAll()
            appDb.playlistDao.deleteAll()
            appDb.episodeDao.deleteAllEpisodes()

            downloadTracker.removeDownlaods(allIds)
        }
    }


    suspend fun isDownloadAvailable(contentId: String) : Boolean{
        var result = appDb.downloadDao.getDownload(contentId)
        return result != null
    }

    suspend fun downloadAudioBook(audiobook: Audiobook<Chapter , Author<String , String>>) : Boolean{
        //check if downloaded before
        if(isDownloadAvailable(audiobook._id!!)){
            return false
        }
        else{
            var bookdbInfo = AudioBookDbInfo(audiobook._id!! , audiobook.name!! ,audiobook.releaseDate.toString(), audiobook.coverImagePath!! , audiobook.authorName!!.joinToString(", ") , length =  audiobook.length)
            appDb.audiobookDao.saveBook(bookdbInfo)

            var chapterDbinfos = audiobook.chapters!!.map {
                ChapterDbInfo(it._id , it.chapterNumber!! , it.name!! , it.duration!! , it.mpdPath!! , it.chapterfilePath!! , audiobook._id!!)
            }
            appDb.chapterDao.saveChapters(chapterDbinfos)

            var downloadInfo = DbDownloadInfo(null , audiobook._id!! , audiobook.name!! , DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK , Download.DOWNLOAD_CATEGORY_AUDIOBOOK , audiobook.coverImagePath!! ,
                Date().toString() ,audiobook.authorName!!.joinToString(", ") , null , audiobook.authorName!!.joinToString(", "))

            appDb.downloadDao.addDownload(downloadInfo)
            return true
        }
    }

    suspend fun saveEbookDownloadToDb(ebook : EBook<Author<String,String>>) : Boolean{
        if(isDownloadAvailable(ebook._id!!)){
            return false
        }
        else{
            return try {
                var ebookDbInfo = EbookDbInfo(ebook._id!!  , ebook.name!! , ebook.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL),
                    ebook.authorName!!.joinToString(", ") , 0)
                appDb.ebookDao.saveBook(ebookDbInfo)

                var downloadInfo = DbDownloadInfo(null , ebook._id!! , ebook.name!! , DownloadViewmodel.DOWNLOAD_TYPE_EBOOK , Download.DOWNLOAD_CATEGORY_BOOK ,
                    ebook.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL), Date().toString() , ebook.authorName!!.joinToString(", ") , null , ebook.authorName!!.joinToString(", "))
                appDb.downloadDao.addDownload(downloadInfo)
                true
            }catch (ex : Throwable){
                ex.message?.let { Log.i("dberror" , it) }
                false
            }

        }
    }

    suspend fun deleteDownloadedAudioBook(audiobookId : String){
        withContext(Dispatchers.IO){
            appDb.downloadDao.getDownload(audiobookId)?.let {
                appDb.downloadDao.removeDownload(it)
//                getDownloads()
            }
            var bookChaptersId = appDb.chapterDao.getBookChapters(audiobookId).map { chapterDbInfo -> chapterDbInfo._id }
            downloadTracker.removeDownlaods(bookChaptersId)

            appDb.chapterDao.deleteBookChapters(audiobookId)
            appDb.audiobookDao.deleteBook(audiobookId)
        }
    }

    suspend fun deleteEbook(ebookId : String){
        withContext(Dispatchers.IO){
            appDb.downloadDao.getDownload(ebookId)?.let {
                appDb.downloadDao.removeDownload(it)
                appDb.ebookDao.getBook(ebookId)?.let {
                    downloadTracker.deleteEbookFile(ebookId)
                    appDb.ebookDao.deleteBook(ebookId)
                }
            }
        }
    }


    suspend fun downloadSongs(songs : List<Song<String , String>>){

        var playlist = PlaylistDbInfo(_id = SingleSongPlaylistId , name = "Downloaded Songs" , dateCreated = Date().toString())
        appDb.playlistDao.savePlaylist(playlist)

        var songDbInfo = songs.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.thumbnailPath , song.songFilePath , song.mpdPath , song.artistsName?.joinToString(", ") , null , null , lyricsPath = song.lyrics)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var playlistSongDbInfo = songs.map { song -> PlaylistSongDbJoin(playlist._id , song._id) }

        appDb.playlistSongJoinDao.insertAll(playlistSongDbInfo)

//        var downloadInfos = songs.map { song ->
//            var artists = if(song.artists!!.size > 1){ song.artists!!.joinToString(", ") }
//            else{ song.artists!![0].toString() }
//            DbDownloadInfo(null , song._id , song.tittle!! , DownloadViewmodel.DOWNLOAD_TYPE_SONG , song.thumbnailPath!! , Date().toString() , null , null , artists )
//        }

        var downloadInfo = DbDownloadInfo(0 , playlist._id , playlist.name , DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST , Download.DOWNLOAD_CATEGORY_MUSIC , R.drawable.music_placeholder.toString(), Date().toString() ,
            "${songs.size} songs" , null , null)
        appDb.downloadDao.addDownload(downloadInfo)
    }


    suspend fun saveEpisodetoDatabase(episode : PodcastEpisode){
        var episodeDbInfo = EpisodeDBInfo(episode._id , tittle = episode.tittle , description = episode.description , duration = episode.duration , podcast = episode.podcast,
        podcastName = episode.podcastName , filePath = episode.filePath , date = episode.dateCreated.toString() , thumbnail = episode.thumbnailPath)
        appDb.episodeDao.saveEpisode(episodeDbInfo)

        var downloadInfo = DbDownloadInfo(null , episode._id , episode.tittle!! , DownloadViewmodel.DOWNLOAD_TYPE_EPISODE , Download.DOWNLOAD_CATEGORY_PODCAST , episode.thumbnailPath!!, Date().toString() ,
            episode.podcastName , null , null)
        appDb.downloadDao.addDownload(downloadInfo)
    }


    suspend fun getEpisode(episodeId : String) = appDb.episodeDao.getEpisode(episodeId)

    suspend fun removeAlbumSongsFromDownload(selectedSongsId : List<String> , albumId : String){
        withContext(Dispatchers.IO){
            var albumSongjoinINfo = selectedSongsId.map { songId ->
                AlbumSongDbJoin(albumId , songId)
            }
           appDb.albumSongJoinDao.deleteAllAlbumsongs(albumSongjoinINfo)
            selectedSongsId.forEach { songId ->
                if(!isDownloadedInOtherPlace(songId)){
                    downloadTracker.removeDownload(songId)
                    appDb.songDao.getSong(songId)?.let { appDb.songDao.deleteSong(it) }
                }
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
                if(!isDownloadedInOtherPlace(songId)){
                    downloadTracker.removeDownload(songId)
                    appDb.songDao.getSong(songId)?.let { appDb.songDao.deleteSong(it) }
                }
            }
        }
    }



    fun updateChapterCurrentPositionAndDuration(chapterId : String, position : Long, duration : Int = 0){
        CoroutineScope(Dispatchers.IO).launch{
            appDb.chapterDao.updateChapterCurrentPositionAndDuration(chapterId , position , duration)
        }
    }

    fun updateChapterPosition(chapterId : String, position : Long){
        CoroutineScope(Dispatchers.IO).launch{
            appDb.chapterDao.updateChapterCurrentPosition(chapterId , position)
        }
    }

    fun updateAudiobookCurrentChapter(bookId : String , chapterIndex: Int){
        CoroutineScope(Dispatchers.IO).launch{
            appDb.audiobookDao.updateLastListeningChapter(bookId , chapterIndex)
        }
    }

    fun updateChapterState(chapterId: String , status : Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            appDb.chapterDao.updateChapterCompletionState(chapterId , status)
        }
    }


}