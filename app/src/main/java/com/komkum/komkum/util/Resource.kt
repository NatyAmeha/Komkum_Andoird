package com.komkum.komkum.util


sealed class Resource<T>(var data: T? = null, var message: String? = null) {
     class Success<T>(data: T? = null , message: String? = null) : Resource<T>(data, message)

     open class Error<T>(error: String , data : T?= null) : Resource<T>(data = data ,  message = error)  {
         class NetworkIssue<T>(networkERror: String) : Error<T>(networkERror)
         class ApiIssue<T>(apiError: String) : Error<T>(apiError)
    }

     class Loading<T>(loadingMessage: String? = null) : Resource<T>(message = loadingMessage)

    companion object{
        val NETWORK_ERROR = "NETWORK_ERROR"
        val SERVER_ERROR = "SERVER_ERROR"

        val UN_AUTHORIZED_ACCESS = "Your session has esxpired"
    }
}


