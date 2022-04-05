package com.komkum.komkum.data.api

import android.content.Context
import android.util.Log
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import okhttp3.Interceptor
import okhttp3.Response

class AuthIntercepter(var context : Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var preference = PreferenceHelper.getInstance(context)
        var token = preference.get(AccountState.TOKEN_PREFERENCE , AccountState.INVALID_TOKEN_PREFERENCE_VALUE)
        var request = chain.request()

        request = request.newBuilder()
            .addHeader("Authorization" , "Bearer $token")
            .build()

        var response = chain.proceed(request)
        return response
    }
}