package com.komkum.komkum.data.repo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.UserApi
import com.komkum.komkum.data.model.Subscription
import com.komkum.komkum.data.model.SubscriptionPlan
import com.komkum.komkum.data.model.VerifyPayment
import com.komkum.komkum.di.ActivityScope
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.dataRequestHelper
import com.komkum.komkum.util.extensions.toResource
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityScoped
class SubscriptionRepository @Inject constructor(var userApi: UserApi) {
    var error = MutableLiveData<String>()
    suspend fun getsubscriptions() : Resource<List<SubscriptionPlan>>{
      return   withContext(Dispatchers.IO){
            try {
                var result = userApi.getSubscriptions().toResource()
                return@withContext result
            }catch (ex : Throwable){
                ex.message?.let { Log.i("apierror" , it) }
                return@withContext  if(ex.message == Resource.NETWORK_ERROR) Resource.Error.NetworkIssue<List<SubscriptionPlan>>(ex.message!!)
                else Resource.Error.ApiIssue<List<SubscriptionPlan>>(ex.message!!)
            }
        }
    }

    suspend fun getSubscriptionsBeta() = dataRequestHelper<Any,List<SubscriptionPlan>>(errorHandler = fun(message : String) = error.postValue(message)){
        userApi.getSubscriptions()
    }

    suspend fun verifySubscription(verifyPayment: VerifyPayment) = dataRequestHelper(verifyPayment ,fun(message : String) = error.postValue(message)){
        userApi.verifyPayment(it!!)
    }

    suspend fun upgradeSubscription(subscriptionId : String , paymentMethod : Int , verficationToken : String? = null) = dataRequestHelper(subscriptionId , fun(message : String) = error.postValue(message)){
        userApi.upgradeSubscription(subscriptionId, paymentMethod , verficationToken)
    }
}