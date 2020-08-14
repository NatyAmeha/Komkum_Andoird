package com.example.ethiomusic.Downloader

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.WorkManager
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.util.NotificationHelper
import com.example.ethiomusic.util.PreferenceHelper
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import javax.inject.Inject

class MediaDownloaderService : DownloadService(NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_ID , NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_INTERVAL) {

     val JobId = 1

    @Inject
    lateinit var mediaDownloadManager: DownloadManager


    override fun onCreate() {
        var application = this.application as EthioMusicApplication
        application.appComponent.inject(this)
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

    override fun getForegroundNotification(downloads: MutableList<Download>?): Notification {
        var intent = Intent(this , MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(this , 2,  intent , PendingIntent.FLAG_UPDATE_CURRENT)
        var notification = DownloadNotificationHelper(this , NotificationHelper.MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID.toString())
            .buildProgressNotification(R.drawable.exo_notification_play , pendingIntent , "download" , downloads)
        return notification
    }

    override fun getScheduler(): Scheduler? =
        if(Util.SDK_INT >= 21 ) PlatformScheduler(this , 1)
      else null


    inner class MediaDownloadListner : DownloadManager.Listener{
        override fun onRequirementsStateChanged(downloadManager: DownloadManager?, requirements: Requirements?, notMetRequirements: Int) {
            Log.i("downloadreq" , "requirement changed")
            startForeground(this@MediaDownloaderService , MediaDownloaderService::class.java)
        }

    }
}