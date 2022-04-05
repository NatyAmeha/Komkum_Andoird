package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.PaymentApi
import com.komkum.komkum.data.model.Donation
import com.komkum.komkum.util.extensions.dataRequestHelper
import javax.inject.Inject

class PaymentRepository @Inject constructor(var paymentApi : PaymentApi) {
    var error = MutableLiveData<String>()

    suspend fun recharegWallet(amount : Int , id : String , paymentVendor : Int) = dataRequestHelper(amount , fun(message) = error.postValue(message)){
        paymentApi.rechargeWallet(it!! , id , paymentVendor)
    }

    suspend fun payWithWallet(amount : Double , transactionType : Int) = dataRequestHelper(amount , fun(message) = error.postValue(message)){
        paymentApi.payWithWallet(it!! , transactionType)
    }

    suspend fun transferRewardToWallet(amount : Int , adId : String , teamId : String , discountcode : String) = dataRequestHelper(amount , fun(message) = error.postValue(message)){
        paymentApi.transferReward(it!! , adId , teamId , discountcode)
    }

    suspend fun transferCommissionToWallet(amount : Double ) = dataRequestHelper(amount , fun(message) = error.postValue(message)){
        paymentApi.transferCommission(it!! )
    }

    suspend fun transferGameReward(amount : Double ) = dataRequestHelper(amount , fun(message) = error.postValue(message)){
        paymentApi.transferGameReward(it!! )
    }

    suspend fun makeDonation(donationInfo : Donation) = dataRequestHelper(donationInfo , fun(message) = error.postValue(message)){
        paymentApi.makeDonation(it!!)
    }

    suspend fun getWalletBalance() = dataRequestHelper(null , fun(message) = error.postValue(message)){
        paymentApi.getWalletBalance()
    }

    suspend fun getWalletTransactions() = dataRequestHelper(null , fun(message) = error.postValue(message)){
        paymentApi.walletTransactions()
    }
}