package com.example.ethiomusic.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.ethiomusic.data.model.AlbumDbInfo

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveAlbum(album : AlbumDbInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveAlbums(albumList : List<AlbumDbInfo>)

    @Query("SELECT * FROM albumdbinfo WHERE _id = :albumId")
    suspend fun getAlbum(albumId : String) : AlbumDbInfo?

    @Delete
    suspend fun deleteAlbum(album : AlbumDbInfo)

    @Query("DELETE  FROM albumdbinfo")
    suspend fun deleteAll()
}