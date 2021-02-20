package com.zomatunes.zomatunes.ui.account.subscription

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.zomatunes.zomatunes.MainActivity

import com.zomatunes.zomatunes.OnboardingActivityViewmodel
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.config.AppKey


import com.zomatunes.zomatunes.data.model.SubscriptionPlan
import com.zomatunes.zomatunes.data.model.VerifyPayment
import com.zomatunes.zomatunes.databinding.FragmentSubscriptionBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.util.IYenepayPaymentHandler
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.handlers.PaymentHandlerActivity

/**
 * A simple [Fragment] subclass.
 */
class SubscriptionFragment : Fragment() , IRecyclerViewInteractionListener<SubscriptionPlan> , IYenepayPaymentHandler{

    lateinit var binding: FragmentSubscriptionBinding
    lateinit var preference : SharedPreferences
    lateinit var subscriptionPlan: SubscriptionPlan

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    val PAYPAL_PAYMENT_CODE = 7777

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSubscriptionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel

        viewmodel.subscriptionList.observe(viewLifecycleOwner , Observer{resource ->
            var subscriptionAdapter = SubscriptionAdapter(this)
            binding.subscriptionRecyclerview.adapter = subscriptionAdapter
            binding.subscriptionRecyclerview.layoutManager = LinearLayoutManager(context)
            subscriptionAdapter.submitList(resource.data)
        })
//        when {
//            this.toControllerActivity().isHalfRegistered(view.context) -> binding.textView2.text = "Select Your Subscription Plan"
//            else -> binding.textView2.text = "Your Subscription Plan has been expired Subscribe again"
//        }
    }

    override fun onDestroy() {
        var paypalserviceIntent = Intent(requireContext() , PayPalService::class.java)
        requireActivity().stopService(paypalserviceIntent)
        super.onDestroy()
    }

    override fun onItemClick(data: SubscriptionPlan, position: Int, option: Int?) {
        preference = PreferenceHelper.getInstance(requireContext())
        subscriptionPlan = data
        var paymentmethods = listOf<GridItem>(
            BasicGridItem(R.drawable.ic_yenepaylogo , "YenePay"),
            BasicGridItem(R.drawable.ic_paypal , "PayPal"),
            BasicGridItem(R.drawable.ic_stripe_vector_logo , "Stripe"),
            BasicGridItem(R.drawable.ic_baseline_payment_24 , "Free")
        )
        MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Payment Method")
            message(text = "Choose your payment method to complete payment")
            cornerRadius(14f)
            cancelOnTouchOutside(true)
            gridItems(items = paymentmethods){dialog, index, item ->
                when(index){
                    0 -> {
                        var preference = PreferenceHelper.getInstance(requireContext())
                        preference["PAYMENT_FOR"] = PaymentManager.PAYMENT_FOR_SUBSCRIPTION
                        preference["SUB_USER_ID"] = viewmodel.user!!._id!!
                        preference["SUB_SUBSCRIPTION_ID"] = subscriptionPlan._id!!
                        viewmodel.handleyenepayPayment(data.priceInBirr!!.toDouble() , data._id!! , data.name!! , requireActivity())
                    }
                    1 -> paypalPaymentHandler(data)
                    2 -> Toast.makeText(requireContext() , item.title , Toast.LENGTH_SHORT).show()
                    3 -> {
                        binding.subscriptionProgressbar.visibility = View.VISIBLE
                        handleFreePayment(data)
//                        viewmodel.subscriptionRequest(requireActivity()  , data  ,"NOPAYMENT").observe(viewLifecycleOwner , Observer{state ->
//                        })

//                        preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.HALF_REGISTERED_PREFERENCE_VALUE
//                            preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
//
//                        preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                        if(this@SubscriptionFragment.toControllerActivity().isHalfRegistered(requireContext())){
//                            dialog.dismiss()
//                            findNavController().navigate(R.id.categorySelectionFragment)
//                        }
//                        else{
//                            dialog.dismiss()
//                            sendIntent(MainActivity::class.java)
//                        }

                        dialog.dismiss()
                        this@SubscriptionFragment.toControllerActivity().updateToFullyRegisteredAccount(view.context)
                        sendIntent(MainActivity::class.java)

                    }
                }
            }
        }
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
                Toast.makeText(requireActivity() , "enter activity result complete" , Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context , "canceled"+response.statusText , Toast.LENGTH_LONG).show()
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                var error = data?.getStringExtra(PaymentHandlerActivity.KEY_ERROR_MESSAGE) ?: "Unkonw system error occured"
                Toast.makeText(requireContext() , error , Toast.LENGTH_LONG).show()
            }
        }
        else if(requestCode == PAYPAL_PAYMENT_CODE){
            when (resultCode) {
                Activity.RESULT_OK -> {
                    var verifyPaymentInfo = VerifyPayment(viewmodel.user!!._id!! , subscriptionPlan._id , VerifyPayment.SUBSCRIPTION_PAYMENT_TYPE)
                    viewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(viewLifecycleOwner , Observer{accountState ->
                        when(accountState){
                            is AccountState.ValidSubscription ->{
                                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                                sendIntent(MainActivity::class.java)
//                                if(this@SubscriptionFragment.toControllerActivity().isHalfRegistered(requireContext())){
//                                    findNavController().navigate(R.id.categorySelectionFragment)
//                                }
//                                else{
//                                    sendIntent(MainActivity::class.java)
//                                }
                            }
                            is AccountState.NoSubscription ->{
                                Toast.makeText(requireContext() , "${accountState.message} subscription" , Toast.LENGTH_LONG).show()
                            }
                        }
                    })
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

    fun handleFreePayment(subscriptionPlan: SubscriptionPlan){
        var verifyPaymentInfo = VerifyPayment(viewmodel.user!!._id!! , subscriptionPlan._id , VerifyPayment.FREE_PAYMENT_TYPE)
        viewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(viewLifecycleOwner , Observer{accountState ->
            when(accountState){
                is AccountState.ValidSubscription ->{
                    preference[AccountState.USED_AUDIOBOOK_CREDIT] = 0
                    preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                    sendIntent(MainActivity::class.java)
                }
                is AccountState.NoSubscription ->{
                    Toast.makeText(requireContext() , "${accountState.message} INVALID subscription" , Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    override fun oncomplete() {
        Toast.makeText(requireContext() , "result arrved " , Toast.LENGTH_LONG).show()
    }


}
