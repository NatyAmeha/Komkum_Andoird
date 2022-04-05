package com.komkum.komkum.ui.account.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.komkum.komkum.OnboardingActivityViewmodel

import com.komkum.komkum.R
import com.komkum.komkum.data.model.AuthenticationModel
import com.komkum.komkum.databinding.FragmentForgotPasswordBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    lateinit var binding : FragmentForgotPasswordBinding
    val onboardingViewmodel : OnboardingActivityViewmodel by activityViewModels()

    var resendCodeAgain = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar, getString(R.string.forgot_password))

        binding.emailEditText.doOnTextChanged { text, start, before, count ->
            binding.sendEmailBtn.isEnabled = !text.isNullOrEmpty()
        }

        binding.sendEmailBtn.setOnClickListener {
            binding.emailSendProgressbar.isVisible = true
            binding.sendEmailBtn.isEnabled = false
            var emailOrPhone = binding.emailEditText.text.toString()
            var info = AuthenticationModel(email = emailOrPhone , phoneNumber = emailOrPhone)
            onboardingViewmodel.forgotPassword(info).handleSingleDataObservation(viewLifecycleOwner){
                it.resetToken?.let { it1 -> Log.i("resettoken" , it1) }
                if (!it.userFound){
                    binding.emailSendProgressbar.isVisible = false
                    Toast.makeText(requireContext() , getString(R.string.no_account_found) , Toast.LENGTH_LONG).show()
                }
                else if(it.userFound && it.phoneNumber != null){
                    Toast.makeText(requireContext() , getString(R.string.sending_verfication_code) , Toast.LENGTH_LONG).show()

                    verifyPhoneNumber(it.phoneNumber!! , it.resetToken!!)
                    Timer().schedule(60*1000){
                        requireActivity().runOnUiThread {
                            if(resendCodeAgain){
                                binding.emailSendProgressbar.isVisible = false
                                Toast.makeText(requireContext() , getString(R.string.unable_to_send_code) , Toast.LENGTH_LONG).show()
                                binding.sendEmailBtn.text = getString(R.string.resend_code)
                                binding.sendEmailBtn.isEnabled = true
                            }
                        }
                    }
                }
                else if(it.userFound && it.phoneNumber == null){
                    Toast.makeText(requireContext() , getString(R.string.email_sent_to_email) , Toast.LENGTH_LONG).show()
                    binding.emailSendProgressbar.isVisible = false
                }
                else {
                    binding.sendEmailBtn.text = "Resend"
                    Toast.makeText(requireContext() , "Email not sent. try again" , Toast.LENGTH_LONG).show()
                }
            }
        }

        onboardingViewmodel.getError().observe(viewLifecycleOwner){ }
        onboardingViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.sendEmailBtn.isEnabled = true
                 binding.emailSendProgressbar.isVisible = false
                 Toast.makeText(requireContext() , it ?: "error occured" , Toast.LENGTH_LONG).show()
            }
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


    fun verifyPhoneNumber(phoneNumber : String , resetToken : String){
        var callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                resendCodeAgain = false
                binding.emailSendProgressbar.isVisible = false
                binding.sendEmailBtn.isEnabled = true

                var bundle = bundleOf("PHONE_NUMBER" to phoneNumber , "RESET_TOKEN" to resetToken)
                findNavController().navigate(R.id.resetPasswordFragment , bundle)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                binding.emailSendProgressbar.isVisible = false
                binding.sendEmailBtn.isEnabled = true
                Toast.makeText(requireContext() , p0.message , Toast.LENGTH_LONG).show()
            }

        }
        var option = PhoneAuthOptions.newBuilder()
            .setActivity(requireActivity())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(option)

    }




}
