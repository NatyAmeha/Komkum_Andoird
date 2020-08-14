package com.example.ethiomusic.data.db

import androidx.room.*
import com.example.ethiomusic.data.model.SongDbInfo

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSong(song : SongDbInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSongs(songList : List<SongDbInfo>)

    @Query("SELECT * FROM songdbinfo WHERE albumId = :albumId")
    fun getSongsByAlbumId(albumId : String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE playlistId = :playlistId")
    fun getSongsByPaylistId(playlistId : String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE _id = :songId")
    suspend fun getSong(songId : String) : SongDbInfo?

    @Query("SELECT * FROM songdbinfo WHERE _id IN (:songIds) AND albumId = :albumId")
    suspend fun getAlbumSongs(songIds: List<String>, albumId: String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE _id IN (:songIds) AND playlistId = :playlistId")
    suspend fun getPlaylistSongs(songIds: List<String>, playlistId: String) : List<SongDbInfo>

    @Query("SELECT * FROM songdbinfo WHERE _id = :songId")
    suspend fun getSongs(songId : String) : List<SongDbInfo>


    @Delete
    suspend fun deleteSong(song : SongDbInfo)

    @Delete
    suspend fun deleteSongs(songList : List<SongDbInfo>)

    @Query("DELETE  FROM songdbinfo")
    suspend fun deleteAll()
}