package com.zomatunes.zomatunes.ui.account.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.OnboardingActivityViewmodel

import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.AuthenticationModel
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_reset_password.*

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    val onboardingViewmodel : OnboardingActivityViewmodel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            var token = it.getString("TOKEN")
        }
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        reset_password_btn.setOnClickListener {
            reset_password_progressbar.isVisible = true
            var password = new_password_edit_text.text.toString()
            var info  = AuthenticationModel(password = password)
            var token = ""
            onboardingViewmodel.resetPassword(info , token).handleSingleDataObservation(viewLifecycleOwner){
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }

}
