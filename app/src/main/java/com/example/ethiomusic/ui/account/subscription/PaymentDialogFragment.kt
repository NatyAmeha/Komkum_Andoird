package com.example.ethiomusic.ui.account.subscription

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.OnboardingActivityViewmodel
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.SubscriptionPlan
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import kotlinx.android.synthetic.main.fragment_payment_dialog.*
import androidx.recyclerview.widget.GridLayoutManager as GridLayoutManager1

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"


class PaymentDialogFragment : BottomSheetDialogFragment() , IRecyclerViewInteractionListener<String> {

     var selectedSubscription : SubscriptionPlan? = null
    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    companion object {
        // TODO: Customize parameters
        fun newInstance(data : SubscriptionPlan): PaymentDialogFragment =
            PaymentDialogFragment().apply {
                arguments =  bundleOf("SUBSCRIPTION" to data)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val paymentlist = listOf("YENEPAY" , "PAYPAL" , "NOPAYMENT")
         selectedSubscription   =  arguments?.getParcelable("SUBSCRIPTION")

        payment_List_recyclerview.layoutManager = GridLayoutManager1(context, 2)
        payment_List_recyclerview.adapter = PaymentAdapter(paymentlist  , this)
}


    override fun onItemClick(data: String, position: Int, option: Int?) {
//        viewmodel.subscriptionRequest(data , data).observe(viewLifecycleOwner , Observer{ state ->
//            when(state){
//                is AccountState.ValidSubscription ->{
//                    if( this.toControllerActivity().isHalfRegistered(requireContext())){
//                        dialog?.dismiss()
//                        findNavController().navigate(R.id.action_subscriptionFragment_to_artistSelectionListFragment)
//                    }
//                    else{
//                        dialog?.dismiss()
//                        sendIntent(MainActivity::class.java)
//                    }
//                }
//                is AccountState.InvalidSubscription -> Toast.makeText(requireContext() , state.message , Toast.LENGTH_LONG).show()
//            }
//        })
    }

    override fun activiateMultiSelectionMode() {

    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }
}
