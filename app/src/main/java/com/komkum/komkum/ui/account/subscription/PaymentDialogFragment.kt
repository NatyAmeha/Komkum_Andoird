package com.komkum.komkum.ui.account.subscription

import android.os.Bundle
import android.os.Message
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.OnboardingActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.SubscriptionPlan
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.extensions.sendIntent
import com.komkum.komkum.util.extensions.toControllerActivity
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener

import androidx.recyclerview.widget.GridLayoutManager as GridLayoutManager1


class PaymentDialogFragment : Fragment() {

     var selectedSubscription : SubscriptionPlan? = null
    val viewmodel : OnboardingActivityViewmodel by activityViewModels()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            selectedSubscription = it.getParcelable("SUBSCRIPTION_PLAN")
        }
        return inflater.inflate(R.layout.fragment_payment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val paymentlist = listOf("YENEPAY" , "PAYPAL" , "NOPAYMENT")
//         selectedSubscription   =  arguments?.getParcelable("SUBSCRIPTION")

//        var webview  = yenepay_webview
//        webview.settings.javaScriptEnabled = true
//        webview.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
////                view?.goBack()
//                return false
//            }
//
//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                return super.shouldOverrideUrlLoading(view, request)
//            }
//
//            override fun onLoadResource(view: WebView?, url: String?) {
//                super.onLoadResource(view, url)
//            }
//
//            override fun shouldInterceptRequest(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): WebResourceResponse? {
//                return super.shouldInterceptRequest(view, request)
//            }
//
//            override fun onTooManyRedirects(
//                view: WebView?,
//                cancelMsg: Message?,
//                continueMsg: Message?
//            ) {
//                super.onTooManyRedirects(view, cancelMsg, continueMsg)
//            }
//        }

//        viewmodel.subscriptionRequest(requireContext()  , selectedSubscription!!  ,"YENEPAY").observe(viewLifecycleOwner , Observer{
//            webview.loadUrl(it)
//        })


}


}
