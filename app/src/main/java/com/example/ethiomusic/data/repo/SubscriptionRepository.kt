package com.example.ethiomusic.data.repo

import android.util.Log
import com.example.ethiomusic.data.api.UserApi
import com.example.ethiomusic.data.model.Subscription
import com.example.ethiomusic.data.model.SubscriptionPlan
import com.example.ethiomusic.di.ActivityScope
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.toResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityScope
class SubscriptionRepository @Inject constructor(var userApi: UserApi) {

    suspend fun getsubscriptions() : Resource<List<SubscriptionPlan>>{
      return   withContext(Dispatchers.IO){
            try {
                var result = userApi.getSubscriptions().toResource()
                return@withContext result
            }catch (ex : Throwable){
                Log.i("apierror" , ex.message)
               return@withContext  if(ex.message == Resource.NETWORK_ERROR) Resource.Error.NetworkIssue<List<SubscriptionPlan>>(ex.message!!)
                else Resource.Error.ApiIssue<List<SubscriptionPlan>>(ex.message!!)

            }
        }

    }
}