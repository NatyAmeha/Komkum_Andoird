package com.komkum.komkum.ui.account.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.komkum.komkum.OnboardingActivityViewmodel

import com.komkum.komkum.R
import com.komkum.komkum.data.model.AuthenticationModel
import com.komkum.komkum.databinding.FragmentResetPasswordBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {
    lateinit var binding : FragmentResetPasswordBinding
    val onboardingViewmodel : OnboardingActivityViewmodel by activityViewModels()
    var resetToken : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            resetToken = it.getString("RESET_TOKEN")
        }
        binding = FragmentResetPasswordBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when(item.itemId){
            android.R.id.home -> {
                MaterialDialog(requireContext()).show {
                    title(res = R.string.discard)
                    positiveButton(res = R.string.yes){ findNavController().navigate(R.id.loginFragment)}
                    negativeButton(res = R.string.cancel)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        Toast.makeText(requireContext() , resetToken , Toast.LENGTH_LONG).show()
        configureActionBar(binding.toolbar , getString(R.string.reset_password))
        Toast.makeText(requireContext() , getString(R.string.verification_succeeded) , Toast.LENGTH_LONG).show()
        binding.resetPasswordBtn.setOnClickListener {
            binding.resetPasswordProgressbar.isVisible = true
            var password = binding.newPasswordEditText.text.toString()
            var confirmPassword = binding.confirmNewPasswordEditText.text.toString()
            if(password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword){
                binding.resetPasswordProgressbar.isVisible = true
                var info  = AuthenticationModel(password = password)
                resetToken?.let {
                    onboardingViewmodel.resetPassword(info , it).handleSingleDataObservation(viewLifecycleOwner){
                        Toast.makeText(requireContext() , getString(R.string.password_reset_successful) , Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.loginFragment)
                    }
                }
            }
            else{
                binding.resetPasswordProgressbar.isVisible = false
                if(password.isNotBlank() || confirmPassword.isNotBlank())
                    Toast.makeText(requireContext() , getString(R.string.fill_form_correctly) , Toast.LENGTH_LONG).show()
                else
                    binding.confirmNewPasswordEditText.error = getString(R.string.password_not_correct)
            }


        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            MaterialDialog(requireContext()).show {
                title(res = R.string.discard)
                positiveButton(res = R.string.yes){ findNavController().navigate(R.id.loginFragment)}
                negativeButton(res = R.string.cancel)
            }

        }

        onboardingViewmodel.getError().observe(viewLifecycleOwner){}
        onboardingViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.resetPasswordProgressbar.isVisible = false
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }
    }

}
