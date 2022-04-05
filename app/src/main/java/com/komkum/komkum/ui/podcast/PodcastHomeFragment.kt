package com.komkum.komkum.ui.podcast

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.toLowerCase
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.tabs.TabLayoutMediator
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.PodcastHomeFragmentBinding
import com.komkum.komkum.ui.book.BookViewModel
import com.komkum.komkum.ui.component.donationListViewpager
import com.komkum.komkum.ui.home.BaseBottomTabFragment
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.Resource

import com.komkum.komkum.util.adaper.PodcastHomeViewpagerAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.concurrent.fixedRateTimer


@ExperimentalPagerApi
@AndroidEntryPoint
class PodcastHomeFragment : BaseBottomTabFragment() , IRecyclerViewInteractionListener<Podcast<String>> {

    val  podcastViewmodel: PodcastViewModel by viewModels()
    lateinit var binding : PodcastHomeFragmentBinding
    lateinit var timer : Timer

    init {
        lifecycleScope.launchWhenCreated {
            podcastViewmodel.getPodcastHome()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PodcastHomeFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.statusBarColor = resources.getColor(R.color.light_secondaryDarkColor)
        binding.donationComposeview.setContent {
            ZomaTunesTheme(true) {
                podcastDonationsSections()
            }
        }

        var info = RecyclerViewHelper(type = "PODCAST" , interactionListener = this , owner = viewLifecycleOwner)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.podcastViewmodel = podcastViewmodel
        binding.podcastInfo = info

        podcastViewmodel.podcastHomeResult.observe(viewLifecycleOwner){
            binding.podcastProgressbar.isVisible = false
            binding.category1Textview.text = it?.podcastCategories?.get(0)?.toLowerCase()
            binding.category2Textview.text = it?.podcastCategories?.get(1)?.toLowerCase()
            binding.category3Textview.text = it?.podcastCategories?.get(2)?.toLowerCase()
            binding.category4Textview.text = it?.podcastCategories?.get(3)?.toLowerCase()

            it?.featuredPodcasts?.let {podcastList ->
                var adapter = PodcastHomeViewpagerAdapter(info , podcastList)
                binding.popularpodcastViewpager.adapter = adapter
                binding.popularpodcastViewpager.offscreenPageLimit = 5
                TabLayoutMediator(binding.viewpagerTablayout , binding.popularpodcastViewpager){tab, position -> }.attach()

                timer =  fixedRateTimer("podcast_carausel" , false , period = 5000){
                    var newPosition = binding.popularpodcastViewpager.currentItem + 1
                    if (newPosition > podcastList.lastIndex) newPosition = 0
                    requireActivity().runOnUiThread {
                        binding.popularpodcastViewpager.setCurrentItem(newPosition , true)
                    }
                }
            }
        }

        podcastViewmodel.podcastRepo.error.observe(viewLifecycleOwner ){error ->
            binding.podcastProgressbar.visibility = View.GONE
            binding.errorView.root.visibility = View.VISIBLE
            binding.errorView.errorTextview.text = "Something went wrong. \nPlease try again or listen your downloads "
            binding.errorView.gotoDownloadBtn.setOnClickListener {
                findNavController().navigate(R.id.downloadListFragment)
            }
            binding.errorView.tryagainBtn.setOnClickListener {
                var bundle = bundleOf("SELECTED_PAGE" to 1)
                findNavController().navigate(R.id.baseFragment , bundle)
            }
        }


    }

    override fun onDetach() {
        if (::timer.isInitialized) timer.cancel()
        super.onDetach()
    }

    override fun onItemClick(data: Podcast<String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).moveToPodcast(data._id )
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}



    @ExperimentalPagerApi
    @Composable
    fun podcastDonationsSections() {
        val homeREsult by podcastViewmodel.podcastHomeResult.observeAsState()
        homeREsult?.topPodcasterByDonation?.let { donationLeaderboards ->
//            var sortedLeaderboard = donationLeaderboards.sortedByDescending { board -> board.totalAmount }
            donationListViewpager(donations = donationLeaderboards){publisherId ->
                (requireActivity() as MainActivity).moveToPodcastPublisher(publisherId)
            }
        }
    }

}