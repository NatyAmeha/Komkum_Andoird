package com.example.ethiomusic.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.ethiomusic.data.model.DbDownloadInfo

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addDownloads(downloads : List<DbDownloadInfo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addDownload(downloadData : DbDownloadInfo)

    @Query("SELECT * FROM dbdownloadinfo")
    fun getDownloads() : LiveData<List<DbDownloadInfo>>

    @Query("SELECT * FROM dbdownloadinfo")
    fun getDownloadList() : List<DbDownloadInfo>

    @Query("SELECT * FROM dbdownloadinfo WHERE contentId =:contentId")
    suspend fun getDownload(contentId : String) : DbDownloadInfo?

//    @Query("SELECT * FROM dbdownloadinfo WHERE contentId =:contentId")
//    suspend fun getDownloadLivedata(contentId : String) : LiveData<DbDownloadInfo>

    @Update
    suspend fun updateDownloadState(downloadInfo: DbDownloadInfo)

    @Update
    suspend fun updateDownloadsState(donwloads : List<DbDownloadInfo>)

    @Delete
    suspend fun removeDownload(downloadInfo : DbDownloadInfo)

    @Delete()
    suspend fun removeDownloads(donwloadInfos : List<DbDownloadInfo>)
}