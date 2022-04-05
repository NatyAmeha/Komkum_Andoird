package com.komkum.komkum.ui.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.User
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.FragmentRecommendationBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.onboarding.CategorySelectionAdapter
import com.komkum.komkum.ui.search.SearchViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.BrowseCategoryAdapter
import com.komkum.komkum.util.adaper.RecommendationViewpagerAdpter
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleListDataObservation
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendationFragment : Fragment() {

    lateinit var binding : FragmentRecommendationBinding
    val userViewmodel : UserViewModel by activityViewModels()

    var user : UserWithSubscription? = null

    init {
        lifecycleScope.launchWhenCreated {
            userViewmodel.getUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pref = PreferenceHelper.getInstance(requireContext())
        pref[AccountState.IS_REDIRECTION] = true
        userViewmodel.userData.observe(viewLifecycleOwner){usr ->
            user = usr
            userViewmodel.getBrowseLists().observe(viewLifecycleOwner){
                binding.progressBar3.isVisible = false
                binding.nextRecommendaationBtn.isEnabled = true

                it?.let {
                    var recommendationAdapter = RecommendationViewpagerAdpter(requireActivity() , user!! , it)
                    binding.recommendationViewpager.isUserInputEnabled = false
                    binding.recommendationViewpager.adapter = recommendationAdapter
                }
            }
        }

        binding.fetchRecommendationBtn.setOnClickListener {
            it.isVisible = false
            findNavController().navigate(R.id.recommendationFragment)
        }

        binding.nextRecommendaationBtn.setOnClickListener {
            if(binding.recommendationViewpager.currentItem < 1)
                binding.recommendationViewpager.setCurrentItem(binding.recommendationViewpager.currentItem + 1 , true)
            else{
                binding.progressBar3.isVisible = true
                it.isEnabled = false
//                var selectedMusicCategory = userViewmodel.musicCategoryStateManager.multiselectedItems.value
//                var selectedBookCategory = userViewmodel.bookdRecommendationStatemanager.multiselectedItems.value?.map { browse -> browse.name!! }
//                var selectedMusicGenre = userViewmodel.musicRecommendationStateManager.multiselectedItems.value?.map{browse -> browse.name!!}
//                user?.category = selectedMusicCategory
//                user?.bookGenre = selectedBookCategory
//                user?.musicGenre = selectedMusicGenre
                user?.let { u ->
                    u.musicGenre = u.musicGenre?.distinct()?.toMutableList()
                    u.category = u.category?.distinct()?.toMutableList()
                    u.bookGenre = u.bookGenre?.distinct()?.toMutableList()
                    u.podcastCategory = u.podcastCategory?.distinct()?.toMutableList()
                    userViewmodel.updateUserInfo(u).observe(viewLifecycleOwner){
                        binding.progressBar3.isVisible = false
                        pref[PreferenceHelper.NEW_ENTERTAINMENT_PREFERENCE] = true
                        if(it) (requireActivity() as MainActivity).showSnacbar("Preference saved")
                        else Toast.makeText(requireContext() , "unable to save preference" , Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().popBackStack(R.id.recommendationFragment , true)
        }

        userViewmodel.getError().observe(viewLifecycleOwner){}
        userViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.progressBar3.isVisible = false
            error?.handleError(requireContext() , {
                binding.fetchRecommendationBtn.isVisible = true
                userViewmodel.error.value = null
            }){
                binding.recommendationErrorTextview.isVisible = true
                binding.recommendationContainer.isVisible = false
                binding.progressBar3.isVisible = false
            }
        }

    }

}