package com.zomatunes.zomatunes.data.db

import androidx.room.*
import com.zomatunes.zomatunes.data.model.AlbumSongDbJoin
import com.zomatunes.zomatunes.data.model.SongDbInfo

@Dao
interface AlbumSongJoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(albumSongJoinINfo : AlbumSongDbJoin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albumSongJoinINfo : List<AlbumSongDbJoin>)

    @Query("SELECT * FROM SongDbInfo INNER JOIN AlbumSongDbJoin ON SongDbInfo._id = AlbumSongDbJoin.songId WHERE AlbumSongDbJoin.albumId = :albumId")
    suspend fun getAlbumSongs(albumId : String) : List<SongDbInfo>?

    @Query("SELECT * FROM AlbumSongDbJoin")
    suspend fun getAllAlbumsSongs() : List<AlbumSongDbJoin>

    @Delete
    suspend fun deleteAlbumsongs(albumSongJoinINfo : AlbumSongDbJoin)

    @Delete
    suspend fun deleteAllAlbumsongs(albumSongJoinINfo : List<AlbumSongDbJoin>)

    @Query("DELETE  FROM albumsongdbjoin")
    suspend fun deleteAll()
}