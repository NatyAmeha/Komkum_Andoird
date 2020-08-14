package com.example.ethiomusic.util.extensions

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.ethiomusic.OnboardingActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.Error
import java.lang.Exception

fun <T> Response<T>.toResource(): Resource<T> {
    Log.i("responsecode" , code().toString())

    return if (isSuccessful) {
        body()?.let {
            return Resource.Success(it)
        }
        Resource.Success(message = "No Body Data")

    } else {
        when {
            code() == 500  -> throw Throwable(message()+errorBody().toString())
            code() == 401 -> throw Throwable(Resource.UN_AUTHORIZED_ACCESS)
            else -> {
                throw Throwable(message())
            }
        }
    }

}

fun String.handleError(context : Context , handler : (() -> Unit)? = null){
    if(this == Resource.UN_AUTHORIZED_ACCESS){
        var prefmanager = PreferenceHelper.getInstance(context)
        prefmanager[AccountState.LOGIN_PREFERENCE] = AccountState.LOGOUT_PREFERENCE_VALUE
        var intent = Intent(context , OnboardingActivity::class.java)
        context.startActivity(intent)
    }
    else{
        handler?.let { it() }
//        Toast.makeText(context , this , Toast.LENGTH_LONG).show()
    }
}

fun <T , R> apiDataRequestHelper(param : T? = null ,  errorHandler : (message : String) -> Any  , block : suspend (blockParam : T?) -> Response<R>)
        = liveData {
    withContext(Dispatchers.IO){
        emit(Resource.Loading())
        try{
           if (param == null) emit(block(null).toResource())
           else emit(block(param).toResource())

        }catch (ex : Throwable){
            Log.i("apidataerror" , ex.message)
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
        Log.i("apidataerror" , ex.message)
        ex.message?.let { errorHandler(it) }
        Resource.Error<R>(ex.message ?: "Error occured")
    }
}

suspend fun <T , R> dbDataRequestHelper(param : T? = null, errorHandler : (message : String) -> Any, block : suspend (blockParam : T?) -> R) =
    withContext(Dispatchers.IO){
        try{
            if (param == null)  Resource.Success(data = block(null))
            else  Resource.Success(data = block(param))

        }catch (ex : Exception){
            Log.i("dbdataError" , ex.message)
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
        Log.i("apierror", ex.message)
        ex.message?.let { errorHandler(it) }
        false
    }
}





