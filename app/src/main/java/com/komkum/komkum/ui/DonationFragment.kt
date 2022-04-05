package com.komkum.komkum.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Donation
import com.komkum.komkum.data.model.Transaction
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.databinding.FragmentDonationBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.user.UserViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.showCustomDialog
import com.komkum.komkum.util.viewhelper.CircleTransformation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DonationFragment : Fragment() {

    lateinit var binding : FragmentDonationBinding
    val userViewmodel : UserViewModel by viewModels()
    val mainactivityViewmodel : MainActivityViewmodel by activityViewModels()

    var donationType : Int = -1
    var receiverId : String? = null
    var receiverName : String? = null
    var creatorId : String? = null
    var creatorImage : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            donationType = it.getInt("DONATION_TYPE")
            receiverId = it.getString("RECEIVER_ID")
            receiverName = it.getString("RECEIVER_NAME")
            creatorImage  = it.getString("CREATOR_IMAGE")
            creatorId = it.getString("CREATOR_ID")
        }
        binding = FragmentDonationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "${getString(R.string.donation_for)} $receiverName")

        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        pref[AccountState.IS_REDIRECTION] = true

        creatorId?.let {
            mainactivityViewmodel.getDonations(it).observe(viewLifecycleOwner) {
                binding.donationProgressbar.isVisible = false

                if (!it.isNullOrEmpty()) {
                    var currentUserDonation = it.filter { it.doner == userId }
                    if(!currentUserDonation.isNullOrEmpty()){
                        binding.yourDonationGroup.isVisible = true
                        binding.yourDonationAmountTextview.text = "${getString(R.string.birr)} ${currentUserDonation.sumOf { it.amount ?: 0 }}"
                        var allDonations= mutableMapOf<String? , Int>()
                        it.groupBy { it.doner }.forEach{ (key , value) ->
                            var totaldonationAmount = value.sumOf { it.amount ?: 0 }
                            allDonations[key] = totaldonationAmount
                        }
                        var sorteddonations = allDonations.toList().sortedByDescending { (key , value) -> value}
                        var currentUserRank = sorteddonations.indexOfFirst { (key , value) -> key == userId } + 1
                        if(currentUserRank > 0){
                            binding.donationRankTextview.text = "$currentUserRank"
                        }
                    }

                    binding.memberListTextview.text =
                        "${it?.distinctBy { it.doner }.size} ${getString(R.string.people_donated_to)} "
                    var donations = it.distinctBy { it.doner }.sortedByDescending { it.amount }
                    var d = it.groupBy { it.doner }.toList().sortedByDescending { (key , value) ->
                         value.sumOf { it.amount ?: 0 }
                    }.toMap()
                    binding.number1DonerImageview.displayAvatar(
                        d[d.keys.toList().getOrNull(0)]?.getOrNull(0)?.donerImage ,
                        (d[d.keys.toList().getOrNull(0)]?.getOrNull(0)?.donerName ?: "?").first().toString(),
                        20f
                    )

                    binding.number2DonerImageview.displayAvatar(
                        d[d.keys.toList().getOrNull(1)]?.getOrNull(0)?.donerImage,
                        (d[d.keys.toList().getOrNull(1)]?.getOrNull(0)?.donerName ?: "?").first().toString(),
                        20f,
                        backgroundColor =  R.drawable.circle_2
                    )

                    binding.number3DonerImageview.displayAvatar(
                        d[d.keys.toList().getOrNull(2)]?.getOrNull(0)?.donerImage,
                        (d[d.keys.toList().getOrNull(2)]?.getOrNull(0)?.donerName?: "?").first().toString(),
                        20f,
                        backgroundColor = R.drawable.circle_3
                    )
                }
                else{
                    binding.memberListTextview.text = "${getString(R.string.first_supporter)} $receiverName"
                    binding.number1DonerImageview.displayAvatar(null, "?", 20f)
                    binding.number2DonerImageview.displayAvatar(null, "?", 20f, backgroundColor =  R.drawable.circle_2)
                    binding.number3DonerImageview.displayAvatar(null, "?", 20f, backgroundColor =  R.drawable.circle_3)

                }
            }


            binding.donationAmountEditText.doOnTextChanged { text, start, before, count ->
                if (!text.isNullOrBlank()) {
                    var newAmount = text.toString().toInt()
                    binding.makeDonationBtn.isVisible = newAmount >= 5
                } else binding.makeDonationBtn.isVisible = false

            }

            binding.donationChipGroup.setOnCheckedChangeListener { group, checkedId ->
                group.children.toList().firstOrNull { (it as Chip).isChecked }?.let {
                    var amount = (it as Chip).text.split(" ").first().toString().toInt()
                    binding.donationAmountEditText.setText(amount.toString())
                }
            }

            binding.makeDonationBtn.setOnClickListener {
                var amount = binding.donationAmountEditText.text.toString().toInt()
                receiverName?.let { handleDonationPayment(amount , it) }
            }

            userViewmodel.getError().observe(viewLifecycleOwner) {}
            userViewmodel.error.observe(viewLifecycleOwner) {
                binding.donationProgressbar.isVisible = false
                it?.handleError(requireContext() , { mainactivityViewmodel.removeOldErrors() } ,
                    mainactivityViewmodel.signUpSource){
                    (requireActivity() as MainActivity)
                        .showErrorSnacbar("${getString(R.string.error_message)}.\n${it}" , "Dismiss"){}
                }
            }

            mainactivityViewmodel.getError().observe(viewLifecycleOwner){}
            mainactivityViewmodel.error.observe(viewLifecycleOwner){
                binding.donationProgressbar.isVisible = false
                it?.handleError(requireContext() ,  { mainactivityViewmodel.removeOldErrors() },
                    mainactivityViewmodel.signUpSource){}

            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }



    fun handleDonationPayment(amount : Int , receiverName : String){
        var pref = PreferenceHelper.getInstance(requireContext())
        var donerId = pref.get(AccountState.USER_ID , "")
        var donerName = pref.get(AccountState.USERNAME , "")
        var donerImage = pref.get(AccountState.PROFILE_IMAGE , "")

        binding.donationProgressbar.isVisible = true
        mainactivityViewmodel.getWalletBalance().observe(viewLifecycleOwner){
            binding.donationProgressbar.isVisible = false
            var paymentmethods = listOf(
                PaymentMethod(getString(R.string.wallet), "${getString(R.string.balance)} - ${getString(R.string.birr)} $it" , R.drawable.ic_baseline_account_balance_wallet_24),
            )
            mainactivityViewmodel.showPaymentMethodsDialog(requireContext() , paymentmethods, amount.toDouble() , viewLifecycleOwner){
                when(it){
                    0->{
                        binding.donationProgressbar.isVisible = true
                        var donationInfo = Donation(null , amount , donerId , donerName , donerImage , receiverId , receiverName , creatorId , donationType , creatorImage)
                        userViewmodel.makeDonation(donationInfo).observe(viewLifecycleOwner){
                            binding.donationProgressbar.isVisible = false
                            if(it == true){
                                (requireActivity() as MainActivity).showSnacbar("Successfully Donated to $receiverName")
                                findNavController().navigateUp()
                            }
                            else Toast.makeText(requireContext() ,  "Error occured" , Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun makeDonation(amount : Int , receiverName : String , onSuccess : () -> Unit){
        var pref = PreferenceHelper.getInstance(requireContext())
        var donerId = pref.get(AccountState.USER_ID , "")
        var donerName = pref.get(AccountState.USERNAME , "")
        var donerImage = pref.get(AccountState.PROFILE_IMAGE , "")

        if(donerId.isNotBlank() && receiverId != null){
            requireActivity().showCustomDialog(
                title = "Donate ETB $amount",
                message = "Make ETB $amount donation to $receiverName",
                pos_action_String = "Donate",
                neg_action_String = "Cancel",
                negetiveAction = {binding.donationProgressbar.isVisible = false}
            ){
                binding.donationProgressbar.isVisible = true
                var donationInfo = Donation(null , amount , donerId , donerName , donerImage , receiverId , receiverName , creatorId , donationType , creatorImage)
                userViewmodel.makeDonation(donationInfo).observe(viewLifecycleOwner){
                    binding.donationProgressbar.isVisible = false
                    if(it == true) onSuccess()
                    else Toast.makeText(requireContext() ,  "Error occured" , Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}