package com.example.ethiomusic.di

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DownloadModule {
    @Singleton
    @Provides
    fun provideexoDb(context: Context) : ExoDatabaseProvider{
        return ExoDatabaseProvider(context)
    }

    @Singleton
    @Provides
    fun provideCache(context: Context , dbProvider : ExoDatabaseProvider) : SimpleCache{
        Log.i("donwloadlocation" , context.filesDir.absolutePath)
        return SimpleCache(context.filesDir , NoOpCacheEvictor() , dbProvider)
    }

    @Singleton
    @Provides
    fun provideDataSource(context: Context) : DefaultDataSourceFactory{
        return DefaultDataSourceFactory(context , "upstream factory")
    }
    @Singleton
    @Provides
    fun provideCacheDataSource(cache : SimpleCache , upstreamDataSource : DefaultDataSourceFactory) : CacheDataSourceFactory{
        return CacheDataSourceFactory(cache , upstreamDataSource)
    }
    @Singleton
    @Provides
    fun provideDownloadManager(context : Context , cache : SimpleCache , datasource : DefaultDataSourceFactory , dbProvider : ExoDatabaseProvider) : DownloadManager{
        return DownloadManager(context , dbProvider , cache , datasource)
    }
}