package com.komkum.komkum.ui.account.subscription

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity

import com.komkum.komkum.R
import com.komkum.komkum.databinding.FragmentCheckoutBinding
import com.komkum.komkum.util.extensions.sendIntent

/**
 * A simple [Fragment] subclass.
 */
class CheckoutFragment : Fragment() {

    lateinit var binding: FragmentCheckoutBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.paymentCompleteContinueBtn.setOnClickListener {
            this@CheckoutFragment.sendIntent(MainActivity::class.java)
        }
    }
}
