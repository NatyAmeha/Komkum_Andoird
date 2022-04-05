package com.komkum.komkum.ui.payment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.chip.Chip
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Payment
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.databinding.FragmentWalletRechargeBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.ui.user.UserViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.PaymentMethodAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class WalletRechargeFragment : Fragment() {

    lateinit var binding : FragmentWalletRechargeBinding
    val userViewmodel : UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWalletRechargeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.recharge_wallet))
        binding.rechargeWalletBtn.setOnClickListener {
            var amount = binding.enterAmountEditText.text.toString().toInt()
            if(amount < 1) Toast.makeText(requireContext() , getString(R.string.recharge_amount_greater_than_25) , Toast.LENGTH_LONG).show()
            else showPaymentDialog(amount)

        }

        binding.amountChipGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.toList().firstOrNull() { (it as Chip).isChecked }?.let {
                var amount = (it as Chip).text.split(" ").first().toString().toInt()
                binding.enterAmountEditText.setText(amount.toString())
            }
        }

        binding.enterAmountEditText.doOnTextChanged { text, start, before, count ->
            if(!text.isNullOrBlank() ){
                var newAmount = text.toString().toInt()
                binding.rechargeWalletBtn.isVisible = newAmount >=1
            }
            else binding.rechargeWalletBtn.isVisible = false
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            (requireActivity() as MainActivity).showDialog(getString(R.string.discard) , null, getString(R.string.yes) , showNegative = true){
                if( findNavController().isFragmentInBackStack(R.id.accountFragment) )
                    findNavController().popBackStack(R.id.accountFragment , false)
                else findNavController().navigateUp()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                (requireActivity() as MainActivity).showDialog("Exit?" , null, getString(R.string.yes) , showNegative = true){
                    if( findNavController().isFragmentInBackStack(R.id.accountFragment) )
                        findNavController().popBackStack(R.id.accountFragment , false)
                    else findNavController().navigateUp()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }



    fun showPaymentDialog(totalAmount: Int){
        var dialog = MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT))
        dialog.cornerRadius(literalDp = 14f)
            .customView(R.layout.payment_method_custom_view , scrollable = true)
            .cancelOnTouchOutside(false)
        var interactionListener = object : IRecyclerViewInteractionListener<PaymentMethod> {
            override fun onItemClick(data: PaymentMethod, position: Int, option: Int?) {
                when(position){
                    0 -> {
                        dialog.dismiss()
                        rechargeWalletUsingYenepay(totalAmount)
                    }
                }
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var paymentmethods = listOf(PaymentMethod(getString(R.string.yenepay) , getString(R.string.yenepay_description) , R.drawable.ic_yenepaylogo),)

        var info = RecyclerViewHelper(interactionListener = interactionListener , owner = viewLifecycleOwner)
        var paymentAdapter = PaymentMethodAdapter(info , paymentmethods)

        var customview =  dialog.getCustomView()

        var totalPriceTextview = customview.findViewById<TextView>(R.id.total_price_tview)
        var paymentMethodsRecyclerview = customview.findViewById<RecyclerView>(R.id.payment_method_recyclerview)

        totalPriceTextview.text = "${getString(R.string.birr)} $totalAmount"
        paymentMethodsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        paymentMethodsRecyclerview.adapter = paymentAdapter
        dialog.show()

    }



     fun rechargeWalletUsingYenepay(amount : Int){
        var pref = PreferenceHelper.getInstance(requireContext())
        var date = Calendar.getInstance().time.toString()
        var userId = pref.get(AccountState.USER_ID , "")
        var uniqueId = userId
        pref["PAYMENT_FOR"] = PaymentManager.PAYMENT_FOR_WALLET_RECHARGE
        pref["PAYMENT_METHOD"] = Payment.PAYMENT_METHOD_YENEPAY
        pref["RECHARGE_AMOUNT"] = amount
        pref["PAYMENT_IDENTIFIER"] = uniqueId
         binding.rechargeWalletProgressbar.isVisible = false
        userViewmodel.startPaymentForWalletRecharge(amount , uniqueId , requireActivity())
    }
}