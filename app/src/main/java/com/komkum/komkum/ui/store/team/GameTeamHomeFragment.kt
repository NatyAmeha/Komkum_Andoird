package com.komkum.komkum.ui.store.team

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.databinding.FragmentGameTeamHomeBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.store.ProductViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.getRemainingDay
import com.komkum.komkum.util.extensions.showShareMenu
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt

@AndroidEntryPoint
class GameTeamHomeFragment : Fragment() {
    lateinit var binding : FragmentGameTeamHomeBinding

    var teamInfo : Team<Product>? = null

    val productViewmodel : ProductViewModel by viewModels()

    lateinit var timer : Timer


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            teamInfo = it.getParcelable("TEAM_INFO")
        }
        binding = FragmentGameTeamHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        teamInfo?.let {team ->
            displayUI(team)
            team.ad?.adContent?.let{
                productViewmodel.getSmallProductInfo(it).observe(viewLifecycleOwner){
                    it?.let { productInfo ->
                        binding.discountTextview.text= "${team.ad?.discount}% ${getString(R.string.discount_for_top)} ${team.ad?.discountFor} ${getString(R.string.players)}"
                        binding.discountTextview.isVisible = true
                        binding.discountDescriptionTextview.isVisible = true
                        binding.discountDescriptionTextview.text = "${getString(R.string.top)} ${team.ad?.discountFor} ${getString(R.string.game_discount_prize_description)}"

                        var imageList = productInfo.gallery?.filter { gallery -> gallery.type == "image" && !gallery.path.isNullOrBlank()}
                        Picasso.get().load(imageList?.firstOrNull()?.path).fit().centerCrop().into(binding.sponsoredProduct.pImageview)
                        binding.sponsoredProduct.pTitleTextview.text = productInfo.title
                        binding.sponsoredProduct.pSubtitleTextview.text = "${getString(R.string.birr)} ${productInfo?.dscPrice?.roundToInt()}"
                        if(productInfo.refund == true) binding.sponsoredProduct.returnEligableTextview.isVisible = true

                        binding.sponsoredProduct.root.setOnClickListener {
                            var pref = PreferenceHelper.getInstance(requireContext())
                            pref[PreferenceHelper.PURCHASED_FROM] = FcmService.F_EVENT_PARAM_VALUE_PURCHASED_FROM_GAME
                            (requireActivity() as MainActivity).movetoProductDetails(productInfo._id!!)
                        }
                    }
                }
            }
        }
    }

    fun displayUI(teamInfo : Team<Product>){
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")

        binding.totalPrizeTextview.text = "${getString(R.string.birr)} ${teamInfo.ad?.prize}"
        if(teamInfo.ad?.prize != null){
            Picasso.get().load(R.drawable.gameimage)
                .fit().centerCrop().rotate(45f).transform(RoundImageTransformation()).into(binding.gameImageview)
            var firstPrize = ((teamInfo.ad!!.prize!!.times(55)).div(100))
            var secondPrize = ((teamInfo.ad!!.prize!!.times(30)).div(100))
            var thirdPrize = ((teamInfo.ad!!.prize!!.times(15)).div(100))

            binding.firstPrizeTextview.text = "${getString(R.string.birr)} $firstPrize"
            binding.secondPrizeTextview.text = "${getString(R.string.birr)} $secondPrize"
            binding.thirdPrizeTextview.text = "${getString(R.string.birr)} $thirdPrize"

            binding.adOwnerNameTextview.text = teamInfo.ad?.ownerName?.joinToString(", ")
            Picasso.get().load(teamInfo.ad?.thumbnailPath).fit().centerCrop()
                .placeholder(R.drawable.sp_image).into(binding.adOwnerImageview)

            binding.requiredPlayerTextview.text = "${teamInfo.teamSize} ${getString(R.string.players)}"
            binding.currentPlayerTextview.text = "${teamInfo.members?.size} ${getString(R.string.players)}"

            var member = teamInfo.members?.find{member -> member.user == userId && member.played == true}
            if( member != null) binding.statusTextview.text = getString(R.string.played)
            else binding.statusTextview.text = getString(R.string.not_played)


            if(teamInfo.endDate != null){
                var calendar = Calendar.getInstance()
//                calendar.time = teamInfo.endDate
                if(calendar.time> teamInfo.endDate) binding.remainingDayTextview.text = "Game expired"
                else{
                    timer =  fixedRateTimer("gametimer" , false , period = 1000){
                        requireActivity().runOnUiThread {
                            binding.remainingDayTextview.text = teamInfo.endDate?.getRemainingDay(requireContext() , includeSec = true)
                        }
                    }
                }
            }
            else{
                binding.teamExpireTextView.isVisible = false
                binding.teamExpireTextView.isVisible = false
            }
        }
    }

    override fun onDetach() {
        if(::timer.isInitialized) timer.cancel()
        super.onDetach()
    }


    private fun share(teamId : String , prize : Int  , userId: String) {
        var invitationLink = "https://komkum.com/game/team/$teamId?uid=$userId"
        var body = "Who wants to Win upto ETB $prize. play this simple game, invite your friends and take your prize\n " +
                "$invitationLink"
        requireActivity().showShareMenu("Play with friends" , body)
    }


}