package com.example.ethiomusic.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.ethiomusic.data.api.AlbumApi
import com.example.ethiomusic.data.api.LibraryApi
import com.example.ethiomusic.data.db.AppDb
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.ui.download.DownloadViewmodel
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.apiDataRequestHelper
import com.example.ethiomusic.util.extensions.dataRequestHelper
import com.example.ethiomusic.util.extensions.toResource
import com.example.ethiomusic.util.extensions.toSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class AlbumRepository @Inject constructor(var albumApi: AlbumApi , var libraryApi: LibraryApi , var appDb : AppDb) {
    var error = MutableLiveData<String>()

    fun getAlbum(albumId : String , loadFromCache : Boolean? = false) = apiDataRequestHelper(albumId , fun(message : String) = error.postValue(message)){
        albumApi.getAbum(it!!)
    }

    suspend fun getNewAlbum(genre: String? = null) = dataRequestHelper<Any , List<Album<String,String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        albumApi.getNewalbums(genre)
    }

    suspend fun getPopularAlbum(genre : String? = null) = dataRequestHelper<Any , List<Album<String,String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        albumApi.getPopularalbums(genre)
    }

    fun getRecommendedAlbums() = apiDataRequestHelper<Any , List<Album<String,String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        albumApi.getRecommendedalbums()
    }

    suspend fun getAlbumBeta(albumId : String , loadFromCache: Boolean = false) : Resource<Album<Song<String , String> , String>>{
       return  if(loadFromCache){
           withContext(Dispatchers.IO){
               var album = appDb.albumDao.getAlbum(albumId)!!
               var albumSongs = appDb.songDao.getSongsByAlbumId(albumId).toSong()
               var alSongs = appDb.albumSongJoinDao.getAlbumSongs(albumId).toSong()
               var albumResult = Album<Song<String , String> , String>(_id = album._id , name = album.name , albumCoverPath =  album.albumCoverPath , songs = alSongs)
               Resource.Success(data = albumResult)
           }
       }
        else {
            dataRequestHelper(albumId , fun(message : String) = error.postValue(message)){
                albumApi.getAbum(it!!)
            }
        }
    }

    suspend fun downloadAlbum(album : Album<Song<String , String> , String>){
        var albuminfo = AlbumDbInfo(album._id , album.name!! , Date().toString() , album.albumCoverPath!! , album.genre!! , album.category!!)
        appDb.albumDao.saveAlbum(albuminfo)

        var songDbInfo = album.songs!!.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.songFilePath , null , null , album._id , lyricsPath = null)
        }
        appDb.songDao.saveSongs(songDbInfo)
        var albumArtists = if(album.artists!!.size > 1){ album.artists!!.joinToString(", ") }
        else{ album.artists!![0] }

        var downloadInfo = DbDownloadInfo(null , album._id , album.name!! , DownloadViewmodel.DOWNLOAD_TYPE_ALBUM  , album.albumCoverPath!! , Date().toString() ,
            "${album.songs?.size} songs" , null , albumArtists)

        appDb.downloadDao.addDownload(downloadInfo)
    }

    suspend fun downloadAlbumSongs(songs : List<Song<String , String>>){
        var songDbInfo = songs.map { song ->
            SongDbInfo(song._id , song.tittle , song.trackNumber , song.trackLength , Date().toString() , song.songFilePath , null , null , null , lyricsPath = null)
        }
        appDb.songDao.saveSongs(songDbInfo)

        var downloadInfos = songs.map { song ->
            var artists = if(song.artists!!.size > 1){ song.artists!!.joinToString(", ") }
            else{ song.artists!![0].toString() }
            DbDownloadInfo(null , song._id , song.tittle!! , DownloadViewmodel.DOWNLOAD_TYPE_SONG , song.thumbnailPath!! , Date().toString() , null , null , artists )
        }
        appDb.downloadDao.addDownloads(downloadInfos)
    }


     fun getPopularAlbums(filter : String) = apiDataRequestHelper<Any , List<Album<String , String>>>(errorHandler = fun(message : String){error.postValue(message)}){
        albumApi.getPopularAlbums(filter)
     }

    suspend  fun getPopularAlbumsBeta(filter : String) = dataRequestHelper<Any , List<Album<String , String>>>(errorHandler = fun(message : String) = error.postValue(message)){
        albumApi.getPopularAlbums(filter)
    }

    suspend fun getFavoriteAlbums() = dataRequestHelper("album" , fun(message : String) = error.postValue(message)){
        libraryApi.getFavorite(it!!)
    }

    suspend fun addTofavorite(albumIds : List<String>) = dataRequestHelper(albumIds , fun(message : String) = error.postValue(message)){
         albumApi.addTofavorite(albumIds)
    }

    suspend fun removeFavAlbum(albumIds: List<String>) = dataRequestHelper(albumIds ,fun(message : String) = error.postValue(message)){
        albumApi.removeFromFavorite(it!!)
    }



    suspend fun isAlbumInfavorite(albumId : String) : Boolean{
        return try {
            withContext(Dispatchers.IO){
                var result = albumApi.checkAlbumInFavorites(Album(_id = albumId)).toResource()
                return@withContext result.data!!
            }

        }catch (ex : Throwable){
            Log.i("apierror" , ex.message)
            error.postValue( ex.message)
             false
        }
    }
}