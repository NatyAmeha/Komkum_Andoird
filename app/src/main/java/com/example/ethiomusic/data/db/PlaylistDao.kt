package com.example.ethiomusic.data.db

import androidx.room.*
import com.example.ethiomusic.data.model.AlbumDbInfo
import com.example.ethiomusic.data.model.DbDownloadInfo
import com.example.ethiomusic.data.model.PlaylistDbInfo

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun savePlaylist(playlist : PlaylistDbInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun savePlaylists(playlists : List<PlaylistDbInfo>)

    @Query("SELECT * FROM playlistdbinfo WHERE _id = :playlistId")
    suspend fun getPlaylist(playlistId : String) : PlaylistDbInfo

    @Delete
    suspend fun deletePlaylist(playlist : PlaylistDbInfo)

    @Query("DELETE  FROM playlistdbinfo")
    suspend fun deleteAll()
}