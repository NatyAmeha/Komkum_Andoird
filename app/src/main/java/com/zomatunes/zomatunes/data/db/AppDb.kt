package com.zomatunes.zomatunes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zomatunes.zomatunes.data.model.*

@Database(
    entities = [SongDbInfo::class, AlbumDbInfo::class, PlaylistDbInfo::class, DbDownloadInfo::class,
        SearchSuggestion::class, AlbumSongDbJoin::class, PlaylistSongDbJoin::class, AudioBookDbInfo::class, ChapterDbInfo::class, EbookDbInfo::class,
        Friend::class], version = 2
)
abstract class AppDb : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val albumDao: AlbumDao
    abstract val downloadDao: DownloadDao
    abstract val playlistDao: PlaylistDao
    abstract val albumSongJoinDao: AlbumSongJoinDao
    abstract val playlistSongJoinDao: PlaylistSongJoinDao
    abstract val audiobookDao: AudiobookDao
    abstract val ebookDao : EbookDao
    abstract val chapterDao: ChapterDao

    abstract val friendsDao: FriendsDao
}