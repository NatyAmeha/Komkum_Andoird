package com.komkum.komkum.ui.account.subscription

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.android.billingclient.api.BillingClient
import com.komkum.komkum.MainActivity

import com.komkum.komkum.R
import com.komkum.komkum.config.AppKey


import com.komkum.komkum.databinding.FragmentSubscriptionBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.util.IYenepayPaymentHandler
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.handlers.PaymentHandlerActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.usecase.IPayment
import com.komkum.komkum.usecase.OnPaymentListener
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SubscriptionFragment : Fragment() , IRecyclerViewInteractionListener<SubscriptionPlan> , OnPaymentListener{

    lateinit var binding: FragmentSubscriptionBinding
    lateinit var preference : SharedPreferences
    var subscriptionPlan: SubscriptionPlan? = null

    val viewmodel : MainActivityViewmodel by activityViewModels()

    val PAYPAL_PAYMENT_CODE = 7777

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSubscriptionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.subscription_plans))
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        loadSubscriptionInfo()

        viewmodel.getError().observe(viewLifecycleOwner){ Log.i("billing or mainerror" , it)}
        viewmodel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext() ,  {viewmodel.removeOldErrors()},
                viewmodel.signUpSource){
                binding.subscriptionProgressbar.isVisible = false
                if(viewmodel.subscriptionList.value.isNullOrEmpty()){
                    binding.tryAgainBtn.isVisible = true
                    binding.subscriptionErrorTextview.isVisible = true
                }
                viewmodel.error.value = null
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }

        binding.tryAgainBtn.setOnClickListener {
            loadSubscriptionInfo()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadSubscriptionInfo(){
        viewmodel.subscriptionList.observe(viewLifecycleOwner , Observer{
            binding.tryAgainBtn.isVisible = false
            binding.subscriptionErrorTextview.isVisible = false
            binding.subscriptionProgressbar.isVisible = false
            var subscriptionAdapter = SubscriptionAdapter(this)
            binding.subscriptionRecyclerview.adapter = subscriptionAdapter
            binding.subscriptionRecyclerview.layoutManager = LinearLayoutManager(context)
            subscriptionAdapter.submitList(it)
        })
    }


    override fun onDestroy() {
        var paypalserviceIntent = Intent(requireContext() , PayPalService::class.java)
        requireActivity().stopService(paypalserviceIntent)
        super.onDestroy()
    }

    override fun onItemClick(data: SubscriptionPlan, position: Int, option: Int?) {
        preference = PreferenceHelper.getInstance(requireContext())
        subscriptionPlan = data
        handlePaymentForSubscription(subscriptionPlan!!)


//        var paymentmethods = listOf<GridItem>(
//            BasicGridItem(R.drawable.ic_baseline_payment_24 , "Wallet"),
//            BasicGridItem(R.drawable.google_logo , "Google play")
//        )
//        MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
//            title(text = "Payment Method")
//            message(text = "Choose your payment method to complete payment")
//            cornerRadius(14f)
//            cancelOnTouchOutside(true)
//            var a = gridItems(items = paymentmethods){dialog, index, item ->
//                when(index){
//                    0 ->{
//                        binding.subscriptionProgressbar.isVisible = true
//                        viewmodel.initWalletPaymentFlow(Transaction.TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE ,
//                            data.priceInBirr!!.toDouble() , data._id!! , requireContext() , data.name!! ,
//                            viewLifecycleOwner , this@SubscriptionFragment)
//                    }
//                    1 ->{
//                        binding.subscriptionProgressbar.isVisible = true
//                        viewmodel.initGooglePlayPaymentFlow(Transaction.TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE ,
//                            data.priceInDollar!!.toDouble() , data.googleplaySubscriptionId!! ,
//                            requireContext() , data.name!! , viewLifecycleOwner , this@SubscriptionFragment)
//                    }
//
////                    handlegooglePlayPayment(data)
//
////                    2 -> {  // free trial subscription
////                        binding.subscriptionProgressbar.visibility = View.VISIBLE
////                        handleFreePayment(data)
////                        dialog.dismiss()
////                        preference.set(AccountState.SUBSCRIPTON_PREFERENCE , AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE)
////                        sendIntent(MainActivity::class.java)
////                    }
//                }
//            }
//        }

    }

    fun paypalPaymentHandler(data : SubscriptionPlan){
        var paypalServiceConfig = PayPalConfiguration().acceptCreditCards(true)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(AppKey.paypalClientId).merchantName("ZomaTunes")

        var serviceIntent = Intent(requireContext() , PayPalService::class.java)
        serviceIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION , paypalServiceConfig)
        requireActivity().startService(serviceIntent)

        var paymentInfo = PayPalPayment(data.priceInDollar!!.toBigDecimal() , "USD" , data.name , PayPalPayment.PAYMENT_INTENT_SALE)
        var paymentIntent = Intent(requireContext() , PaymentActivity::class.java)
        paymentIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION , paypalServiceConfig)
        paymentIntent.putExtra(PaymentActivity.EXTRA_PAYMENT , paymentInfo)
        startActivityForResult(paymentIntent , PAYPAL_PAYMENT_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

//        Log.i("paymentresultm" , requestCode.toString()+ resultCode.toString())
//        var response = PaymentOrderManager.parseResponse(data)
//        Log.i("paymentr" , response.isPaymentCompleted.toString())

        if(requestCode == PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE){
            Log.i("paymentresult" , "result")
            if(resultCode == Activity.RESULT_OK){
                var response = PaymentOrderManager.parseResponse(data)
                if(response.isPaymentCompleted){
                    Toast.makeText(context , "completed" , Toast.LENGTH_LONG).show()
//                    preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                    if(this@SubscriptionFragment.toControllerActivity().isHalfRegistered(requireContext())){
//                        findNavController().navigate(R.id.action_subscriptionFragment_to_artistSelectionListFragment)
//                    }
//                    else{
//                        sendIntent(MainActivity::class.java)
//                    }
                }
                else if(response.isCanceled){
                    Log.i("paymenterrorcancel" , "thepayment error")
                    Toast.makeText(context , "canceled"+response.statusText , Toast.LENGTH_LONG).show()
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Log.i("paymenterror" , "thepayment error")
                var error = data?.getStringExtra(PaymentHandlerActivity.KEY_ERROR_MESSAGE) ?: "Unkonw system error occured"
                Toast.makeText(requireContext() , error , Toast.LENGTH_LONG).show()
            }
        }
        else if(requestCode == PAYPAL_PAYMENT_CODE){
            when (resultCode) {
                Activity.RESULT_OK -> {
//                    var verifyPaymentInfo = VerifyPayment(viewmodel.user!!._id!! , subscriptionPlan._id , VerifyPayment.PAYMENT_METHOD_PAYPAL)
//                    viewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(viewLifecycleOwner , Observer{accountState ->
//                        when(accountState){
//                            is AccountState.ValidSubscription ->{
//                                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                                sendIntent(MainActivity::class.java)
////                                if(this@SubscriptionFragment.toControllerActivity().isHalfRegistered(requireContext())){
////                                    findNavController().navigate(R.id.categorySelectionFragment)
////                                }
////                                else{
////                                    sendIntent(MainActivity::class.java)
////                                }
//                            }
//                            is AccountState.NoSubscription ->{
//                                Toast.makeText(requireContext() , "${accountState.message} subscription" , Toast.LENGTH_LONG).show()
//                            }
//                        }
//                    })
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(requireContext() , "Payment Cancelled" , Toast.LENGTH_LONG).show()
                }
                PaymentActivity.RESULT_EXTRAS_INVALID -> {
                    Toast.makeText(requireContext() , "Invalid Payment session" , Toast.LENGTH_LONG).show()
                }
            }
        }
        else super.onActivityResult(requestCode, resultCode, data)
//        Toast.makeText(requireContext() , "response arrived" , Toast.LENGTH_LONG).show()
    }

//    fun handleFreePayment(subscriptionPlan: SubscriptionPlan){
//        var pref = PreferenceHelper.getInstance(requireContext())
//        var userId = pref.get(AccountState.USER_ID , "")
//        if(userId.isNotBlank()){
//            var verifyPaymentInfo = VerifyPayment(userId , subscriptionPlan._id , VerifyPayment.FREE_PAYMENT_TYPE)
//            viewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(viewLifecycleOwner , Observer{accountState ->
//                when(accountState){
//                    is AccountState.ValidSubscription ->{
//                        preference[AccountState.USED_AUDIOBOOK_CREDIT] = 0
//                        preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                        sendIntent(MainActivity::class.java)
//                    }
//                    is AccountState.NoSubscription ->{
//                        Toast.makeText(requireContext() , "${accountState.message} INVALID subscription" , Toast.LENGTH_LONG).show()
//                    }
//                }
//            })
//        }
//
//    }

    fun handlePaymentForSubscription(subscriptionInfo : SubscriptionPlan){
        binding.subscriptionProgressbar.isVisible = true
        viewmodel.getWalletBalance().observe(viewLifecycleOwner){
            binding.subscriptionProgressbar.isVisible = false

            var paymentmethods = listOf(
                PaymentMethod(getString(R.string.wallet) , "${getString(R.string.balance)} - ${getString(R.string.birr)} $it" , R.drawable.ic_baseline_account_balance_wallet_24),
                PaymentMethod("Google play" , "Use your credit and debit card" , R.drawable.google_logo)
            )
            viewmodel.showPaymentMethodsDialog(requireContext() , paymentmethods , subscriptionInfo.priceInBirr?.toDouble() ?: 0.0, viewLifecycleOwner){index ->
                when(index){
                    0 ->{
                        binding.subscriptionProgressbar.isVisible = true
                        viewmodel.initWalletPaymentFlow(Transaction.TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE ,
                            subscriptionInfo.priceInBirr!!.toDouble() , subscriptionInfo._id!! , requireContext() , subscriptionInfo.name!! ,
                            viewLifecycleOwner , this@SubscriptionFragment)
                    }
                    1 ->{
                        binding.subscriptionProgressbar.isVisible = true
                        viewmodel.initGooglePlayPaymentFlow(Transaction.TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE ,
                            subscriptionInfo.priceInDollar!!.toDouble() , subscriptionInfo.googleplaySubscriptionId!! ,
                            requireContext() , subscriptionInfo.name!! , viewLifecycleOwner , this@SubscriptionFragment)
                    }
                }
            }

        }

    }



//    fun handlegooglePlayPayment(plan: SubscriptionPlan){
//        binding.subscriptionProgressbar.isVisible = true
//        viewmodel.googlePlaysubscriptionPurchaseState.observe(viewLifecycleOwner){
//            binding.subscriptionProgressbar.isVisible = false
//            it?.let { resource ->
//                when(resource){
//                    is  Resource.Success -> {
//                        var pref = PreferenceHelper.getInstance(requireContext())
//                        var userId = pref.get(AccountState.USER_ID , "")
//                        if(userId.isNotBlank()){
//                            var verifyPaymentInfo = VerifyPayment(userId , plan._id ,
//                                VerifyPayment.PAYMENT_METHOD_GOOGLE_PAY , resource.data!!.purchaseToken)
//                            viewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(viewLifecycleOwner , Observer{accountState ->
//                                binding.subscriptionProgressbar.isVisible = false
//                                when(accountState){
//                                    is AccountState.ValidSubscription ->{
//                                        viewmodel.acknowledgeSubscription(resource.data!!.purchaseToken).observe(viewLifecycleOwner){
//                                            if(it.responseCode == BillingClient.BillingResponseCode.OK){
//                                                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                                                sendIntent(MainActivity::class.java)
//                                            }
//                                            else {
//                                                Log.i("billing acknowledge", it.debugMessage)
//                                                Toast.makeText(requireContext(), "Unable to acknowledge purchase ${it.responseCode}", Toast.LENGTH_LONG).show()
//                                            }
//                                        }
//                                    }
//                                    is AccountState.NoSubscription ->{
//                                        Toast.makeText(requireContext() , "${accountState.message} subscription" , Toast.LENGTH_LONG).show()
//                                    }
//                                }
//                            })
//                        }
//
//                    }
//                    is  Resource.Error ->{
//                        binding.subscriptionProgressbar.isVisible = false
//                        requireActivity().showDialog("Error", resource.message ?: "Error occured payment will refunded" , "Ok"){}
//                    }
//
//                }
//            }
//        }
//
//        plan.googleplaySubscriptionId?.let {
//            viewmodel.purchaseUsingGooglePlay(it, BillingClient.SkuType.SUBS)
//        }
//    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


    override fun onPaymentCompleted(paymentMethod: IPayment, amount: Double, token: String?) {
        binding.subscriptionProgressbar.isVisible = true
        Toast.makeText(requireContext() , "payment completed" , Toast.LENGTH_SHORT).show()
       if(subscriptionPlan != null){
           viewmodel.handleSubscriptionUpgrade(paymentMethod , subscriptionPlan!!._id!! , token){
               binding.subscriptionProgressbar.isVisible = false
               preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
               viewmodel.firebaseAnalytics.setUserProperty(FcmService.MEMBERSHIP_PLAN , subscriptionPlan!!.name)
               findNavController().navigate(R.id.storeHomepageFragment)
           }
       }
    }
}
