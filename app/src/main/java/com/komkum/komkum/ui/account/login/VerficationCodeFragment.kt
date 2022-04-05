package com.komkum.komkum.ui.account.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.komkum.komkum.R
import com.komkum.komkum.databinding.FragmentVerficationCodeBinding
import com.komkum.komkum.util.extensions.configureActionBar

import java.util.concurrent.TimeUnit

class VerficationCodeFragment : Fragment() {

    lateinit var binding : FragmentVerficationCodeBinding
    var phoneNUmber : String? = null
    var verficationCode : String? = null
    var resetToken : String? = null

    var isCodeSent = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            phoneNUmber = it.getString("PHONE_NUMBER")
            resetToken = it.getString("RESET_TOKEN")
        }
        binding = FragmentVerficationCodeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "Verify")
        phoneNUmber?.let {
            binding.phoneNumberTextview.text = "${getString(R.string.verification_code_sent_message)} $phoneNUmber"

            var callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    verficationCode = p0.smsCode
                    isCodeSent = true
                    Log.i("firebasecompleted" , "completed $verficationCode ?: null object")
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    if (p0 is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request  // invalid format
                        binding.verifyCodeProgressbar.isVisible = false
                        Log.i("firebasefailed" , p0.toString())
                        Toast.makeText(requireContext() ,p0.message , Toast.LENGTH_LONG).show()
                        isCodeSent = false
                        // ...
                    } else if (p0 is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Log.i("firebasefailed" , p0.toString())
                        Toast.makeText(requireContext() , "request limit exceeded. Please try again later" , Toast.LENGTH_LONG).show()
                        binding.verifyCodeProgressbar.isVisible = false
                        // switch to email send
                    }
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
//                verficationCode = PhoneAuthCredential..smsCode .smsCode
                    Log.i("firebasesend" , p0 )
                    binding.verifyCodeProgressbar.isVisible = false
                    isCodeSent = true
                }

            }
            var options =  PhoneAuthOptions.newBuilder()
                .setPhoneNumber("+$it")
                .setTimeout(60 , TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callback)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        binding.verifyCodeEditText.doOnTextChanged { text, start, before, count ->
            binding.verfiyCodeBtn.isEnabled = !text.isNullOrEmpty() && isCodeSent
        }

        binding.verfiyCodeBtn.setOnClickListener {
            var code = binding.verifyCodeEditText.text.toString()
            verficationCode?.let { it1 -> Log.i("firebase ver" , it1) }
            var bundle  = bundleOf("token" to resetToken)
            if(code == verficationCode) findNavController().navigate(R.id.resetPasswordFragment , bundle)
            else Toast.makeText(requireContext() , getString(R.string.invalid_verification_code) , Toast.LENGTH_LONG).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().navigate(R.id.loginFragment)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("firebaseerrz" , "on actvity called")
        super.onActivityResult(requestCode, resultCode, data)
    }


}