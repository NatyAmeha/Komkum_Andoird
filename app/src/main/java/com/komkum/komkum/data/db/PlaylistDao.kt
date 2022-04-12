package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.AlbumDbInfo
import com.komkum.komkum.data.model.DbDownloadInfo
import com.komkum.komkum.data.model.PlaylistDbInfo

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun savePlaylist(playlist : PlaylistDbInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun savePlaylists(playlists : List<PlaylistDbInfo>)

    @Query("SELECT * FROM playlistdbinfo WHERE _id = :playlistId")
     fun getPlaylist(playlistId : String) : PlaylistDbInfo

    @Delete
     fun deletePlaylist(playlist : PlaylistDbInfo)

    @Query("DELETE  FROM playlistdbinfo")
     fun deleteAll()
}