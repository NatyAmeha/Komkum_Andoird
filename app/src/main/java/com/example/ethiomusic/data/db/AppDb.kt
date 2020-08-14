package com.example.ethiomusic.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ethiomusic.data.model.*

@Database(entities = [SongDbInfo::class , AlbumDbInfo::class , PlaylistDbInfo::class , DbDownloadInfo::class ,
    SearchSuggestion::class, AlbumSongDbJoin::class , PlaylistSongDbJoin::class] , version = 1)
abstract class AppDb : RoomDatabase() {
    abstract val songDao : SongDao
    abstract val albumDao : AlbumDao
    abstract val downloadDao : DownloadDao
    abstract val playlistDao : PlaylistDao
    abstract val albumSongJoinDao : AlbumSongJoinDao
    abstract val playlistSongJoinDao : PlaylistSongJoinDao
}