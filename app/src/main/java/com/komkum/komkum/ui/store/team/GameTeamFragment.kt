package com.komkum.komkum.ui.store.team

import android.Manifest
import android.content.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.facebook.login.LoginManager

import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.ktx.logEvent
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.data.model.TeamMember
import com.komkum.komkum.databinding.FragmentGameTeamBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.adaper.GameTeamViewpagerAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class GameTeamFragment : Fragment() {

    lateinit var binding : FragmentGameTeamBinding
    val teamViewmodel : TeamViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var adId : String? = null
    var inviterId : String? = null
    var pageIndex : Int = 0
    var currentQuestion = 0

    var canPlay = true

    var backstackId : Int? = null

    var showAsMainError = true
    var teamLeaderboard : List<TeamMember>? = emptyList()
    var prize : Int = -1  //
    var discountcode : String? = null  // it will has a value when user is in top player list
    var isUserDiscountcodeOwner = false
    var isWinnerRewarded = false    // value will be true if the prize already transfered to his wallet


    lateinit var pref : SharedPreferences

    init {
        lifecycleScope.launchWhenCreated {
            pref = PreferenceHelper.getInstance(requireActivity())
            pref[AccountState.IS_REDIRECTION] = true
            arguments?.let {
                adId = it.getString("AD_ID")
                inviterId = it.getString("INVITER_ID")
                pageIndex = it.getInt("PAGE_INDEX")
                backstackId = it.getInt("BACKSTACK_ID")

            }
            adId?.let { id ->
                requireActivity().requestAndAccessLocationBeta(onFailure = { teamViewmodel.getGameTeamDetails(id) }) {location ->
                    teamViewmodel.getGameTeamDetails(id , location.longitude , location.latitude)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGameTeamBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var userId = pref.get(AccountState.USER_ID , "")
        var currentDate = Calendar.getInstance().time

        teamViewmodel.teamDetails.observe(viewLifecycleOwner){
            binding.gameTeamProgressbar.isVisible = false
            binding.tJoinBtn.isVisible = true
            it?.let {teamInf ->
                canPlay = if(it.members.isNullOrEmpty()) false
                else it.members!!.size >= it.teamSize ?:15

                var gameIdentifier = teamInf._id!! + userId
                var viewpagerAdapter = GameTeamViewpagerAdapter(requireActivity() , teamInf)
                binding.gameTeamViewpager.adapter = viewpagerAdapter
                TabLayoutMediator(binding.teamTabLayout , binding.gameTeamViewpager){tab , position ->
                    when(position){
                        0 -> tab.text = getString(R.string.overview)
                        1 -> tab.text = getString(R.string.leaderboard)
                    }
                }.attach()
                binding.gameTeamViewpager.currentItem = pageIndex

                teamLeaderboard = teamInf.members?.sortedByDescending { member -> member.result?.plus(member.reward?.size ?: 0) }

                var currentUserInfo = teamInf.members?.find{member -> member.user == userId && member.joined == true}
                // when game expired (the time to play the game already expired)
                if(teamInf.endDate != null && teamInf.endDate!! < currentDate){
                    if(!teamLeaderboard.isNullOrEmpty() && currentUserInfo != null && currentUserInfo.played == true){
                        var index = teamLeaderboard!!.indexOf(currentUserInfo)
                        // for top players and top 3 winners
                        if(index >= 0 && index < teamInf.ad?.discountFor ?: 10){
                            binding.rewardDisplayCardview.isVisible = true
                            binding.gameHeaderTextview.text = "${getString(R.string.top_player)} ${teamInf.ad?.discountFor} ${getString(R.string.winner_players)} ${getString(R.string.in_top_player)}"
                            teamInf.ad?._id?.let {
                                binding.gameTeamProgressbar.isVisible = true
                                teamViewmodel.getDiscountCode(teamInf._id!! , it).observe(viewLifecycleOwner){
                                    binding.gameTeamProgressbar.isVisible = false
                                    it?.let { code ->
                                        discountcode = code.value
                                        isUserDiscountcodeOwner = code.owner == userId
                                    }
                                }
                            }
                            when(index){
                                0 -> {
                                    prize = ((teamInf.ad?.prize ?: 0).times(55)).div(100)
                                    binding.gameHeaderTextview.text = getString(R.string.game_winner)
                                }
                                1 -> {
                                    prize = ((teamInf.ad?.prize ?: 0).times(30)).div(100)
                                    binding.gameHeaderTextview.text = getString(R.string.game_winner)
                                }
                                2 -> {
                                    prize = ((teamInf.ad?.prize ?: 0).times(15)).div(100)
                                    binding.gameHeaderTextview.text = getString(R.string.game_winner)
                                }
                                else -> {}
                            }.also {
                                if((index in 0..2 && currentUserInfo.rewarded == true)){
                                    prize = 0
                                    isWinnerRewarded = true
                                }
                            }

                            binding.gameHeaderBtn.setOnClickListener {
                                showRewardDialog(teamInf , index)
                            }
                        }
                    }
                }
                // when game is active and user can play the game
                else if(teamInf.active && teamInf.endDate != null && teamInf.endDate!! >= currentDate){
                    binding.rewardDisplayCardview.isVisible = currentUserInfo != null
                    binding.gameHeaderTextview.text = getString(R.string.collect_more_point)
                    binding.gameHeaderBtn.text = "${getString(R.string.invite_people)}"
                    binding.gameHeaderBtn.setOnClickListener {
                        if(userId.isNotBlank()) teamInf.ad?.prize?.let { it1 -> share(teamInf.ad!!.subtitle!! ,  teamInf.ad!!._id!! , it1, userId) }
                    }

                    var result = pref.get(PreferenceHelper.FIRST_TIME_FOR_GAME_INSTRUCTION , true)
                    if(result){
                        pref[PreferenceHelper.FIRST_TIME_FOR_GAME_INSTRUCTION] = false
                        requireContext().showCustomDialog(getString(R.string.game_instruction),
                            getString(R.string.game_instruction_details) ,
                            null , getString(R.string.continue_text) , getString(R.string.cancel) ,
                            view = R.layout.game_instruction_dialog , bottomSheet = true , layoutMode = LayoutMode.MATCH_PARENT ,negetiveAction =  {}){}
                    }

//                    if(!teamLeaderboard.isNullOrEmpty() && currentUserInfo != null && teamInf.endDate!! >= currentDate){
//                        var index = teamLeaderboard!!.indexOf(currentUserInfo)
//                        if(index > -1){
//                            binding.userRankGroup.isVisible = true
//                            binding.userRankAvatarview.displayAvatar(null , "${index +1}" , 16f , 80)
//                            binding.userPointTextview.text = "${(currentUserInfo.reward?.size ?: 0).plus(currentUserInfo.result ?: 0)} point"
//                        }
//                    }
                }
                // when game is not active
                else{
                    binding.rewardDisplayCardview.isVisible = true
                    binding.gameHeaderBtn.isVisible = false
                    binding.gameHeaderTextview.text = "Waiting ${teamInf?.teamSize?.minus(teamInf?.members?.size ?: 0)} people to join the game"

                    if(currentUserInfo != null){
                        binding.rewardDisplayCardview.isVisible = true
                        binding.gameHeaderTextview.text = getString(R.string.invite_friend_to_game)
                        binding.gameHeaderBtn.text = "${getString(R.string.invite_people)}"
                        binding.gameHeaderBtn.setOnClickListener {
                            if(userId.isNotBlank()) teamInf.ad?.prize?.let { it1 -> share(teamInf.ad!!.subtitle!! , teamInf.ad!!._id!! , it1, userId) }
                        }
                    }
                }




                var member = teamInf.members?.find{member -> member.user == userId && member.joined == true}
                if( member != null){
                    currentQuestion = member.answers?.size ?: 0
                    binding.tJoinBtn.isVisible = false
                    binding.startGameBtn.isVisible = true
                }
                else{
                    binding.tJoinBtn.isVisible = true
                    binding.startGameBtn.isVisible = false
                }

                if(teamInf.endDate != null){
                    if(currentDate > teamInf.endDate){
                        binding.startGameBtn.isVisible = false
                        binding.tJoinBtn.isVisible = false

                        if(member?.played == null || member.played == false) requireActivity().showDialog(getString(R.string.team_expired) , getString(R.string.game_cannot_be_played) , getString(
                                                    R.string.find_more_game) ,
                            negetiveAction = {findNavController().navigateUp()} ){
                            findNavController().navigate(R.id.gameListFragment)
                        }
                    }
                }



                //send game_visit_event to firebase
                mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.FIREBASEANALYTICS_EVENT_AD_CLICK){
                    param(FcmService.F_EVENT_PARAM_GAME_NAME , it.ad!!.subtitle!!)
                }

                binding.tJoinBtn.setOnClickListener {
                    showAsMainError = false
//                    Toast.makeText(requireContext() , inviterId , Toast.LENGTH_SHORT).show()
                    binding.gameTeamProgressbar.isVisible = true
                    teamViewmodel.joinTeam(teamInf._id!! , inviterId).observe(viewLifecycleOwner){
                        binding.gameTeamProgressbar.isVisible = false
                        showAsMainError = true
                        if(it == true) teamInf.ad?.let { ad ->
                            (requireActivity() as MainActivity).moveToGameTeamDetails(ad._id , backstackId = backstackId)
                        }
                    }
                }

                binding.startGameBtn.setOnClickListener {
                    if((!teamInf.active && teamInf.ad != null) || !canPlay) showGameNotActiveDialog(teamInf)
                    else{
                        teamInf.ad?.let {
                            var member = teamInf.members?.find{member -> member.user == userId && member.joined == true}
                            currentQuestion = member?.answers?.size ?: 0
                            var qIndex = pref.get(gameIdentifier , 0)

                            if(member?.played == true) currentQuestion = 1000
                            else if(qIndex > currentQuestion) currentQuestion = qIndex

//                            Toast.makeText(requireContext() , " $gameIdentifier $qIndex  $currentQuestion" , Toast.LENGTH_SHORT).show()
                            (requireActivity() as MainActivity).moveToAdDisplay(it , currentQuestion , teamInf._id!! , member?.result ?: 0 , member?.played)
                        }
                    }
                }
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if(backstackId == null) findNavController().navigateUp()
            else findNavController().popBackStack(backstackId!! , false)
        }

        teamViewmodel.getError().observe(viewLifecycleOwner){}
        teamViewmodel.error.observe(viewLifecycleOwner){
            binding.gameTeamProgressbar.isVisible = false
            it?.handleError(requireContext() , {teamViewmodel.removeOldErrors()},
                signupSource = mainActivityViewmodel.signUpSource){
                if(showAsMainError) binding.errorTextview.isVisible = showAsMainError
                else (requireActivity() as MainActivity)
                    .showErrorSnacbar("${getString(R.string.error_message)}.\n${it}" , "Dismiss"){}
            }

        }


    }


    fun showRewardDialog(teamInfo : Team<Product>, currentUserRank : Int){
        var dialog = MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT))
        dialog.cornerRadius(literalDp = 14f).customView(R.layout.game_reward_b_sheet_view , scrollable = true)
        var customview =  dialog.getCustomView()

        var transferREwardBtn = customview.findViewById<Button>(R.id.transfer_prize_btn)
        if(!isWinnerRewarded || !isUserDiscountcodeOwner){
            transferREwardBtn.isEnabled = true
        }
        else transferREwardBtn.text = "You already took your reward"

        transferREwardBtn.setOnClickListener {
            dialog.dismiss()

            requireActivity().showDialog("Info" , "The discount code will be yours and you can use it when you order the product associated with this game" , positiveButtonText = "Ok, Continue"){
                binding.gameTeamProgressbar.isVisible = true
                if( teamInfo.ad != null && !isWinnerRewarded && discountcode != null){
                    teamViewmodel.transferGamereward(prize!! , teamInfo.ad!!._id , teamInfo._id!! , discountcode!!).observe(viewLifecycleOwner){
                        binding.gameTeamProgressbar.isVisible = false
                        prize = 0
                        if(it == true) (requireActivity() as MainActivity).showSnacbar(getString(R.string.wallet_transfer_message) , "view"){
                            findNavController().navigate(R.id.transactionFragment)
                        }
                        else Toast.makeText(requireContext() , getString(R.string.wallet_transfer_error_message) , Toast.LENGTH_LONG).show()
                    }
                }
            }


        }
        var prizeTextView = customview.findViewById<TextView>(R.id.game_prize_textview)
        var prizeDescriptionTextview = customview.findViewById<TextView>(R.id.textView208)
        if(prize > 0 && !isWinnerRewarded)
            prizeTextView.text = "${getString(R.string.birr)} $prize"
        else if(currentUserRank < 0 || currentUserRank  > 2){
            prizeTextView.text = "0"
            prizeDescriptionTextview.text = getString(R.string.cash_prize_for_top_3_players)
        }
        else{
            prizeTextView.text = "0"
            prizeDescriptionTextview.text = getString(R.string.prize_already_transferred)
        }

        var discountCodeTextView = customview.findViewById<TextView>(R.id.discount_code_textview)
        discountCodeTextView.text = discountcode

//        var copyDiscountcodeIcon = customview.findViewById<ImageView>(R.id.copy_discount_code_imageview)
//        copyDiscountcodeIcon.isVisible = !discountCodeTextView.text.isNullOrEmpty()
//        copyDiscountcodeIcon.setOnClickListener {
//            var clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            var clipData = ClipData.newPlainText("Discount code" , discountcode)
//            clipboardManager.setPrimaryClip(clipData)
//            Toast.makeText(requireContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
//        }

        var productRecyclerview = customview.findViewById<RecyclerView>(R.id.game_product_recyclerview)
        dialog.show()
    }

    fun showGameNotActiveDialog(tInfo : Team<Product>){
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")

        var invitationLink = "https://komkum.com/game/team/${tInfo._id}?uid=$userId"
        var body = "Who wants to Win upto ETB ${tInfo.ad?.prize}. play this simple quiz, invite your friends and take your prize\n " +
                "$invitationLink"

        requireActivity().showDialog("Error" , "${tInfo.teamSize} ${getString(R.string.game_team_size_description)}" , getString(R.string.invite_people) ){
            requireActivity().showShareMenu("${tInfo.ad?.subtitle}" , body)
        }
    }

    private fun share(title : String , teamId : String , prize : Int  , userId: String) {
        var invitationLink = "https://komkum.com/game/team/$teamId?uid=$userId"
        var body = "Who wants to Win upto ETB $prize. play this simple game, invite your friends and take your prize\n " +
                "$invitationLink"
        requireActivity().showShareMenu("Play $title with friends", body)
    }

}
