package com.zomatunes.zomatunes.di.auth

import com.zomatunes.zomatunes.data.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import retrofit2.Retrofit

@InstallIn(ApplicationComponent::class)
@Module
class AuthModule {

    @Provides
    fun userApiProvider(retrofit: Retrofit) : UserApi {
        return retrofit.create(UserApi::class.java)
    }

}