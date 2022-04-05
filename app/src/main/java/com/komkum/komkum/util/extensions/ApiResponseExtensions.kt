package com.komkum.komkum.util.extensions

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.R
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.delete
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.Resource
import com.facebook.login.LoginManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.komkum.komkum.util.notification.FcmService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import retrofit2.Response

import java.lang.Exception
import kotlin.Error
import kotlin.reflect.typeOf

fun <T> Response<T>.toResource(): Resource<T> {

    Log.i("responsecode" , code().toString())

    return if (isSuccessful) {
        Log.i("responsebody" , body().toString())
        body()?.let {
            return Resource.Success(it)
        }
        Resource.Success(message = "No Body Data")

    } else {
        Log.i("responsebodyerror" , "${errorBody().toString()}")

        when {
            code() == 500  -> throw Throwable(errorBody()?.string())
            code() == 401 -> throw Throwable(Resource.UN_AUTHORIZED_ACCESS)
            else -> {
                throw Throwable(errorBody()?.string())
            }
        }
    }
}

fun String?.handleError(context : Context , onUnAuthorizedError : ((error :String) -> Unit)? = null,
     signupSource : String? = FcmService.F_EPV_ORGANIC, otherError : ((error : String?) -> Unit)? = null){
    if(this == Resource.UN_AUTHORIZED_ACCESS){
        var prefmanager = PreferenceHelper.getInstance(context)
        prefmanager[AccountState.LOGIN_PREFERENCE] = AccountState.LOGOUT_PREFERENCE_VALUE
        prefmanager[AccountState.TOKEN_PREFERENCE] = AccountState.INVALID_TOKEN_PREFERENCE_VALUE
        prefmanager.delete(AccountState.USER_ID)
        prefmanager.delete(AccountState.USERNAME)
        prefmanager.delete(AccountState.EMAIL)
        prefmanager.delete(AccountState.PROFILE_IMAGE)
        prefmanager.delete(AccountState.USED_AUDIOBOOK_CREDIT)
//        prefmanager.delete(AccountState.ACCESS_FACEBOOK_FRIENDS)

        LoginManager.getInstance().logOut()

        onUnAuthorizedError?.let { it(this) }
        var intent = Intent(context , OnboardingActivity::class.java)
        intent.putExtra(FcmService.F_EP_SIGN_UP_SOURCE , signupSource)
        context.startActivity(intent)
    }
    else{
        otherError?.let { it(this) }
//        Toast.makeText(context , this , Toast.LENGTH_LONG).show()
    }
}



fun <T , R> apiDataRequestHelper(param : T? = null ,  errorHandler : (message : String) -> Any  , block : suspend (blockParam : T?) -> Response<R>) = liveData {
    withContext(Dispatchers.IO){
        emit(Resource.Loading())
        try{
           if (param == null) emit(block(null).toResource())
           else emit(block(param).toResource())

        }catch (ex : Throwable){
            Log.i("apidataerror" , ex.toString()  ?:"Error ")
            ex.message?.let { errorHandler(it) }
        }
    }
}


 suspend fun <T , R> dataRequestHelper(param : T? = null, errorHandler : (message : String) -> Any, block : suspend (blockParam : T?) -> Response<R>) =
    withContext(Dispatchers.IO){
    try{
        if (param == null) block(null).toResource()
        else block(param).toResource()
    }catch (ex : Throwable){
        ex.message?.let { Log.i("apidataerror" , it) }
        ex.message?.let { errorHandler(it) }
        Resource.Error(ex.message ?: "Error occured")
    }
}

suspend fun <T , R> dbDataRequestHelper(param : T? = null, errorHandler : (message : String) -> Any, block : suspend (blockParam : T?) -> R) =
    withContext(Dispatchers.IO){
        try{
            if (param == null)  Resource.Success(data = block(null))
            else  Resource.Success(data = block(param))

        }catch (ex : Exception){
            ex.message?.let { Log.i("dbdataError" , it) }
            ex.message?.let { errorHandler(it)
            }
            Resource.Error<R>(ex.message ?: "Error occured")
        }
    }





 fun<T> LiveData<Resource<List<T>>>.handleListDataObservation(owner : LifecycleOwner, callback : (data : List<T>) -> Unit){
     this.observe(owner , Observer{resource ->
         resource.data?.let {
             callback(it)
         }
     })
 }

fun<T> LiveData<Resource<T>>.handleSingleDataObservation(owner : LifecycleOwner , callback : (data : T) -> Unit){
    this.observe(owner , Observer{resource ->
        resource.data?.let {
            callback(it)
        }
    })
}


suspend fun <T> apiDataCheckHelper(param : T? = null ,  errorHandler : (message : String) -> Any  , block : suspend (blockParam : T?) -> Response<Boolean>)
   =  withContext(Dispatchers.IO) {
    try {
        if (param == null) block(null).toResource().data!!
        else block(param).toResource().data!!

    } catch (ex: Throwable) {
        ex.message?.let { Log.i("apierror", it) }
        ex.message?.let { errorHandler(it) }
        false
    }
}





