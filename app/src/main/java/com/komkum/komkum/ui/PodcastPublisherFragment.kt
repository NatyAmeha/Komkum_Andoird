package com.komkum.komkum.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentPodcastPublisherBinding
import com.komkum.komkum.ui.component.leaderboardWithHeader
import com.komkum.komkum.ui.podcast.PodcastViewModel
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.showShareMenu
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PodcastPublisherFragment : Fragment() , IRecyclerViewInteractionListener<Podcast<String>> {
    lateinit var binding : FragmentPodcastPublisherBinding

    val podcastViewmodel : PodcastViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()


    var publisherId : String? = null
    var publisher : PodcastPublisher<Podcast<String>>? = null

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                publisherId = it.getString("PUBLISHER_ID")
                publisherId?.let {
                    podcastViewmodel.getPodcastPublisher(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentPodcastPublisherBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")
        var info = RecyclerViewHelper(type = "PODCAST" , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 , interactionListener = this , owner = viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.podcastViewmodel = podcastViewmodel
        binding.podcastInfo = info



        podcastViewmodel.publisherResult.observe(viewLifecycleOwner){
            publisher = it
            binding.donatePublisherBtn.isVisible = it.donationEnabled ?: true
            Picasso.get().load(it.image).fit().centerCrop().transform(CircleTransformation()).into(binding.publisherImageview)

            binding.donationComposeview.setContent {
                ZomaTunesTheme(true) {
                    donationLeaderboardSection(it._id, it.name!! , it.image!!)
                }
            }
        }

        binding.donatePublisherBtn.setOnClickListener {
            publisher?.let {
                (requireActivity() as MainActivity).moveToDonation(Donation.PODCAST_DONATION , it.user!! , it.name!! , publisherId!! , it.image!!)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: Podcast<String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).moveToPodcast(data._id)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    @Composable
    fun donationLeaderboardSection(podcastPublisherId: String , publisherName : String , image : String) {
        val donations by mainActivityViewmodel.getDonations(podcastPublisherId).observeAsState()

        if(!donations.isNullOrEmpty()){
            binding.faanSupportTextview.isVisible = true
            binding.donationComposeview.isVisible = true
            var totalDonations = donations?.map { donation -> donation.amount!! }?.reduce { acc, i -> acc.plus(i) }
            leaderboardWithHeader(
                image = image,
                title = publisherName,
                subtitle = "${donations!!.size} donations",
                extra = "ETB $totalDonations",
                items = donations!!.toLeaderboard()
            )
        }
    }
}