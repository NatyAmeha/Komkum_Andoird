package com.zomatunes.zomatunes.di

import android.content.Context
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class PlayerModule {

    @Singleton
    @Provides
    fun provideDrmSessionManager() : DrmSessionManager<FrameworkMediaCrypto>{
        val keyid = "88XgNh5mVLKPgEnHeLI5Rg"
        val key = "pGMaFTpEPfnu0FkwQ9t1GQ"
        val keys = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"${key}\", \"kid\":\"${keyid}\"}]}"

        val ld = LocalMediaDrmCallback(keys.toByteArray())
        return DefaultDrmSessionManager(C.CLEARKEY_UUID , FrameworkMediaDrm.newInstance(C.CLEARKEY_UUID) , ld , null)
    }



    @Singleton
    @Provides
    fun providePlayer(@ApplicationContext context : Context , drmSessionManager: DrmSessionManager<FrameworkMediaCrypto> ,
    renderersFactory: RenderersFactory) : ExoPlayer{

        val trackSelector = DefaultTrackSelector()
        val streamQualityValue = PreferenceHelper.getInstance(context).getString("stream_quality" , "Auto")!!
        when(streamQualityValue){
            "96" -> {trackSelector.buildUponParameters().setMaxAudioBitrate(96).build()}
            "128" -> {trackSelector.buildUponParameters().setMaxAudioBitrate(128).build()}
            "256" -> {trackSelector.buildUponParameters().setMaxAudioBitrate(256).build()}
             else -> {}
        }

        val keyid = "88XgNh5mVLKPgEnHeLI5Rg"
        val key = "pGMaFTpEPfnu0FkwQ9t1GQ"
        val keys = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"${key}\", \"kid\":\"${keyid}\"}]}"

//        var  key = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"aeqoAqZ2Ovl56NGUD7iDkg\", \"kid\":\"q7onHovPVSu9LoakNKml2Q\"},{\"kty\":\"oct\",\"k\":\"69eaa802a6763af979e8d1940fb88392\", \"kid\":\"abba271e8bcf552bbd2e86a434a9a5d9\"},{\"kty\":\"oct\",\"k\":\"cb541084c99731aef4fff74500c12ead\", \"kid\":\"6d76f25cb17f5e16b8eaef6bbf582d8e\"}],\"type\":\"temporary\"}"

        var audioAttribute = AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player =  ExoPlayerFactory.newSimpleInstance(context , renderersFactory , trackSelector , drmSessionManager)
        player.setAudioAttributes(audioAttribute , true)
        return player
    }

    @Singleton
    @Provides
    fun provideRendererFactory(@ApplicationContext context : Context) : RenderersFactory{
        return  DefaultRenderersFactory(context)
    }

}