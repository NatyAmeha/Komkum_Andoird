package com.komkum.komkum.ui.podcast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentPodcastListBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PodcastListFragment : Fragment() , IRecyclerViewInteractionListener<Podcast<String>> {

    lateinit var binding : FragmentPodcastListBinding
    val podcastViewmodel : PodcastViewModel by viewModels()

    var category : String? = null
    var loadType : Int? = null

    var toolbarTitle : String = this.getString(R.string.your_podcasts)
    companion object{
        const val PODCASTS_BY_CATEGORY = 0
        const val USER_PODCASTS = 1
    }

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                category = it.getString("CATEGORY")
                loadType = it.getInt("LOAD_TYPE")

                when(loadType){
                    PODCASTS_BY_CATEGORY -> {
                        toolbarTitle = category ?: ""
                        podcastViewmodel.getPodcastsByCategory(category!!)
                    }
                    USER_PODCASTS -> podcastViewmodel.getUserFavoritePodcasts()
                    else -> podcastViewmodel.getUserFavoritePodcasts()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPodcastListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        configureActionBar(binding.toolbar)
        var info = RecyclerViewHelper(type = "PODCAST" , interactionListener = this ,
            owner = viewLifecycleOwner , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.podcastInfo = info
        binding.podcastViewmodel = podcastViewmodel

        podcastViewmodel.podcastLists.observe(viewLifecycleOwner){
            configureActionBar(binding.toolbar , toolbarTitle)
            binding.podcastListProgressbar.isVisible = false
            if(it.isNullOrEmpty()){
                binding.errorTextview.isVisible = true
            }
        }
    }

    override fun onItemClick(data: Podcast<String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).moveToPodcast(data._id)
    }
    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}