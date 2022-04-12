package com.komkum.komkum.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.novoda.downloadmanager.DownloadBatchStatus
import com.novoda.downloadmanager.NotificationCustomizer
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.MNotification
import com.komkum.komkum.data.model.NotificationTypes


object  NotificationHelper {

    const val PLAYBACK_NOTIFICATION_CHANNEL_ID = 1
    const val PLAYBACK_NOTIFICATION_ID = 2

    const val MEDIA_DOWNLOAD_NOTIFICATION_CHANNEL_ID = 3
    const val MEDIA_DOWNLOAD_NOTIFICATION_ID = 4
    const val NEW_ALBUM_NOTIFICATION_ID = 5
    const val BASE_NOTIFICATION_ID = 100
    const val MEDIA_DOWNLOAD_NOTIFICATION_INTERVAL = 1000L

    const val NEW_ALBUM_CHANNEL_ID = "NEW_ALBUM"
    const val FCM_NOTIFICATION_CHANNEL_ID = "MAIN_NOTIFICATION_ID"
    const val FCM_NOTIFICATION_CHANNEL_NAME = "Komkum"

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

    fun showNotification(context : Context , notificationInfo : MNotification){
        var destination : Int? = null
        var arg : Bundle? = null
        var notificationId : Int
        when(notificationInfo.intentType?.toInt()){
            NotificationTypes.SUBSCRIPTION_UPGRADE.ordinal ->{
                destination = R.id.accountFragment
                notificationId = 101
            }
            NotificationTypes.REWARD.ordinal ->{
                destination = R.id.accountFragment
                notificationId = 102
            }

            NotificationTypes.ARTIST_DONATION.ordinal ->{
                destination = R.id.artistFragment
                arg = bundleOf("ARTISTID" to notificationInfo.intent)
                notificationId = 103
            }

            NotificationTypes.NEW_SONG.ordinal ->{
                destination = R.id.baseFragment
                notificationId = 104
            }
            NotificationTypes.NEW_ALBUM.ordinal ->{
                destination = R.id.albumFragment
                arg = bundleOf("ALBUMID" to notificationInfo.intent)
                notificationId = 105
            }
            NotificationTypes.NEW_EPISODE.ordinal ->{
                destination = R.id.podcastEpisodeFragment
                arg = bundleOf("EPISODE_ID" to notificationInfo.intent)
                notificationId = 106
            }

            NotificationTypes.TEAM_FORMATION_COMPLETE.ordinal ->{
                destination = R.id.teamFragment
                arg = bundleOf("TEAM_ID" to notificationInfo.intent)
                notificationId = 107
            }

            NotificationTypes.GAME_TEAM_ACTIVE.ordinal ->{
                destination = R.id.gameTeamFragment
                arg = bundleOf("AD_ID" to notificationInfo.intent)
                notificationId = 108
            }

            NotificationTypes.GAME_REWARD.ordinal ->{
                destination = R.id.gameTeamFragment
                arg = bundleOf("AD_ID" to notificationInfo.intent , "PAGE_INDEX" to 1)
                notificationId = 109 + Math.random().toInt()
            }

            NotificationTypes.FMCG_REMINDER.ordinal ->{
                destination = R.id.packageListFragment
                arg = bundleOf( "FROM_REMINDER" to true)
                notificationId = 110
            }
            NotificationTypes.ORDER_STATUS_CHANGE.ordinal ->{
                destination = R.id.orderDetailFragment
                arg = bundleOf("ORDER_ID" to notificationInfo.intent , "LOAD_TYPE" to 0)
                notificationId = 111
            }
            NotificationTypes.TEAM_EXPIRATION_REMINDER.ordinal ->{
                destination = R.id.teamFragment
                arg = bundleOf("TEAM_ID" to notificationInfo.intent , "FROM_REMINDER" to true)
                notificationId = 112
            }
            NotificationTypes.CASHOUT_REQUEST_APPROVED.ordinal ->{
                destination = R.id.transactionFragment
                notificationId = 113
            }
            NotificationTypes.CASHOUT_REQUEST_DECLINED.ordinal -> {
                destination = R.id.cashoutFragment
                notificationId = 114
            }
            else ->{
                destination = R.id.storeHomepageFragment
                notificationId = 115
            }
        }

        var pendingIntent  = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.main_navigation)
            .setDestination(destination)
            .setArguments(arg)
            .createPendingIntent()

        var notificationManager = getNotificationManager(context)

//        var largeImage = context.resources.getDrawable(R.drawable.circularimg).toBitmap()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(context, FCM_NOTIFICATION_CHANNEL_ID, FCM_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        var notificationBuilder = NotificationCompat.Builder(context, FCM_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(notificationInfo.title)
            .setContentText(notificationInfo.body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_stat_notification_icon)
            .setColor(ContextCompat.getColor(context, R.color.light_primaryColor))

        if(!notificationInfo.image.isNullOrEmpty()){
            try{
                Log.i("fcmimage" , notificationInfo.image ?: "empty image")
                var imageIcon =  Picasso.get().load(notificationInfo.image).placeholder(R.drawable.appicon).get()
                notificationBuilder.setLargeIcon(imageIcon)
            }catch (ex : Throwable){
                Log.i("fcmimageerror" , "Notification image error")
            }
        }
        notificationBuilder
//                .setStyle(NotificationCompat.BigPictureStyle()
//                .bigPicture(imageIcon).bigLargeIcon(null))
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationInfo.body))

        notificationBuilder.setContentIntent(pendingIntent)

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
//            buildCompleteNotification(context, notificationId, tittle)
            return
        }

        notify(context, notificationId, builder.build())
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