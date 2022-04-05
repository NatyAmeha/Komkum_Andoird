package com.komkum.komkum.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.komkum.komkum.ControllerActivity
import com.komkum.komkum.data.api.*
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.komkum.komkum.data.db.SampleMigration
import com.komkum.komkum.data.repo.PaymentRepository
import com.komkum.komkum.usecase.IPayment
import com.komkum.komkum.usecase.IWallet
import com.komkum.komkum.usecase.WalletPayment
import com.komkum.komkum.usecase.YenePayVendor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier





@InstallIn(ApplicationComponent::class)
@Module
class AppModule {

    @Provides
    fun RetrofitBuilder(@ApplicationContext context : Context) : Retrofit{
        var remoteUrl = "https://${AdapterDiffUtil.URL}/"
        var localUrl = "http://10.0.2.2:4000/api/"

        var okhttp = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthIntercepter(context)).build()
        var gson = GsonBuilder().setLenient().create()

        var retrofit  = Retrofit.Builder().baseUrl(remoteUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okhttp)
            .build()
        return retrofit
    }

    @Provides
    fun provideControllerActivity() : ControllerActivity{
        return ControllerActivity()
    }

    @Provides
    fun appDbProvider(@ApplicationContext context : Context) : AppDb{
        var appDbInstance = Room.databaseBuilder(context , AppDb::class.java , "ZomaTunes")
            .fallbackToDestructiveMigration()
            .build()
        return appDbInstance
    }

    @Provides
    fun homeApiProvider(retrofit : Retrofit) : HomeApi{
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    fun albumApiProvider(retrofit : Retrofit) : AlbumApi{
        return retrofit.create(AlbumApi::class.java)
    }

    @Provides
    fun libraryApiProvider(retrofit : Retrofit) : LibraryApi{
        return retrofit.create(LibraryApi::class.java)
    }

    @Provides
    fun artistApiProvider(retrofit : Retrofit) : ArtistApi{
        return retrofit.create(ArtistApi::class.java)
    }

    @Provides
    fun songApiProvider(retrofit : Retrofit) : SongApi{
        return retrofit.create(SongApi::class.java)
    }
    @Provides
    fun radioApiProvider(retrofit : Retrofit) : RadioApi{
        return retrofit.create(RadioApi::class.java)
    }


    @Provides
    fun podcastApiProvider(retrofit : Retrofit) : PodcastApi{
        return retrofit.create(PodcastApi::class.java)
    }

    @Provides
    fun bookApiProvider(retrofit: Retrofit) : BookApi{
        return retrofit.create(BookApi::class.java)
    }

    @Provides
    fun authorApiProvider(retrofit: Retrofit) : AuthorApi{
        return retrofit.create(AuthorApi::class.java)
    }

    @Provides
    fun productApiProvider(retrofit: Retrofit) : ProductApi{
        return retrofit.create(ProductApi::class.java)
    }

    @Provides
    fun teamApiProvider(retrofit: Retrofit) : TeamApi{
        return retrofit.create(TeamApi::class.java)
    }

    @Provides
    fun orderApiProvider(retrofit: Retrofit) : OrderApi{
        return retrofit.create(OrderApi::class.java)
    }

    @Provides
    fun adApiProvider(retrofit: Retrofit) : AdApi{
        return retrofit.create(AdApi::class.java)
    }

    @Provides
    fun paymentApiProvider(retrofit: Retrofit) : PaymentApi{
        return retrofit.create(PaymentApi::class.java)
    }





    ////////////////////////////////////////////////////////////////////////////////////////





}