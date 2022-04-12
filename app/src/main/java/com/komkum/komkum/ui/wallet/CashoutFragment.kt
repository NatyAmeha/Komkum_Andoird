package com.komkum.komkum.ui.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Cashout
import com.komkum.komkum.data.model.Error
import com.komkum.komkum.data.viewmodel.Constants
import com.komkum.komkum.databinding.FragmentCashoutBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.user.UserViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.showDialog
import com.komkum.komkum.util.isVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CashoutFragment : Fragment() {

    lateinit var binding : FragmentCashoutBinding
    val userViewModel : UserViewModel by viewModels()

    var walletBalance : Int = 0
    var selectedCashoutMethod : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            walletBalance = it.getInt("WALLET_BALANCE" , 0)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCashoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.cash_out))
        var pref = PreferenceHelper.getInstance(requireContext())
        var isFirstTimeForCashout = pref.get(PreferenceHelper.FIRST_TIME_FOR_CASH_OUT_REQUEST , true)
        if(isFirstTimeForCashout){
            binding.cardView9.isVisible = true
            pref[PreferenceHelper.FIRST_TIME_FOR_CASH_OUT_REQUEST] = false
        }

        var userId = pref.get(AccountState.USER_ID ,"")
        var phoneNumber = pref.get(AccountState.PHONE_NUMBER , "")

        userViewModel.getCashoutRequests(userId).observe(viewLifecycleOwner){
            it?.let {
                if(it.amount != null && it.status != null){
                    binding.previousRequestGroup.isVisible = true
                    var requestTExt = "${getString(R.string.birr)} ${it.amount}"
                    binding.previousRequestTextview.text = requestTExt
                     when(it.status){
                        Cashout.CASHOUT_APPROVED -> {
                            binding.previousRequestStatusTextview.text = getString(R.string.approved)
                            binding.previousRequestStatusTextview.setTextColor(resources.getColor(R.color.primaryColor))
                        }

                        Cashout.CASHOUT_DECLINED -> {
                            binding.previousRequestStatusTextview.setTextColor(resources.getColor(R.color.red))
                            binding.previousRequestStatusTextview.text =getString(R.string.declined)
                        }
                        Cashout.CASHOUT_PENDING -> {
                            binding.requestCashOutBtn.isEnabled = false
                            binding.previousRequestStatusTextview.setTextColor(resources.getColor(R.color.gray_background))
                            binding.previousRequestStatusTextview.text = getString(R.string.pending)
                        }

                        else-> binding.previousRequestGroup.isVisible = false
                    }
                }
            }
        }


        var cashoutMessage = if(walletBalance < Constants.AMOUNT_ELIGABLE_FOR_CASHOUT)
            "${getString(R.string.you_have)} ${getString(R.string.birr)} $walletBalance ${getString(R.string.in_your_wallet)} ${getString(R.string.cashout_eligability_message)}"
        else "${getString(R.string.you_have)} ${getString(R.string.birr)} $walletBalance ${getString(R.string.in_your_wallet)} ${getString(R.string.you_can_only_withdraw_up_to)} ${getString(R.string.birr)} ${walletBalance - Constants.AMOUNT_LEFT_AFTER_CASHOUT} ${getString(R.string.up_to_amount)}"
        binding.walletBalanceTextview.text = cashoutMessage
        binding.walletBalanceTextview.isVisible = true
        binding.cashOutPhoneNumberEditText.setText(phoneNumber)

        var cashoutMethods = listOf(getString(R.string.mobile_card) ,
            getString(R.string.telebirr) ,
            getString(R.string.cbe_birr))
        var adapter = ArrayAdapter(requireContext() , android.R.layout.simple_list_item_1 , cashoutMethods)
        binding.cashOutMethodEditText.setAdapter(adapter)

        binding.closeBtn.setOnClickListener {
            binding.cardView9.isVisible = false
            pref[PreferenceHelper.FIRST_TIME_FOR_CASH_OUT_REQUEST] = false
        }

        binding.cashOutMethodEditText.setOnItemClickListener { adapterView, view, i, l ->
            selectedCashoutMethod = adapter.getItem(i)
        }

        binding.requestCashOutBtn.setOnClickListener {
            var amount = binding.cashOutAmountEditText.text.toString()
            phoneNumber = binding.cashOutPhoneNumberEditText.text.toString()

            if(userId.isNotBlank() && phoneNumber.isNotBlank() && amount.isNotEmpty() && !selectedCashoutMethod.isNullOrEmpty()){
                binding.progressbar.isVisible = true
                var cashoutInfo = Cashout(null, userId , amount.toInt() , phoneNumber , selectedCashoutMethod)
                userViewModel.requestCashout(cashoutInfo).observe(viewLifecycleOwner){
                    binding.progressbar.isVisible = false
                    it?.let {
                        if(it){
                                (requireActivity() as MainActivity).showDialog(getString(R.string.successful) ,
                                    getString(R.string.cashout_request_status_message), isBottomSheet = true){
                                }
                            binding.requestCashOutBtn.isEnabled = false
                            var requestTExt = "${getString(R.string.birr)} ${amount}"
                            binding.previousRequestTextview.text = requestTExt
                            binding.previousRequestStatusTextview.text = getString(R.string.pending)
                            binding.previousRequestGroup.isVisible = true

                        }
                        else Toast.makeText(requireContext() , "Unable to send a cash out request" , Toast.LENGTH_LONG).show()

                    }
                }
            }
            else {
                if(amount.isEmpty()) binding.cashOutAmountEditText.error = getString(R.string.enter_amount)
                if(selectedCashoutMethod.isNullOrEmpty()) binding.cashOutMethodEditText.error = getString(R.string.select_cash_out_method)
                if(phoneNumber.isEmpty()) binding.cashOutPhoneNumberEditText.error = getString(R.string.enter_phone)
                Toast.makeText(requireContext() , getString(R.string.fill_form_correctly) , Toast.LENGTH_LONG).show()
            }
        }

        userViewModel.getError().observe(viewLifecycleOwner){}
        userViewModel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                it?.let {
                    when{
                        it.startsWith(Error.CASHOUT_DUPLICATE_REQUEST_ERROR) -> Toast.makeText(requireContext() , Error.CASHOUT_DUPLICATE_REQUEST_ERROR , Toast.LENGTH_LONG).show()
                        it.startsWith(Error.CASHOUT_REQUEST_ELIGABLITY_ERROR) -> Toast.makeText(requireContext() , Error.CASHOUT_REQUEST_ELIGABLITY_ERROR , Toast.LENGTH_LONG).show()
                        it.startsWith(Error.CASHOUT_REQUEST_ERROR_50_BIRR_MUST_BE_LEFT) -> Toast.makeText(requireContext() , Error.CASHOUT_REQUEST_ERROR_50_BIRR_MUST_BE_LEFT , Toast.LENGTH_LONG).show()
                        it.startsWith(Error.CASHOUT__REQUEST_AMOUNT_0_ERROR) -> Toast.makeText(requireContext() , Error.CASHOUT__REQUEST_AMOUNT_0_ERROR , Toast.LENGTH_LONG).show()
                        it.startsWith(Error.CASHOUT__REQUEST_INSUFFIIENT_BALANCE) -> Toast.makeText(requireContext() , Error.CASHOUT__REQUEST_INSUFFIIENT_BALANCE , Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(requireContext() , "$it" , Toast.LENGTH_LONG).show()
                    }
                }
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


}