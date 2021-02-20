package com.zomatunes.zomatunes.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

class Download(
    var songs: List<DownloadInfo>,
    var albums: List<DownloadInfo>,
    var playlists: List<DownloadInfo>,
    var audiobooks : List<DownloadInfo>,
    var books : List<DownloadInfo>
){
    companion object{
        const val DOWNLOAD_CATEGORY_MUSIC = 0
        const val DOWNLOAD_CATEGORY_AUDIOBOOK = 1
        const val DOWNLOAD_CATEGORY_BOOK = 2
    }
}


data class DownloadInfo(
    var id: String, // map songId , AlbumId , PlaylistId , bookId to id
    var date: Date
)

data class DailyDownloadResult(
    var albumSongDownloads: List<String>,
    var singleSongDownloads: List<String>,
    var playlistSongDownloads: List<String>
)


@Entity
data class DbDownloadInfo(
    var _id: Int?,

    @PrimaryKey
    var contentId: String,      //song, album, or playlist id , audiobook id
    var name: String,
    var type: Int,                 // IDENTIfy  song playlit album , audiobook audiobook
    var category : Int,            // idnetify music audiobook podcast
    var imagePath: String,
    var date: String,
    var description: String? = null,
    var state: String? = null,
    var artists: String?
)

data class DownloadType(
    var type : String,
    var downloads : List<DbDownloadInfo>? = null
)