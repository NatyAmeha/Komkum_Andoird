package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.PlaylistSongDbJoin
import com.komkum.komkum.data.model.SongDbInfo

@Dao
interface PlaylistSongJoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlistSongJoinINfo : PlaylistSongDbJoin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playlistSongJoinInfo : List<PlaylistSongDbJoin>)


    @Query("SELECT * FROM SongDbInfo INNER JOIN PlaylistSongDbJoin on SongDbInfo._id = PlaylistSongDbJoin.songId WHERE PlaylistSongDbJoin.playlistId = :playlistId")
    suspend fun getPlaylistSongs(playlistId : String) : List<SongDbInfo>

    @Query("SELECT * FROM PlaylistSongDbJoin")
    suspend fun getAllPlaylistsSongs() : List<PlaylistSongDbJoin>

    @Delete
    suspend fun deletePlaylistsongs(playlistsongJoinInfo : PlaylistSongDbJoin)

    @Delete
    suspend fun deletePlaylistsongLists(playlistSongJoinInfo: List<PlaylistSongDbJoin>)

    @Query("DELETE  FROM PlaylistSongDbJoin")
    suspend fun deleteAll()
}