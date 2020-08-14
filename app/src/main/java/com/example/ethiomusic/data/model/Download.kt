package com.example.ethiomusic.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

class Download(
    var songs: Array<DownloadInfo>,
    var albums: Array<DownloadInfo>,
    var playlists: Array<DownloadInfo>
)


data class DownloadInfo(
    var id: String, // map songId , AlbumId , PlaylistId to id
    var date: Date
)

data class DailyDownloadResult(
    var albumSongDownloads: List<String>,
    var singleSongDownloads: List<String>,
    var playlistSongDownloads: List<String>
)


@Entity
data class DbDownloadInfo(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var _id: Int?,
    var contentId: String,      //song, album, or playlist id
    var name: String,
    var type: Int,
    var imagePath: String,
    var date: String,
    var description: String? = null,
    var state: String? = null,
    var artists: String?

)