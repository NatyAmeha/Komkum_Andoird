package com.komkum.komkum.data.api


import com.komkum.komkum.data.model.Donation
import com.komkum.komkum.data.model.Transaction
import retrofit2.Response
import retrofit2.http.*

interface PaymentApi {

    @PUT("wallet/recharge")
    suspend fun rechargeWallet(@Query("amount") amount : Int , @Query("trid") transactionid : String , @Query("method") paymentVendor : Int) : Response<Boolean>

    @PUT("wallet/pay")
    suspend fun payWithWallet(@Query("amount") amount : Double , @Query("payfor") transctionType : Int) : Response<String>

    @PUT("wallet/reward")
    suspend fun transferReward(@Query("amount") amount : Int , @Query("ad") adId : String , @Query("team") teamId : String , @Query("discountcode") discountCode : String) : Response<Boolean>


    @POST("donation/create")
    suspend fun makeDonation(@Body donationInfo: Donation) : Response<Boolean>

    @GET("wallet/balance")
    suspend fun getWalletBalance() : Response<Double>

    @GET("wallet/transacations")
    suspend fun walletTransactions() : Response<List<Transaction>>

    @PUT("wallet/transfer/commission")
    suspend fun transferCommission(@Query("amount")amount : Double) : Response<Boolean>

    @PUT("wallet/transfer/reward")
    suspend fun transferGameReward(@Query("amount")amount : Double) : Response<Boolean>
}