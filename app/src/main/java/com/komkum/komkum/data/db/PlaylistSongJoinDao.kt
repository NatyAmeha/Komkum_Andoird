package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.PlaylistSongDbJoin
import com.komkum.komkum.data.model.SongDbInfo

@Dao
interface PlaylistSongJoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(playlistSongJoinINfo : PlaylistSongDbJoin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(playlistSongJoinInfo : List<PlaylistSongDbJoin>)


    @Query("SELECT * FROM SongDbInfo INNER JOIN PlaylistSongDbJoin on SongDbInfo._id = PlaylistSongDbJoin.songId WHERE PlaylistSongDbJoin.playlistId = :playlistId")
     fun getPlaylistSongs(playlistId : String) : List<SongDbInfo>

    @Query("SELECT * FROM PlaylistSongDbJoin")
     fun getAllPlaylistsSongs() : List<PlaylistSongDbJoin>

    @Delete
     fun deletePlaylistsongs(playlistsongJoinInfo : PlaylistSongDbJoin)

    @Delete
     fun deletePlaylistsongLists(playlistSongJoinInfo: List<PlaylistSongDbJoin>)

    @Query("DELETE  FROM PlaylistSongDbJoin")
     fun deleteAll()
}