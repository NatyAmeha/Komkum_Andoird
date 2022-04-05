package com.komkum.komkum.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.size
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2

import com.komkum.komkum.R
import com.komkum.komkum.util.extensions.toControllerActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.komkum.komkum.MainActivity
import com.komkum.komkum.databinding.FragmentMainOnboardingBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.sendIntent


class MainOnboardingFragment : Fragment() {

    lateinit var binding : FragmentMainOnboardingBinding
    var currentPage = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainOnboardingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pref = PreferenceHelper.getInstance(requireContext())
        var onboardingList = listOf<OnboardingModel>(
            OnboardingModel(getString(R.string.buy_more_spend_less) , getString(R.string.buy_more_spend_less_description) , R.drawable.referral_image),
            OnboardingModel(getString(R.string.onboarding_commission) , getString(R.string.onboarding_commission_description) , R.drawable.loyality_image),
            OnboardingModel(getString(R.string.convenient_payment_options) , getString(R.string.onboarding_cash_on_delivery_description) , R.drawable.cash_on_delivery),
            OnboardingModel(getString(R.string.fun_shopping_experience) , getString(R.string.onboarding_game_description) , R.drawable.game_reward_image))

        var adapter = OnboardingViewpagerAdapter(onboardingList)
        binding.onboardingViewpager.adapter = adapter
        binding.onboardingViewpager.offscreenPageLimit = 3
        TabLayoutMediator(binding.tabLayout , binding.onboardingViewpager){tab, position -> }.attach()

        binding.onboardingViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                if(currentPage == onboardingList.size -1) binding.onboardingNextBtn.text = getString(R.string.continue_text)
            }
        })

        binding.onboardingNextBtn.setOnClickListener {
            if(currentPage < onboardingList.size -1) binding.onboardingViewpager.currentItem = currentPage+1
            else {
                // check if user visit for the first time
                pref[PreferenceHelper.FIRST_TIME_USER] = false
                pref[PreferenceHelper.FIRST_TIME_USER_FROM_LINK] = false
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            pref[PreferenceHelper.FIRST_TIME_USER] = false
            pref[PreferenceHelper.FIRST_TIME_USER_FROM_LINK] = false
            requireActivity().finish()
        }
    }
}


