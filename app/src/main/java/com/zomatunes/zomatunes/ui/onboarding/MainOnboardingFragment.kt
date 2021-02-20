package com.zomatunes.zomatunes.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2

import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_main_onboarding.*

class MainOnboardingFragment : Fragment() {

    var currentPage = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var onboardingList = listOf<OnboardingModel>(
            OnboardingModel("All in one place" , "Enjoy your favorite Music, Movies, Books and Audiobook in one place" , R.drawable.fastimage),
            OnboardingModel("Pay less enjoy More" , "Access entire library of Music, Movies and Books with low price" , R.drawable.easypayment),
            OnboardingModel("Join from anywhere" , "You can use local and international payment vendors" , R.drawable.paypallogo),
            OnboardingModel("Tune your Experiance" , "Adjust your listening and reading experience according to your mood and preference" , R.drawable.securepayment))

        var adapter = OnboardingViewpagerAdapter(onboardingList)
        onboarding_viewpager.adapter = adapter
        TabLayoutMediator(tabLayout , onboarding_viewpager){tab, position -> }.attach()

        onboarding_viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                if(currentPage == onboardingList.size -1) onboarding_next_btn.text = "Continue"
            }
        })

        onboarding_next_btn.setOnClickListener {
            if(currentPage < onboardingList.size -1) onboarding_viewpager.currentItem = currentPage+1
            else {
                this.toControllerActivity().updateToHalfRegisteredAccount(view.context)
                findNavController().navigate(R.id.action_mainOnboardingFragment_to_mainAccountFragment)
            }

        }
    }
}


