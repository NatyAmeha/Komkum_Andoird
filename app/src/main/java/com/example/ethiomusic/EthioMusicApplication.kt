package com.example.ethiomusic

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.example.ethiomusic.di.AppComponent
//import com.example.ethiomusic.di.AppComponent

import com.example.ethiomusic.di.DaggerAppComponent
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.model.YenePayConfiguration

class EthioMusicApplication : Application() {

    val appComponent : AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }



    override fun onCreate() {
        super.onCreate()
        var baseUrl : String = "192.168.43.166"
        Log.i("application", "on create called")

        var completionInent =   PendingIntent.getActivity(applicationContext , PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE ,
        Intent(applicationContext , OnboardingActivity::class.java) , 0)

        var cancelationIntent = PendingIntent.getActivity(applicationContext , PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE ,
        Intent(applicationContext , OnboardingActivity::class.java) , 0)

        var builder = YenePayConfiguration.Builder(applicationContext)
            .setGlobalCompletionIntent(completionInent)
            .setGlobalCancelIntent(cancelationIntent)
            .build()

//        YenePayConfiguration.setDefaultInstance(builder)
    }
}