package com.example.ethiomusic.ui.account.subscription

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.example.ethiomusic.MainActivity

import com.example.ethiomusic.OnboardingActivityViewmodel
import com.example.ethiomusic.R


import com.example.ethiomusic.data.model.SubscriptionPlan
import com.example.ethiomusic.databinding.FragmentSubscriptionBinding
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.IYenepayPaymentHandler
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.errors.InvalidPaymentException
import com.yenepaySDK.handlers.PaymentHandlerActivity
import com.yenepaySDK.model.OrderedItem

/**
 * A simple [Fragment] subclass.
 */
class SubscriptionFragment : Fragment() , IRecyclerViewInteractionListener<SubscriptionPlan> , IYenepayPaymentHandler{



    lateinit var binding: FragmentSubscriptionBinding
    lateinit var preference : SharedPreferences
    val viewmodel : OnboardingActivityViewmodel by activityViewModels()


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

    override fun onItemClick(data: SubscriptionPlan, position: Int, option: Int?) {
        preference = PreferenceHelper.getInstance(requireContext())
        var paymentmethods = listOf<GridItem>(
            BasicGridItem(R.drawable.ic_baseline_payment_24 , "YenePay"),
            BasicGridItem(R.drawable.ic_baseline_payment_24 , "PayPal"),
            BasicGridItem(R.drawable.ic_baseline_payment_24 , "Stripe"),
            BasicGridItem(R.drawable.ic_baseline_payment_24 , "Free")
        )
        MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(text = "Choose your Payment Method")
            cornerRadius(14f)
            cancelOnTouchOutside(true)
            gridItems(items = paymentmethods){dialog, index, item ->
                when(index){
                    0 -> {
//                        viewmodel.subscriptionRequest(requireContext()  , data  ,"YENEPAY").observe(viewLifecycleOwner , Observer{state ->
//
//                        })

                        var merchantId = "0235"
                        var orderId = "${data._id}"
                        var orderItem = OrderedItem(data._id , data.name ,  1 , data.priceInBirr!!.toDouble())
                        var returnUrl = "com.example.ethiomusic.yenepay:/payment2redirect"

                        var paymentOrder = PaymentOrderManager(merchantId , orderId)
                        paymentOrder.paymentProcess = PaymentOrderManager.PROCESS_EXPRESS
                        paymentOrder.isUseSandboxEnabled = true
                        paymentOrder.isShoppingCartMode = false
                        paymentOrder.returnUrl = returnUrl
                        try {
                            paymentOrder.addItem(orderItem)

                            var payment = paymentOrder.generatePayment()
                            var intent = paymentOrder.getPaymentRequestIntent(context, payment);
                            startActivityForResult(intent, PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE);

                        }catch (ex : InvalidPaymentException){
                            Toast.makeText(context , ex.message , Toast.LENGTH_LONG).show()
                        }
                    }
                    1 -> Toast.makeText(requireContext() , item.title , Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext() , item.title , Toast.LENGTH_SHORT).show()
                    3 -> {
                        binding.subscriptionProgressbar.visibility = View.VISIBLE
                        viewmodel.subscriptionRequest(requireActivity()  , data  ,"NOPAYMENT").observe(viewLifecycleOwner , Observer{state ->
//                            preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.HALF_REGISTERED_PREFERENCE_VALUE
//                            preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
                            preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                            if(this@SubscriptionFragment.toControllerActivity().isHalfRegistered(requireContext())){
                                dialog.dismiss()
                                findNavController().navigate(R.id.action_subscriptionFragment_to_artistSelectionListFragment)
                            }
                            else{
                                dialog.dismiss()
                                sendIntent(MainActivity::class.java)
                            }
                        })

                    }
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.i("paymentresultm" , requestCode.toString()+ resultCode.toString())

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
                    Toast.makeText(context , "canceled"+response.statusText , Toast.LENGTH_LONG).show()
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                var error = data?.getStringExtra(PaymentHandlerActivity.KEY_ERROR_MESSAGE) ?: "Unkonw system error occured"
                Toast.makeText(requireContext() , error , Toast.LENGTH_LONG).show()
            }
        }
        else super.onActivityResult(requestCode, resultCode, data)
//        Toast.makeText(requireContext() , "response arrived" , Toast.LENGTH_LONG).show()
    }

    override fun activiateMultiSelectionMode() {

    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun oncomplete() {
        Toast.makeText(requireContext() , "result arrved " , Toast.LENGTH_LONG).show()
    }


}
