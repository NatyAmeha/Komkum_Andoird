package com.zomatunes.zomatunes.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.novoda.downloadmanager.DownloadBatchStatus
import com.novoda.downloadmanager.NotificationCustomizer
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import java.lang.Exception


object  NotificationHelper {

    const val PLAYBACK_NOTIFICATION_CHANNEL_ID = 1
    const val PLAYBACK_NOTIFICATION_ID = 2

    const val MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID = 3
    const val MEDIA_DOWNLOAD_NOTIFICATION_ID = 4
    const val NEW_ALBUM_NOTIFICATION_ID = 5
    const val NEW_SONG_NOTIFICATION_iD = 100
    const val MEDIA_DOWNLOAD_NOTIFICATION_INTERVAL = 1000L

    const val NEW_ALBUM_CHANNEL_ID = "NEW_ALBUM"
    const val FCM_NOTIFICATION_CHANNEL_ID = "MUSIC_NOTIFICATION_ID"
    const val FCM_NOTIFICATION_CHANNEL_NAME = "Music"

    const val NOTIFICATION_DESTINATION_ARTIST = "Artist"
    const val NOTIFICATION_DESTINATION_SONGG = "Song"



    fun getNotificationManager(context: Context)  =  NotificationManagerCompat.from(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, channelId: String, name: String, importance: Int){
        var channel = NotificationChannel(channelId, name, importance)
        getNotificationManager(context).createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBookDownloadNotifcationChannel(context : Context){
        var channel = NotificationChannel( "BOOK_DOWNLOAD_CHANNEL" , "Download"  , NotificationManager.IMPORTANCE_HIGH)
        getNotificationManager(context).createNotificationChannel(channel)
    }


    fun showFcmMusicNotification(context: Context, tittle: String?, body: String?, imageUrl: String?, intent: PendingIntent){
        var notificationManager = getNotificationManager(context)
        var notificationId = NEW_SONG_NOTIFICATION_iD + 1
//        var largeImage = context.resources.getDrawable(R.drawable.circularimg).toBitmap()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(context, FCM_NOTIFICATION_CHANNEL_ID, FCM_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        var notificationBuilder = NotificationCompat.Builder(context, FCM_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(tittle)
            .setContentText(body)
            .setAutoCancel(false)
//            .setStyle(NotificationCompat.Style())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_audiotrack_black_24dp)
            .setContentIntent(intent)
        notificationManager.notify(notificationId, notificationBuilder.build())


       var a =  Picasso.get().load(imageUrl?.replace("localhost" , AdapterDiffUtil.URL))
            .resize(250, 250).get()
        notificationBuilder.setLargeIcon(a)
        notificationManager.notify(notificationId, notificationBuilder.build())


    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun buildAlbumDownloadNotification(
        context: Context,
        notificationId: Int,
        tittle: String,
        progress: Int
    ){
        createNotificationChannel(
            context,
            "Downloader",
            "Download",
            NotificationManagerCompat.IMPORTANCE_LOW
        )
        var intent = Intent(context, MainActivity::class.java)
        var pendingINtent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        var builder = NotificationCompat.Builder(context, "Downloader")
            .setSmallIcon(R.drawable.exo_notification_small_icon)
            .setContentTitle(tittle)
            .setContentText("${progress}% ")
            .setProgress(100, progress, false)
            .setContentIntent(pendingINtent)
            .setColorized(true)
            .setOngoing(true)
        if(progress == 100){
            buildCompleteNotification(context, notificationId, tittle)
            return
        }

        notify(context, notificationId, builder.build())
    }


    fun buildCompleteNotification(context: Context, notificationId: Int, message: String){
        var intent = Intent(context, MainActivity::class.java)
        var pendingINtent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        var a =DownloadNotificationHelper(
            context,
            MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID.toString()
        )
            .buildDownloadCompletedNotification(
                R.drawable.exo_notification_small_icon,
                pendingINtent,
                message
            )
        notify(context, notificationId, a)
    }

    fun notify(context: Context, notificationId: Int, notification: Notification){
        getNotificationManager(context).notify(notificationId, notification)
    }





    @RequiresApi(Build.VERSION_CODES.O)
    fun buildNewAlbumNotification(context: Context){
        createNotificationChannel(
            context,
            NEW_ALBUM_CHANNEL_ID,
            "Album",
            NotificationManagerCompat.IMPORTANCE_HIGH
        )

        var intent = Intent(context, MainActivity::class.java)
        var drawable = context.resources.getDrawable(R.drawable.ic_album_black_24dp)
         var bitmap = drawable.toBitmap()
        var pendingINtent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        var builder = NotificationCompat.Builder(context, NEW_ALBUM_CHANNEL_ID)
            .setSmallIcon(R.drawable.exo_notification_small_icon)
            .setLargeIcon(bitmap)
            .setContentTitle("Album Name")
            .setProgress(100, 34, false)
            .setContentText("New album from some artist name")
            .setContentIntent(pendingINtent)
//            .setColorized(true)
//            .setOngoing(false)
//            .setAutoCancel(true)

        getNotificationManager(context).notify(NEW_ALBUM_NOTIFICATION_ID, builder.build())

    }
}

class NovodaDownloadNotification<T : Any> : NotificationCustomizer<T>{
    override fun notificationDisplayState(payload: T): NotificationCustomizer.NotificationDisplayState {
        TODO("Not yet implemented")
    }

    override fun customNotificationFrom(p0: NotificationCompat.Builder?, p1: T): Notification {
        TODO("Not yet implemented")
    }

}


class NovodaNotificationCustomizer : NotificationCustomizer<DownloadBatchStatus>{
    override fun notificationDisplayState(payload: DownloadBatchStatus?): NotificationCustomizer.NotificationDisplayState {
        return if(payload?.status() == DownloadBatchStatus.Status.PAUSED) NotificationCustomizer.NotificationDisplayState.HIDDEN_NOTIFICATION
        else if(payload?.status() == DownloadBatchStatus.Status.DOWNLOADING) NotificationCustomizer.NotificationDisplayState.SINGLE_PERSISTENT_NOTIFICATION
        else NotificationCustomizer.NotificationDisplayState.HIDDEN_NOTIFICATION
    }

    override fun customNotificationFrom(p0: NotificationCompat.Builder?, p1: DownloadBatchStatus?): Notification {
        TODO("Not yet implemented")
    }

}