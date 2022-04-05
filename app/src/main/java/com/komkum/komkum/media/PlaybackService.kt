package com.komkum.komkum.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.media.MediaBrowserServiceCompat
import androidx.navigation.NavDeepLinkBuilder
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.util.NotificationHelper
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaBrowserServiceCompat() {

    lateinit var sessionConnector: MediaSessionConnector
    lateinit var mediaSession : MediaSessionCompat

    lateinit var playerNotificationManager: PlayerNotificationManager

    @Inject
     lateinit var player : ExoPlayer

    @Inject
    lateinit var playbackPreparer :CustomPlaybackPreparer




    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("ETHIOUSIC_ROOT" , null)
    }

    override fun onCreate() {
        super.onCreate()
        var preference = PreferenceHelper.getInstance(this)
        var isSubscriptionValid = preference.get(AccountState.SUBSCRIPTON_PREFERENCE , AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE)

//        player.addListener(PlayerListener())


        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this , NotificationHelper.PLAYBACK_NOTIFICATION_CHANNEL_ID.toString() ,
            R.string.app_name  , R.string.app_name, NotificationHelper.PLAYBACK_NOTIFICATION_ID ,
            object : PlayerNotificationManager.MediaDescriptionAdapter{
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    var intent = Intent(this@PlaybackService , MainActivity::class.java)
                    intent.putExtra("OPENPLAYER" , true)
                    intent.putExtra("PLAYBACK_STATE" ,  playbackPreparer.playerState)
//                    return NavDeepLinkBuilder(this@PlaybackService)
//                        .setGraph(R.navigation.main_navigation)
//                        .setDestination(R.id.playerFragment)
//                        .setArguments(bundleOf("OPENPLAYER" to true , "PLAYBACK_STATE" to playbackPreparer.playerState))
//                        .createPendingIntent()
                    return PendingIntent.getActivity(this@PlaybackService , 111 , intent , PendingIntent.FLAG_UPDATE_CURRENT)

                }

                override fun getCurrentContentText(player: Player): String? {
                   return if(playbackPreparer.playlistInfo.isEmpty()){
                       player.stop()
                       ""
                   }else  playbackPreparer.playlistInfo[player.currentWindowIndex!!].ownerName?.joinToString(", ")

                }

                override fun getCurrentContentTitle(player: Player): String {
                    return if(playbackPreparer.playlistInfo.isEmpty()){
                        player.stop()
                        ""
                    } else{
                        var selectedStream = playbackPreparer.playlistInfo[player.currentWindowIndex]
                        if(selectedStream.isAd == true){
                            playerNotificationManager.setUseNextAction(false)
                            playerNotificationManager.setUsePreviousAction(false)
                        }
                        else{
                            if(isSubscriptionValid == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE) {
                                playerNotificationManager.setUseNextAction(true)
                                playerNotificationManager.setUseNextActionInCompactView(true)
                                if(playbackPreparer.playerState ==3) playerNotificationManager.setUsePreviousAction(false)
                                else playerNotificationManager.setUsePreviousAction(true)

                                playerNotificationManager.setUsePreviousActionInCompactView(true)
                            }
                            else{
                                playerNotificationManager.setUseNextAction(true)
                                playerNotificationManager.setUseNextActionInCompactView(true)
                                if( playbackPreparer.playerState == 5){
                                    playerNotificationManager.setUsePreviousAction(true)
                                }
                                else playerNotificationManager.setUsePreviousAction(false)

                                playerNotificationManager.setUsePreviousActionInCompactView(true)

                            }

                        }

                        playbackPreparer.playlistInfo[player.currentWindowIndex].tittle ?: "Unkown"
                    }
                }

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                    var target = object : Target{

                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) { callback?.onBitmap(bitmap) }
                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    }
                    if(playbackPreparer.playlistInfo.isNotEmpty()){
                        var imageUrl =  playbackPreparer.playlistInfo[player?.currentWindowIndex!!].thumbnailPath?.replace("localhost" , AdapterDiffUtil.URL)
                        Picasso.get().load(imageUrl).placeholder(R.drawable.music_placeholder).into(target)
                    }


                    var bitmap = resources.getDrawable(R.drawable.music_placeholder)
                    return bitmap.toBitmap()
                }

            } , object : PlayerNotificationManager.NotificationListener{

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    Log.i("playererror" ,  "notiication error   ${ player.currentPosition}")
                    stopForeground(true)
                    stopSelf()
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
//                   startService(Intent(this@PlaybackService , PlaybackService::class.java))
                    startForeground(notificationId , notification)
                }

            })

        mediaSession = MediaSessionCompat(this , "ROOT_PLAYER_MEDIA_SESSION").apply {
            isActive = true
        }
        sessionToken = mediaSession.sessionToken
       var a =  playbackPreparer.mediaType
        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)

        playerNotificationManager.setUseNextAction(true)
        playerNotificationManager.setUseNextActionInCompactView(true)
        playerNotificationManager.setUsePreviousAction(true)
        playerNotificationManager.setUsePreviousActionInCompactView(true)

        playerNotificationManager.setFastForwardIncrementMs(0)
        playerNotificationManager.setRewindIncrementMs(0)
        playerNotificationManager.setUseStopAction(true)

        playerNotificationManager.setUseChronometer(false)

        if(isSubscriptionValid == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE) {
            playerNotificationManager.setUseNextAction(true)
            playerNotificationManager.setUseNextActionInCompactView(true)
            playerNotificationManager.setUsePreviousAction(true)
            playerNotificationManager.setUsePreviousActionInCompactView(true)
        }
        else{
            playerNotificationManager.setUseNextAction(true)
            playerNotificationManager.setUsePreviousAction(false)
        }



        sessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(player)
            setPlaybackPreparer(playbackPreparer)
            setQueueNavigator(PlaylistQueueNavigator(mediaSession , playbackPreparer))
        }

    }

    override fun onDestroy() {
        Log.i("playbackservice" , "destroyed")
        playerNotificationManager.setPlayer(null)

        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}

