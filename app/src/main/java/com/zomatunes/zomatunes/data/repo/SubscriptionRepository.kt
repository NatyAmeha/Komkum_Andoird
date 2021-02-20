package com.zomatunes.zomatunes.data.repo

import android.util.Log
import com.zomatunes.zomatunes.data.api.UserApi
import com.zomatunes.zomatunes.data.model.Subscription
import com.zomatunes.zomatunes.data.model.SubscriptionPlan
import com.zomatunes.zomatunes.di.ActivityScope
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.extensions.toResource
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityScoped
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