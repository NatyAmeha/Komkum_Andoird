package com.zomatunes.zomatunes.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.zomatunes.zomatunes.data.model.DbDownloadInfo

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownloads(downloads : List<DbDownloadInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownload(downloadData : DbDownloadInfo)

    @Query("SELECT * FROM DbDownloadInfo")
    fun getDownloads() : LiveData<List<DbDownloadInfo>>

    @Query("SELECT * FROM DbDownloadInfo")
    suspend fun getDownloadsBeta() : List<DbDownloadInfo>

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

    @Delete
    suspend fun removeDownloads(donwloadInfos : List<DbDownloadInfo>)
}