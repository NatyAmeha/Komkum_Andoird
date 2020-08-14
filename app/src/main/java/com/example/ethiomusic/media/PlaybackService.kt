package com.example.ethiomusic.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Base64
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.media.MediaBrowserServiceCompat
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.util.NotificationHelper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.net.URL
import java.nio.ByteBuffer
import javax.inject.Inject

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
        var application = this.application as EthioMusicApplication
        application.appComponent.inject(this)

        super.onCreate()
        Log.i("servicecreate" , "created")



//        player.addListener(PlayerListener())

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this , NotificationHelper.PLAYBACK_NOTIFICATION_CHANNEL_ID.toString() ,
            R.string.app_name  , R.string.app_name, NotificationHelper.PLAYBACK_NOTIFICATION_ID ,
            object : PlayerNotificationManager.MediaDescriptionAdapter{
                override fun createCurrentContentIntent(player: Player?): PendingIntent? {
                    var intent = Intent(this@PlaybackService , MainActivity::class.java)
                    intent.putExtra("OPENPLAYER" , true)
                   return PendingIntent.getActivity(this@PlaybackService , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT)
                }

                override fun getCurrentContentText(player: Player?): String? {
                   return  playbackPreparer.playlistInfo[player?.currentWindowIndex!!].genre

                }

                override fun getCurrentContentTitle(player: Player?): String {
                  return playbackPreparer.playlistInfo[player?.currentWindowIndex!!].tittle ?: "Unkown"
                }

                override fun getCurrentLargeIcon(player: Player?, callback: PlayerNotificationManager.BitmapCallback?): Bitmap? {
                 CoroutineScope(Dispatchers.IO).launch {
                     var imageUrl =  playbackPreparer.playlistInfo[player?.currentWindowIndex!!].thumbnailPath?.replace("localhost" , "192.168.43.166")
                     var url = URL(imageUrl)
                     try {
                         var bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                         callback?.onBitmap(bitmap)
                     }catch (ex : Exception){

                     }
                    }
                    var bitmap = resources.getDrawable(R.drawable.backimg)
                    return bitmap.toBitmap()
                }

            } , object : PlayerNotificationManager.NotificationListener{
                override fun onNotificationCancelled(notificationId: Int) {
                    stopForeground(true)
                    stopSelf()
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean) {
//                   startService(Intent(this@PlaybackService , PlaybackService::class.java))
                    startForeground(notificationId , notification)
                }

            })

        mediaSession = MediaSessionCompat(this , "ROOT_PLAYER_MEDIA_SESSION").apply {
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionToken)
        playerNotificationManager.setFastForwardIncrementMs(0)
        playerNotificationManager.setRewindIncrementMs(0)
        playerNotificationManager.setUseStopAction(true)




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


//    inner class PlayerListener : Player.EventListener {
//        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            when(playbackState){
//                Player.STATE_ENDED ->{
//                    Log.i("player" , "playback ended")
//                }
//            }
//        }
//
//        override fun onIsPlayingChanged(isPlaying: Boolean) {
//            super.onIsPlayingChanged(isPlaying)
//        }
//
//        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
//            Log.i("timeline" , timeline?.windowCount.toString())
//        }
//
//    }


}