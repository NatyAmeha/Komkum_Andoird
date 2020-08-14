package com.example.ethiomusic.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.google.android.exoplayer2.ui.DownloadNotificationHelper

class NotificationHelper {
    companion object{
       const val PLAYBACK_NOTIFICATION_CHANNEL_ID = 1
       const val PLAYBACK_NOTIFICATION_ID = 2

        const val MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID = 3
        const val MEDIA_DOWNLOAD_NOTIFICATION_ID = 4
        const val MEDIA_DOWNLOAD_NOTIFICATION_INTERVAL = 1000L


        const val BASE_NOTIFICATION_ID = 10

        const val NEW_ALBUM_CHANNEL_ID = "NEW_ALBUM"
        const val NEW_ALBUM_NOTIFICATION_ID = 5

        const val ALBUM_DOWNLOAD_NOTIFICATION_ID = 6
    }


    fun getNotificationManager(context : Context)  =  NotificationManagerCompat.from(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context : Context , channelId : String, name : String, importance : Int){
        var channel = NotificationChannel(channelId , name , importance)
        getNotificationManager(context).createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buildAlbumDownloadNotification(context : Context  , notificationId: Int , tittle : String , progress : Int){
        createNotificationChannel(context , "Downloader" , "Download" , NotificationManagerCompat.IMPORTANCE_LOW)
        var intent = Intent(context , MainActivity::class.java)
        var pendingINtent = PendingIntent.getActivity(context ,0 , intent , PendingIntent.FLAG_UPDATE_CURRENT)
        var builder = NotificationCompat.Builder(context , "Downloader")
            .setSmallIcon(R.drawable.exo_notification_small_icon)
            .setContentTitle(tittle)
            .setContentText("${progress}% ")
            .setProgress(100 , progress, false)
            .setContentIntent(pendingINtent)
            .setColorized(true)
            .setOngoing(true)
        if(progress == 100){
            buildCompleteNotification(context , notificationId , tittle)
            return
        }

        notify(context , notificationId , builder.build())
    }


    fun buildCompleteNotification(context : Context , notificationId: Int , message : String){
        var intent = Intent(context , MainActivity::class.java)
        var pendingINtent = PendingIntent.getActivity(context ,0 , intent , PendingIntent.FLAG_UPDATE_CURRENT)
        var a =DownloadNotificationHelper(context , MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID.toString())
            .buildDownloadCompletedNotification(R.drawable.exo_notification_small_icon , pendingINtent , message)
        notify(context , notificationId , a)
    }

    fun notify(context : Context , notificationId : Int , notification : Notification){
        getNotificationManager(context).notify(notificationId , notification)
    }





    @RequiresApi(Build.VERSION_CODES.O)
    fun buildNewAlbumNotification(context : Context){
        createNotificationChannel(context , NEW_ALBUM_CHANNEL_ID , "Album" , NotificationManagerCompat.IMPORTANCE_HIGH)

        var intent = Intent(context , MainActivity::class.java)
        var drawable = context.resources.getDrawable(R.drawable.ic_album_black_24dp)
         var bitmap = drawable.toBitmap()
        var pendingINtent = PendingIntent.getActivity(context ,0 , intent , PendingIntent.FLAG_UPDATE_CURRENT)
        var builder = NotificationCompat.Builder(context , NEW_ALBUM_CHANNEL_ID)
            .setSmallIcon(R.drawable.exo_notification_small_icon)
            .setLargeIcon(bitmap)
            .setContentTitle("Album Name")
            .setProgress(100 , 34 , false)
            .setContentText("New album from some artist name")
            .setContentIntent(pendingINtent)
//            .setColorized(true)
//            .setOngoing(false)
//            .setAutoCancel(true)

        getNotificationManager(context).notify(NEW_ALBUM_NOTIFICATION_ID , builder.build())

    }
}