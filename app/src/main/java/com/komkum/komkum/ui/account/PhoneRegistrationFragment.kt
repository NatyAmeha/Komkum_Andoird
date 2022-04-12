package com.komkum.komkum.ui.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.FirebaseException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.komkum.komkum.MainActivity
import com.komkum.komkum.OnboardingActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Error
import com.komkum.komkum.data.model.User
import com.komkum.komkum.databinding.FragmentPhoneRegistrationBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.sendIntent
import com.komkum.komkum.util.extensions.showDialog
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


@AndroidEntryPoint
class PhoneRegistrationFragment : Fragment() {

    lateinit var binding : FragmentPhoneRegistrationBinding
    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    var resendCodeAgain = true
    var canExitRegistration = true

    lateinit var timer : TimerTask

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPhoneRegistrationBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                if(canExitRegistration)
                    findNavController().navigateUp()
                else{
                    requireActivity().showDialog(getString(R.string.discard) , showNegative = true ,
                        positiveButtonText = getString(R.string.yes)){
                        findNavController().navigateUp()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.register))
        binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumberEditText)

        binding.phoneNumberEditText.doOnTextChanged { text, start, before, count ->
            binding.sendCodeBtn.isEnabled = count > 0
        }

        binding.sendCodeBtn.setOnClickListener {
            canExitRegistration = false
            binding.registrationProgressbar.isVisible = true
            binding.sendCodeBtn.isEnabled = false
            var phoneNumber = binding.countryCodePicker.fullNumberWithPlus
            verifyPhoneNumber(phoneNumber)
            timer = Timer().schedule(60*1000){
                activity?.runOnUiThread {
                    if(resendCodeAgain){
                        binding.registrationProgressbar.isVisible = false
                        Toast.makeText(requireContext() , getString(R.string.unable_to_send_code) , Toast.LENGTH_LONG).show()
                        binding.sendCodeBtn.text = getString(R.string.resend_code)
                        binding.sendCodeBtn.isEnabled = true
                    }
                }
            }
        }

        binding.registerBtn.setOnClickListener {
            var username = binding.usernameEditText.text.toString()
            var password = binding.passwordEditText.text.toString()
            var phoneNumber = binding.countryCodePicker.fullNumberWithPlus
            if(username.isNotEmpty() && password.isNotEmpty()){
                var userInfo = User(username = username , password = password , phoneNumber = phoneNumber)
                register(userInfo)
            }
            else Toast.makeText(requireContext() , getString(R.string.fill_all_form_fields) , Toast.LENGTH_LONG).show()
        }

        viewmodel.getError().observe(viewLifecycleOwner){}
        viewmodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext() , onUnAuthorizedError =  {viewmodel.removeOldErrors()}){
                if(it?.contains(Error.PHONE_NUMBER_NOT_UNIQUE_ERROR_MSG) == true){
                    viewmodel.removeOldErrors()
                    MaterialDialog(requireContext()).show {
                        title(text = getString(R.string.error))
                        message(text = getString(R.string.phone_number_not_unique))
                        positiveButton(text = getString(R.string.go_to_login_page)){
                            findNavController().navigate(R.id.loginFragment)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        if(::timer.isInitialized) timer.cancel()
        super.onDestroyView()
    }

    fun verifyPhoneNumber(phoneNumber : String){
        var callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                resendCodeAgain = false
                binding.registrationProgressbar.isVisible = false
                binding.sendCodeBtn.isVisible = false
                binding.resendCodeBtn.isVisible = false
                binding.phonenumberTextInput.isEnabled = false
                binding.countryCodePicker.isEnabled = false
                binding.usernameTextInput.isVisible = true
                binding.passowrdTextInput.isVisible = true
                binding.registerBtn.isVisible = true
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                binding.registrationProgressbar.isVisible = false
                binding.sendCodeBtn.isEnabled = true
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

    fun register(userinfo : User){
        var pref = PreferenceHelper.getInstance(requireContext())
        binding.registrationProgressbar.isVisible = true
        viewmodel.signUpWithPhoneNumber(userinfo).observe(viewLifecycleOwner){result ->
            binding.registrationProgressbar.isVisible = false
            canExitRegistration = true
            when(result){
                is AccountState.Registered -> {
                    viewmodel.user  = result.user
                    pref[PreferenceHelper.WALLET_BONUS_DIALOG_BEFORE_SIGN_UP] = false
                    pref[PreferenceHelper.WALLET_BONUS_DIALOG_AFTER_SIGN_UP] = true
                    pref[PreferenceHelper.FIRST_TIME_FOR_RECOMMENDATION_DIALOG] = true
                    pref[PreferenceHelper.FIRST_TIME_FOR_GAME_INSTRUCTION] = true
                    viewmodel.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP){
                        param(FirebaseAnalytics.Param.METHOD ,  FcmService.F_EPV_PHONE)
                        param(FcmService.F_EP_SIGN_UP_SOURCE , viewmodel.signupSource ?: FcmService.F_EPV_ORGANIC)
                    }

                    var isNavRequireLogin = pref.get(AccountState.IS_REDIRECTION , false)
                    if(isNavRequireLogin){
                        pref[AccountState.IS_REDIRECTION] = false
                        pref[PreferenceHelper.RELOAD_STORE_PAGE] = true
                        requireActivity().finish()
                    }
                    else{
                        pref[PreferenceHelper.RELOAD_STORE_PAGE] = false
                        this.sendIntent(MainActivity::class.java)
                    }
                }
                is AccountState.UnRegistered -> {
                    result.message?.let { Log.i("error" , it) }
                    Toast.makeText(context , result.message , Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}