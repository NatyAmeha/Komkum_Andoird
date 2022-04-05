package com.komkum.komkum.Downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.navigation.NavDeepLinkBuilder
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.util.NotificationHelper
import com.komkum.komkum.util.PreferenceHelper
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaDownloaderService : DownloadService(NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_ID , NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_INTERVAL) {

     val JobId = 1

    @Inject
    lateinit var mediaDownloadManager: DownloadManager


    override fun onCreate() {
        super.onCreate()
    }

    override fun getDownloadManager(): DownloadManager {
        var taskNumber =  PreferenceHelper.getInstance(this@MediaDownloaderService).getString("download_task" , "3")!!.toInt()
        var downloadonWifi = PreferenceHelper.getInstance(this).getBoolean("download_on_wifi" , false)
        mediaDownloadManager.maxParallelDownloads = taskNumber
            if(downloadonWifi){
                var requirement = Requirements(Requirements.NETWORK_UNMETERED)
              mediaDownloadManager.requirements = requirement
                Log.i("downloadrrr" , mediaDownloadManager.requirements.requirements.toString())
            }
        mediaDownloadManager.addListener(MediaDownloadListner())
        return mediaDownloadManager
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {

        var pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_navigation)
            .setDestination(R.id.downloadListFragment)
            .createPendingIntent()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            var channel = NotificationChannel(NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID.toString(), "Download" , NotificationManager.IMPORTANCE_DEFAULT)
            NotificationHelper.getNotificationManager(this).createNotificationChannel(channel)
        }
        var notification = DownloadNotificationHelper(this , NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID.toString())
            .buildProgressNotification(this , R.drawable.exo_notification_play , pendingIntent , "" , downloads)
        return notification
    }

    override fun getScheduler(): Scheduler? =
        if(Util.SDK_INT >= 21 ) PlatformScheduler(this , 1)
      else null


    inner class MediaDownloadListner : DownloadManager.Listener{
        override fun onRequirementsStateChanged(downloadManager: DownloadManager, requirements: Requirements, notMetRequirements: Int) {
            Log.i("downloadreq" , "requirement changed")
            startForeground(this@MediaDownloaderService , MediaDownloaderService::class.java)
        }

    }
}