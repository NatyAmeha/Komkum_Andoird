package com.komkum.komkum.ui.payment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.databinding.FragmentWalletWelcomeBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set

class WalletWelcomeFragment : Fragment() {
    lateinit var binding : FragmentWalletWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWalletWelcomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pref = PreferenceHelper.getInstance(requireContext())
        binding.walletContinueBtn.setOnClickListener {
            pref[PreferenceHelper.FIRST_TIME_FOR_WALLET] = false
            findNavController().navigate(R.id.walletRechargeFragment)
        }
    }


}