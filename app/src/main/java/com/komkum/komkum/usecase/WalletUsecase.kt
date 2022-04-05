package com.komkum.komkum.usecase

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Donation
import com.komkum.komkum.data.model.Payment
import com.komkum.komkum.data.model.Transaction
import com.komkum.komkum.data.repo.PaymentRepository
import com.komkum.komkum.util.extensions.showCustomDialog
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


interface IWallet : IPayment{
    suspend fun rechargeWallet(amount: Int , identifier : String ,  paymentVendorType : Int) : Boolean?
//    suspend fun verifyWalletRecharge(amount: Int, paymentMethod : Int) : Boolean?
    suspend fun makeDonation(donation: Donation) : Boolean?
}

class WalletPayment @Inject constructor(var paymentRepo : PaymentRepository)  : IWallet{

    override var error = paymentRepo.error
    override lateinit var paymentStatus: MutableLiveData<String>
    override var paymentMethod: Int = Payment.PAYMENT_METHOD_WALLET
    override lateinit var paymentCompletedListener: OnPaymentListener

    override fun setPaymentListener(paymentLstnr: OnPaymentListener) {
       paymentCompletedListener = paymentLstnr
    }


    override suspend fun rechargeWallet(amount: Int , identifier : String ,  paymentVendorType : Int) : Boolean? {
       var result = paymentRepo.recharegWallet(amount , identifier , paymentVendorType).data
        return result
    }

//    override suspend fun verifyWalletRecharge(amount: Int, paymentMethod: Int): Boolean? {
//       return when(paymentMethod){
//           Payment.PAYMENT_METHOD_YENEPAY -> paymentRepo.recharegWallet(amount ,paymentMethod ).data
//           else -> false
//       }
//    }

    override suspend fun makeDonation(donation: Donation): Boolean? {
        var result = paymentRepo.makeDonation(donation).data
        return result
    }



//    override suspend fun pay(amount: Double, action: (suspend () -> Unit)?): Boolean? {
//        var result = paymentRepo.payWithWallet(amount).data
//        return result
//    }

    override suspend fun initPayment(
        amount: Double,
        paymentType: Int,
        context: Context,
        id: String,
        name: String?,
        owner: LifecycleOwner?,
        action: (suspend () -> Unit)?
    ) {
        //Authenticate the wallet owner
        //pay with wallet

                var transactionId = paymentRepo.payWithWallet(amount , paymentType).data
         Toast.makeText(context , "Payment completed" , Toast.LENGTH_SHORT).show()
                Log.i("tranid" , transactionId ?: "tranplaceholder")
                if(!transactionId.isNullOrEmpty()) paymentCompletedListener.onPaymentCompleted(this@WalletPayment , amount , transactionId)
                else error.value ="Invalid transaction id"
                Log.i("tranidpost" , "post transaction")

//        var walletBalance = paymentRepo.getWalletBalance().data
//        if(walletBalance != null){
//            var message = "Make a payment from your wallet"
//            context.showCustomDialog(
//                title = "ETB $amount",
//                message = message,
//                actionText = "Wallet balance - ETB $walletBalance",
//                pos_action_String = "Pay",
//                neg_action_String = "Cancel",
//                negetiveAction = {}
//            ){
//
//            }
//        }
//        else error.value = "Unable to get wallet balance"

    }

    override suspend fun completePayment(token: String?, paymentType: Int?) {}

    override suspend fun initiatePaymentFlow(
        amount: Double,
        unqiueid: String,
        name: String,
        context: Context,
        action: (suspend () -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun startPaymentFlow(
        amount: Int,
        identifier: String,
        context: Context,
        errorCallback: (message: String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }


}


class WalletUsecase @Inject constructor(var wallet : IWallet , var paymentRepo : PaymentRepository) {
    var error : MutableLiveData<String> = wallet.error

    suspend fun getWalletBalance() : Double?{
        var result = paymentRepo.getWalletBalance().data
        return result
    }


    suspend fun rechargeWallet(amount: Int, identifier: String , paymentVendorType: Int) =
        wallet.rechargeWallet(amount , identifier , paymentVendorType)


    suspend fun transferGameRewardToWallet(amount: Int, adId : String , teamId : String , discountCode : String) : Boolean?{
        var result = paymentRepo.transferRewardToWallet(amount , adId , teamId , discountCode)
        return result.data
    }

    suspend fun transferCommission(amount: Double) : Boolean?{
        var result = paymentRepo.transferCommissionToWallet(amount )
        return result.data
    }

    suspend fun transferGamePrize(amount: Double) : Boolean?{
        var result = paymentRepo.transferGameReward(amount )
        return result.data
    }


    suspend fun makeDonation(donation: Donation) : Boolean?{
        var result = wallet.makeDonation(donation)
        return  result
    }

    suspend fun getWalletTransactions() : List<Transaction>?{
        var result = paymentRepo.getWalletTransactions().data
        return result
    }
}