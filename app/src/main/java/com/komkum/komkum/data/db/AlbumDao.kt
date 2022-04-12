package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.AlbumDbInfo

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun saveAlbum(album : AlbumDbInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun saveAlbums(albumList : List<AlbumDbInfo>)

    @Query("SELECT * FROM albumdbinfo WHERE _id = :albumId")
     fun getAlbum(albumId : String) : AlbumDbInfo?

    @Delete
     fun deleteAlbum(album : AlbumDbInfo)

    @Query("DELETE  FROM albumdbinfo")
     fun deleteAll()
}