package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.SongDbInfo

@Dao
interface SongDao {
    // removing suspend to avoid build failure

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun saveSong(song : SongDbInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun saveSongs(songList : List<SongDbInfo>)

    @Query("SELECT * FROM songdbinfo WHERE albumId = :albumId")
    fun getSongsByAlbumId(albumId : String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE playlistId = :playlistId")
    fun getSongsByPaylistId(playlistId : String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE _id = :songId")
     fun getSong(songId : String) : SongDbInfo?

    @Query("SELECT * FROM songdbinfo WHERE _id IN (:songIds) AND albumId = :albumId")
     fun getAlbumSongs(songIds: List<String>, albumId: String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE _id IN (:songIds) AND playlistId = :playlistId")
     fun getPlaylistSongs(songIds: List<String>, playlistId: String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE _id = :songId")
     fun getSongs(songId : String) : List<SongDbInfo>


    @Delete
     fun deleteSong(song : SongDbInfo)

    @Delete
     fun deleteSongs(songList : List<SongDbInfo>)

    @Query("DELETE  FROM songdbinfo")
     fun deleteAll()
}