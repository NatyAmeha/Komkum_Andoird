package com.komkum.komkum.ui.store.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.data.model.Order
import com.komkum.komkum.databinding.FragmentOrderCompleteBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.store.team.TeamViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.createdynamicLink
import com.komkum.komkum.util.extensions.isFragmentInBackStack
import com.komkum.komkum.util.extensions.showShareMenu
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class OrderCompleteFragment : Fragment() {

    lateinit var binding : FragmentOrderCompleteBinding

    var orderInfo : Order<String, Address>? = null
    val teamViewmodel : TeamViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            orderInfo = it.getParcelable("ORDER")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      binding = FragmentOrderCompleteBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                var isInBackstack = findNavController().isFragmentInBackStack(R.id.storeHomepageFragment)
                if(isInBackstack) findNavController().popBackStack(R.id.storeHomepageFragment , false)
                else findNavController().popBackStack(R.id.teamFragment , false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")

        binding.orderCodeTextview.text = "${getString(R.string.order_code)}   ${orderInfo?.code}"
        binding.goToOrderBtn.setOnClickListener {
           orderInfo?.let{ (requireActivity() as MainActivity).movetoOrderDetailsFragment(it._id!! , OrderDetailFragment.LOAD_FROM_API , popToStart = true)}
        }

        binding.invitePeopleDescriptionTextview.setOnClickListener {
            if(binding.invitePeopleDescriptionTextview.maxLines ==5)
                binding.invitePeopleDescriptionTextview.maxLines = 1000
            else binding.invitePeopleDescriptionTextview.maxLines = 5
        }

        binding.shareYourTeamBtn.setOnClickListener {
            orderInfo?.let {
                var teamId = it.items?.map { it.team }?.filterNotNull()?.firstOrNull()
                var userId = PreferenceHelper.getInstance(requireContext()).get(AccountState.USER_ID , "")
                if(teamId != null && userId.isNotBlank()){
                    binding.progressBar10.isVisible = true
                    teamViewmodel.getTeamDetailsLiveData(teamId).observe(viewLifecycleOwner){team->
                        team?.let { teamInfo ->
                            var stdProductPrices = teamInfo.products!!
                                .sumOf { product -> product.stdPrice!!.times(product.minQty.plus(teamInfo.additionalQty ?: 0)) }

                            var discountPRices = teamInfo.products!!
                                .sumOf { product -> product.dscPrice!!.times(product.minQty.plus(teamInfo.additionalQty ?: 0)) }
                            var discountedPercent = ((1 - (discountPRices / stdProductPrices))*100).roundToInt()
                            var discountedAmount = stdProductPrices.roundToInt() - discountPRices.roundToInt()

                            var totalProductPrice = teamInfo.products?.sumOf { product -> (product.dscPrice ?: 0.0).times(product.minQty) }
                            var commission = (totalProductPrice?.times(5))?.div(100)?.roundToInt().toString()

                            var titleinAmharic = if(teamInfo.products!!.size > 1) "${teamInfo.products!![0].title} አና ሌሎች ${teamInfo.products!!.size -1} ምርቶችን "
                            else "${teamInfo.products!![0].title} "

                            var titleinEnglish = if(teamInfo.products!!.size > 1) "${teamInfo.products!![0].title} and other ${teamInfo.products!!.size -1} products "
                            else "${teamInfo.products!![0].title }"

                            var body = "በዚህ ቡድን ውስጥ ያሉ ምርቶችን እንዲገዙ ሌሎችን በመጋበዝ በእያንዳንዱ ግብዣ 5% ($commission ብር) ኮሚሽን ያግኙ።"
                            var invitationLink = "https://komkum.com/team/${teamId}/invitation?uid=$userId"

                            this.createdynamicLink(invitationLink , "ትልቅ ቅናሽ" ,
                                "${titleinAmharic} ከቤተሰቦ ፣ ከጓድኛዎ አና ከስራ ባልድረባዎ ጋር በኮምኮም መተግበሪ ላይ ሲገዙ ${discountedPercent}% (${discountedAmount} ብር) ቅናሽ ያገኛሉ።",
                                image = teamInfo.products!!.firstOrNull()?.gallery?.firstOrNull{gallery -> gallery.type == "image" }?.path ?: "",
                                onFailure = {
                                    binding.progressBar10.isVisible = false
                                    var linktoShare = "\n\nclick here $invitationLink"
                                    var linkDesc = "\nBuy $titleinEnglish with unbelievable discount and get 5% commission by inviting others to buy the products.\n"
                                    var data = body.plus(linkDesc).plus(linktoShare)
                                    requireActivity().showShareMenu("Buy with team" , data)
                                }){dynamicLink ->
                                binding.progressBar10.isVisible = false
                                var linktoShare = "\n\nclick here $dynamicLink"
                                var data = body.plus(linktoShare)
                                requireActivity().showShareMenu("Buy with team" , data)
                            }

                            mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.TEAM_LINK_SHARE , null)
                        }

                    }

                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            var isInBackstack = findNavController().isFragmentInBackStack(R.id.storeHomepageFragment)
            if(isInBackstack) findNavController().popBackStack(R.id.storeHomepageFragment , false)
            else findNavController().popBackStack(R.id.teamFragment , false)
        }
    }
}