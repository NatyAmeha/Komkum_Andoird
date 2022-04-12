package com.komkum.komkum.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.komkum.komkum.data.model.DbDownloadInfo

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun addDownloads(downloads : List<DbDownloadInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun addDownload(downloadData : DbDownloadInfo)

    @Query("SELECT * FROM DbDownloadInfo")
    fun getDownloads() : LiveData<List<DbDownloadInfo>>

    @Query("SELECT * FROM DbDownloadInfo")
     fun getDownloadsBeta() : List<DbDownloadInfo>

    @Query("SELECT * FROM dbdownloadinfo")
    fun getDownloadList() : List<DbDownloadInfo>

    @Query("SELECT * FROM dbdownloadinfo WHERE contentId =:contentId")
     fun getDownload(contentId : String) : DbDownloadInfo?

//    @Query("SELECT * FROM dbdownloadinfo WHERE contentId =:contentId")
//    suspend fun getDownloadLivedata(contentId : String) : LiveData<DbDownloadInfo>

    @Update
     fun updateDownloadState(downloadInfo: DbDownloadInfo)

    @Update
     fun updateDownloadsState(donwloads : List<DbDownloadInfo>)

    @Delete
     fun removeDownload(downloadInfo : DbDownloadInfo)

    @Delete
     fun removeDownloads(donwloadInfos : List<DbDownloadInfo>)
}