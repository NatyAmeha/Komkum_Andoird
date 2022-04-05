package com.komkum.komkum.ui.store.team

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.data.model.TeamMember
import com.komkum.komkum.databinding.FragmentGameTeamHomeBinding
import com.komkum.komkum.databinding.FragmentGameTeamLederboardBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.extensions.showCustomDialog
import com.komkum.komkum.util.extensions.showShareMenu
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class GameTeamLederboardFragment : Fragment() , IRecyclerViewInteractionListener<TeamMember> {

    lateinit var binding : FragmentGameTeamLederboardBinding

    val teamViewmodel : TeamViewModel by viewModels()

    var teamInfo : Team<Product>? = null
    var teamLeaderboard : List<TeamMember>? = emptyList()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            teamInfo = it.getParcelable("TEAM_INFO")
        }
        binding = FragmentGameTeamLederboardBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var teamMemberInfo = RecyclerViewHelper(type = "GAME_TEAM_MEMBERS" , interactionListener = this)
        binding.teamMemberInfo = teamMemberInfo
        binding.totalProductPrice = 0
        teamInfo?.let { teamInf ->

            var pref = PreferenceHelper.getInstance(requireContext())
            var userId = pref.get(AccountState.USER_ID , "")

            var currentDate = Calendar.getInstance().time
            teamLeaderboard = teamInf.members?.sortedByDescending { member -> member.result?.plus(member.reward?.size ?: 0) }
            binding.leaderboard = teamLeaderboard?.take(teamInf.ad?.discountFor ?: 15)?.
            toMutableList()?.filterIndexed {index, _ ->  index >2}

            if(!teamLeaderboard.isNullOrEmpty()){
                if(teamLeaderboard!!.isNotEmpty()){
                    binding.winner1Avatarview.displayAvatar(teamLeaderboard?.get(0)?.image , teamLeaderboard!![0].username?.first().toString() , 24f , 80)
                    binding.firstWinnerNameTextview.text = teamLeaderboard?.get(0)?.username
                    binding.firstWinnerPointTextview.text = "${(teamLeaderboard?.get(0)?.reward?.size ?: 0)
                        .plus(teamLeaderboard?.get(0)?.result ?: 0)} ${getString(R.string.point)}"
                }
                if(teamLeaderboard!!.size >= 2){
                    binding.winner2Avatarview.displayAvatar(teamLeaderboard?.get(1)?.image , teamLeaderboard!![1].username?.first().toString() , 20f , 60 , R.drawable.circle_2)
                    binding.secondWinnerNameTextview.text = teamLeaderboard?.get(1)?.username
                    binding.secondWinnerPointTextview.text = "${(teamLeaderboard?.get(1)?.reward?.size ?: 0)
                        .plus(teamLeaderboard?.get(1)?.result ?: 0)}  ${getString(R.string.point)}"
                }
                if(teamLeaderboard!!.size >= 3){
                    binding.thirdWinnerNameTextview.text = teamLeaderboard?.get(2)?.username
                    binding.thirdWinnerPointTextview.text = "${(teamLeaderboard?.get(2)?.reward?.size ?: 0)
                        .plus(teamLeaderboard?.get(2)?.result ?: 0)}  ${getString(R.string.point)}"
                    binding.winner3Avatarview.displayAvatar(teamLeaderboard?.get(2)?.image , teamLeaderboard!![2].username?.first().toString() , 20f , 60 , R.drawable.circle_3)
                }
            }


            var currentUserInfo = teamInf.members?.find{member -> member.user == userId && member.joined == true}
            // when game expired (the time to play the game already passed)
            if(teamInf.endDate != null && teamInf.endDate!! < currentDate){
                if(!teamLeaderboard.isNullOrEmpty() && currentUserInfo != null && currentUserInfo.played == true){
                    var index = teamLeaderboard!!.indexOf(currentUserInfo)
                    if(index >= 0){
                        binding.userRankGroup.isVisible = true
                        binding.userRankAvatarview.displayAvatar(null , "${index +1}" , 16f , 80 , R.drawable.circle)
                        binding.userPointTextview.text = "${(currentUserInfo.reward?.size ?: 0).plus(currentUserInfo.result ?: 0)} point"

                        // for top players and top 3 winners
                        if(index < teamInf.ad?.discountFor ?: 10){
                            teamInf.ad?._id?.let {
//                                binding.teamLeaderboardProgressbar.isVisible = true
//                                teamViewmodel.getDiscountCode(teamInf._id!! , it).observe(viewLifecycleOwner){
//                                    binding.teamLeaderboardProgressbar.isVisible = false
//                                    it?.let { code ->
//                                        discountcode = code.value
//                                    }
//                                }
                            }
//                            when(index){
//                                0 -> {
//                                    prize = ((teamInf.ad?.prize ?: 0).times(55)).div(100)
//                                    binding.gameHeaderTextview.text = getString(R.string.game_winner)
//                                }
//                                1 -> {
//                                    prize = ((teamInf.ad?.prize ?: 0).times(30)).div(100)
//                                    binding.gameHeaderTextview.text = getString(R.string.game_winner)
//                                }
//                                2 -> {
//                                    prize = ((teamInf.ad?.prize ?: 0).times(15)).div(100)
//                                    binding.gameHeaderTextview.text = getString(R.string.game_winner)
//                                }
//                                else -> {}
//                            }.also {
//                                if((index in 0..2 && currentUserInfo.rewarded == true)){
//                                    prize = 0
//                                    isWinnerRewarded = true
//                                }
//                            }

//                            binding.gameHeaderBtn.setOnClickListener {
//                                showRewardDialog(teamInf)
//                            }
                        }
                    }
                }
            }
            // when game is active and user can play the game
            else if(teamInf.active && teamInf.endDate != null && teamInf.endDate!! >= currentDate){
//                binding.rewardDisplayCardview.isVisible = currentUserInfo != null
//                binding.gameHeaderTextview.text = getString(R.string.collect_more_point)
//                binding.gameHeaderBtn.text = "${getString(R.string.invite_people)}"
//                binding.gameHeaderBtn.setOnClickListener {
//                    if(userId.isNotBlank()) teamInf.ad?.prize?.let { it1 -> share(teamInf.ad!!._id!! , it1, userId) }
//                }

                if(!teamLeaderboard.isNullOrEmpty() && currentUserInfo != null && teamInf.endDate!! >= currentDate){
                    var index = teamLeaderboard!!.indexOf(currentUserInfo)
                    if(index > -1){
                        binding.userRankGroup.isVisible = true
                        binding.userRankAvatarview.displayAvatar(null , "${index +1}" , 16f , 80)
                        binding.userPointTextview.text = "${(currentUserInfo.reward?.size ?: 0).plus(currentUserInfo.result ?: 0)} point"
                    }
                }
            }
            // when game is not active
            else{
                binding.winner1Avatarview.displayAvatar(null , "?" , 24f , 80)
                binding.winner2Avatarview.displayAvatar(null , "?" , 20f , 60 , R.drawable.circle_2)
                binding.winner3Avatarview.displayAvatar(null, "?" , 20f , 60 , R.drawable.circle_3)

                if(currentUserInfo != null){
//                    binding.rewardDisplayCardview.isVisible = true
//                    binding.gameHeaderTextview.text = getString(R.string.invite_friend_to_game)
//                    binding.gameHeaderBtn.text = "${getString(R.string.invite_people)}"
//                    binding.gameHeaderBtn.setOnClickListener {
//                        if(userId.isNotBlank()) teamInf.ad?.prize?.let { it1 -> share(teamInf.ad!!._id!! , it1, userId) }
//                    }
                }
            }
        }
    }

    override fun onItemClick(data: TeamMember, position: Int, option: Int?) {}
    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}



    private fun share(teamId : String , prize : Int  , userId: String) {
        var invitationLink = "https://komkum.com/game/team/$teamId?uid=$userId"
        var body = "Who wants to Win upto ETB $prize. play this simple game, invite your friends and take your prize\n " +
                "$invitationLink"
        requireActivity().showShareMenu(teamInfo?.ad?.subtitle ?:"Play with friends", body)
    }
}