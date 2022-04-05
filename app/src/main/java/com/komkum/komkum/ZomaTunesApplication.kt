package com.komkum.komkum

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.multidex.MultiDexApplication
import androidx.navigation.NavDeepLinkBuilder
import com.komkum.komkum.ui.payment.PaymentActivity
import com.komkum.komkum.util.PreferenceHelper

//import com.example.com.komkum.komkum.di.AppComponent


import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.komkum.komkum.util.extensions.adjustFontScale
import com.novoda.downloadmanager.ConnectionType
import com.novoda.downloadmanager.DownloadManagerBuilder
import com.novoda.downloadmanager.StorageRequirementRuleFactory
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.model.YenePayConfiguration
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class ZomaTunesApplication : MultiDexApplication() {

    lateinit var downloadManeger : com.novoda.downloadmanager.DownloadManager

    var paymentFor = 0
    var bookId : String? = null   // must have value if purchase flow is for ebook

    companion object{
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_AMHARIC = "am"
    }



    override fun onCreate() {
        super.onCreate()
        var baseUrl : String = "192.168.43.166"
        Log.i("application", "on create called")
//        adjustFontScale(this.resources.configuration)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) createNovodaDownloadManager()

//        var completionInent =   PendingIntent.getActivity(applicationContext , PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE ,
//        Intent(applicationContext , OnboardingActivity::class.java) , 0)

//        var a = NavDeepLinkBuilder(applicationContext)
//            .setGraph(R.navigation.onboarding_navigation)
//            .setDestination(R.id.artistSelectionListFragment)
//            .createPendingIntent()

        //setup default language local
        var default = PreferenceLocaleStore(this , Locale(LANGUAGE_AMHARIC))
        Lingver.init(this , default)

        var paymentIntent =  Intent(applicationContext , PaymentActivity::class.java)
        paymentIntent.putExtra("PAYMENT_FOR" , paymentFor)
        paymentIntent.putExtra("BOOK_ID" , bookId)
        var paymentSuccessPendingIntent = PendingIntent.getActivity(applicationContext , PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE , paymentIntent , 0)

        var cancelationIntent = PendingIntent.getActivity(applicationContext , PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE ,
        Intent(applicationContext , OnboardingActivity::class.java) , 0)

        var builder = YenePayConfiguration.Builder(applicationContext)
            .setGlobalCompletionIntent(paymentSuccessPendingIntent)
            .setGlobalCancelIntent(cancelationIntent)
            .build()

        YenePayConfiguration.setDefaultInstance(builder)
    }



    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var config = Configuration(newConfig)
//        adjustFontScale(config)
    }


    fun createNovodaDownloadManager(){
        var handler =  Handler(Looper.getMainLooper())
        var downloadBuilder =  DownloadManagerBuilder
            .newInstance(applicationContext , handler ,  R.drawable.ic_file_download_black_24dp)
            .withStorageRequirementRules(StorageRequirementRuleFactory.createByteBasedRule(209715200))

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            var channel = NotificationChannel( "BOOK_DOWNLOAD_CHANNEL" , "Download"  , NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null , null)
            downloadBuilder.withNotificationChannel(channel)
        }

        var downloadonWifi = PreferenceHelper.getInstance(applicationContext).getBoolean("download_on_wifi" , false)
        var connectionType =
            if (downloadonWifi) ConnectionType.UNMETERED
            else ConnectionType.ALL
        downloadBuilder.withAllowedConnectionType(connectionType)

        downloadManeger = downloadBuilder.build()
    }
}