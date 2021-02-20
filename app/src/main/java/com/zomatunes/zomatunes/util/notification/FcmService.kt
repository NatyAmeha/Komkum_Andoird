package com.zomatunes.zomatunes.util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Observer
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavDeepLinkDslBuilder
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.api.UserApi
import com.zomatunes.zomatunes.data.repo.UserRepository
import com.zomatunes.zomatunes.util.NotificationHelper
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.google.android.exoplayer2.util.Util
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {
    @Inject
    lateinit var userREpo: UserRepository
    override fun onMessageReceived(p0: RemoteMessage) {
        var data = p0.data
        var title = data["title"]
        var body = data["body"]
        var imageUrl = data["imageUrl"]
        data["type"]?.let {
            when(it){
                NotificationHelper.NOTIFICATION_DESTINATION_ARTIST ->{
                    var pendingIntent  = NavDeepLinkBuilder(applicationContext)
                        .setDestination(R.id.artistFragment)
                        .setGraph(R.navigation.main_navigation)
                        .createPendingIntent()
                    NotificationHelper.showFcmMusicNotification(applicationContext , title , body , imageUrl , pendingIntent)
                }
            }
        }
    }

    override fun onNewToken(p0: String) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.i("fcm", "getInstanceId failed", task.exception)
                return@addOnCompleteListener
            }
            task.result?.token?.let { token ->
                Log.i("fcmtoken", token)
                Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
                var prefHelper = PreferenceHelper.getInstance(applicationContext)
                prefHelper[PreferenceHelper.FCM_TOKEN] = token
                prefHelper[PreferenceHelper.IS_NEW_FCM_TOKEN] = true
            }

        }

    }
}