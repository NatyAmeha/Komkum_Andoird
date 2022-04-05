package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
class Download(
    var songs: List<DownloadInfo>,
    var albums: List<DownloadInfo>,
    var playlists: List<DownloadInfo>,
    var audiobooks : List<DownloadInfo>,
    var books : List<DownloadInfo>
) : Parcelable{
    companion object{
        const val DOWNLOAD_CATEGORY_MUSIC = 0
        const val DOWNLOAD_CATEGORY_AUDIOBOOK = 1
        const val DOWNLOAD_CATEGORY_BOOK = 2
        const val DOWNLOAD_CATEGORY_PODCAST = 3
    }
}

@Keep
@Parcelize
data class DownloadInfo(
    var id: String, // map songId , AlbumId , PlaylistId , bookId to id
    var date: Date
) : Parcelable

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