package com.zomatunes.zomatunes.util.extensions

import android.support.v4.media.session.MediaSessionCompat
import com.zomatunes.zomatunes.data.model.*

val <T> T.exhaustive: T
    get() = this


fun <T : BaseModel> List<T>.checkItemExistence(data: BaseModel): Boolean {
    return contains(data)
}

fun List<MediaSessionCompat.QueueItem>.toSongList() : List<Song<String,String>> {
    var description = map { queueItem -> queueItem.description }
    return  description.map { desc ->
        var _id = desc.mediaId
        var tittle = desc.title.toString()
        var genre = desc.subtitle.toString()
        var imagepath = desc.iconUri
        var lyrics = desc.extras?.getString("LYRICS")
        Song<String,String>(_id = _id!!, tittle = tittle, genre = genre, thumbnailPath = imagepath.toString())
    }
}

fun SongDbInfo.toSong(): Song<String, String> {
    return Song(_id=this._id , tittle = this.tittle ,trackNumber = this.trackNumber , trackLength = this.trackLength, thumbnailPath = this.thumbnailPath ,  songFilePath = this.songFilePath , mpdPath = this.mpdPath)
}

fun List<SongDbInfo>.toSong() : List<Song<String, String>>{
    return this.map { info ->
        Song<String,String>(_id=info._id , tittle = info.tittle , trackNumber = info.trackNumber , trackLength = info.trackLength, thumbnailPath = info.thumbnailPath , songFilePath = info.songFilePath , mpdPath = info.mpdPath)
    }
}


 inline fun <reified T> List<T>.toBaseModel() : List<BaseModel>? {
   return return when(T::class){
       Album::class ->{
           var albumList = this as List<Album<String,String>>
           albumList.map{album -> album.toBaseModel()}
       }

       else -> {
           null
       }
   }
}