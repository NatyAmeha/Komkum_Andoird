package com.zomatunes.zomatunes.ui.account.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.zomatunes.zomatunes.OnboardingActivityViewmodel

import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.AuthenticationModel
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_forgot_password.*

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    val onboardingViewmodel : OnboardingActivityViewmodel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        send_email_btn.setOnClickListener {
            email_send_progressbar.isVisible = true
            var email = email_edit_text.text.toString()
            var info = AuthenticationModel(email = email)
            onboardingViewmodel.forgotPassword(info).handleSingleDataObservation(viewLifecycleOwner){
                if(it) Toast.makeText(requireContext() , "Email sent to your email address" , Toast.LENGTH_LONG).show()
                else {
                    send_email_btn.text = "Resend"
                    Toast.makeText(requireContext() , "Email not sent. try again" , Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
