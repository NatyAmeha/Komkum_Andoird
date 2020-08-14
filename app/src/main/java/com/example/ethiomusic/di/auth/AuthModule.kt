package com.example.ethiomusic.di.auth

import com.example.ethiomusic.data.api.UserApi
import com.example.ethiomusic.di.ActivityScope
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {

    @Provides
    fun userApiProvider(retrofit: Retrofit) : UserApi {
        return retrofit.create(UserApi::class.java)
    }

}