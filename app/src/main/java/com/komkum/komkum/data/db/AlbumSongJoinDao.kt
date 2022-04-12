package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.AlbumSongDbJoin
import com.komkum.komkum.data.model.SongDbInfo

@Dao
interface AlbumSongJoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(albumSongJoinINfo : AlbumSongDbJoin)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAll(albumSongJoinINfo : List<AlbumSongDbJoin>)

    @Query("SELECT * FROM SongDbInfo INNER JOIN AlbumSongDbJoin ON SongDbInfo._id = AlbumSongDbJoin.songId WHERE AlbumSongDbJoin.albumId = :albumId")
     fun getAlbumSongs(albumId : String) : List<SongDbInfo>?

    @Query("SELECT * FROM AlbumSongDbJoin")
     fun getAllAlbumsSongs() : List<AlbumSongDbJoin>

    @Delete
     fun deleteAlbumsongs(albumSongJoinINfo : AlbumSongDbJoin)

    @Delete
     fun deleteAllAlbumsongs(albumSongJoinINfo : List<AlbumSongDbJoin>)

    @Query("DELETE  FROM albumsongdbjoin")
     fun deleteAll()
}