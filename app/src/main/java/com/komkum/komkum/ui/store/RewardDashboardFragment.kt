package com.komkum.komkum.ui.store


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Coupon
import com.komkum.komkum.databinding.FragmentRewardDashboardBinding
import com.komkum.komkum.ui.component.rewardCard
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.ui.user.UserViewModel
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardDashboardFragment : Fragment() {

    lateinit var binding : FragmentRewardDashboardBinding

    val userViewmodel : UserViewModel by viewModels()

//    val mainactivityviewmodel : MainActivityViewmodel by activityViewModels()

    var totalCommissionAmount : Double = 0.0
    var totalGamePrizeAmount : Double = 0.0

    init {
        lifecycleScope.launchWhenCreated {
            userViewmodel.getTotalGameReward()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRewardDashboardBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.rewards))

        binding.rewardComposeview.setContent {
            ZomaTunesTheme(true) {
               rewardListSection()
            }
        }

//        mainactivityviewmodel.getCommission().observe(viewLifecycleOwner){
//            it?.let {
//                totalCommissionAmount = it
//            }
//        }

//        mainactivityviewmodel.allRewards.observe(viewLifecycleOwner){
//            binding.rewardLoadingProgressbar.isVisible = false
//        }


        userViewmodel.getError().observe(viewLifecycleOwner){}
        userViewmodel.error.observe(viewLifecycleOwner){
            binding.rewardLoadingProgressbar.isVisible = false
            it?.handleError(requireContext() , {userViewmodel.removeOldErrors()}){
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }

        }

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

    @Composable
    fun rewardListSection(){
        val rewards by userViewmodel.allRewards.observeAsState()
        var colors = listOf(Color.Gray , Color.Green , Color.Magenta , Color.Cyan , Color.Blue)
        rewards?.let {
            binding.rewardLoadingProgressbar.isVisible = false
            if (it.isNotEmpty()) {
                LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)) {
                    itemsIndexed(rewards!!) { index, reward ->
                        rewardCard(reward, colors[index % colors.size]) { rewardInfo ->
                            if (rewardInfo.type == Coupon.REWARD_TYPE_GAME) rewardInfo.team?.let {
                                (requireActivity() as MainActivity).moveToGameTeamDetails(
                                    it,
                                    backstackId = R.id.rewardDashboardFragment
                                )
                            }
                            else if ((rewardInfo.type == Coupon.REWARD_TYPE_COMMISSION)) rewardInfo.team?.let {
                                (requireActivity() as MainActivity).movetoTeamDetails(it, backstackId = R.id.rewardDashboardFragment)
                            }
                        }
                    }
                }
            } else {
                binding.noRewardTextview.isVisible = true
            }

        }
    }
}