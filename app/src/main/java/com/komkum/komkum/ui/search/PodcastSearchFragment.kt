package com.komkum.komkum.ui.search

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentPodcastSearchBinding
import com.komkum.komkum.ui.podcast.PodcastListFragment
import com.komkum.komkum.ui.podcast.PodcastViewModel
import com.komkum.komkum.util.adaper.BrowseCategoryAdapter
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class PodcastSearchFragment : Fragment() , IRecyclerViewInteractionListener<PodcastEpisode> {

    lateinit var binding : FragmentPodcastSearchBinding

    val podcastViewmodel : PodcastViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    var searchResult : Search? = null
    var browseList : List<MusicBrowse> = emptyList()

    var playingEpisodeId : String? = null

    var showBrowseItem = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            searchResult = it.getParcelable<Search>("SEARCH_RESULT")
            browseList = it.getParcelableArrayList("BROWSE_LIST") ?: emptyList()
        }
        binding = FragmentPodcastSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var podcastCategoryListener = object : IRecyclerViewInteractionListener<MusicBrowse> {
            override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToPodcastList(PodcastListFragment.PODCASTS_BY_CATEGORY , data.name)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }
        binding.podcastCategoryView.libraryItemImageView.setImageResource(R.drawable.ic_dashboard_black_24dp)
        binding.podcastCategoryView.libraryItemTextview.text = getString(R.string.podcast_categories)
        binding.podcastCategoryView.root.setOnClickListener {
            browseList.let{
                var browseInfo = RecyclerViewHelper(interactionListener = podcastCategoryListener , owner = viewLifecycleOwner)
                var browseList = it.groupBy { browse -> browse.contentType }

                binding.searchContainer.visibility = View.GONE
                binding.podcastBrowseRecyclerview.isVisible = true
                showBrowseItem = !showBrowseItem

                browseList[MusicBrowse.CONTENT_TYPE_PODCAST]?.let { browseList ->
                    var list = browseList.groupBy { browse -> browse.category }
                    var categoryBrowseAdapter = BrowseCategoryAdapter(browseInfo , list)
                    binding.podcastBrowseRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.podcastBrowseRecyclerview.adapter = categoryBrowseAdapter
                }
            }
        }

        //podcast interaction handler

        var interactionListener = object : IRecyclerViewInteractionListener<Podcast<String>>{
            override fun onItemClick(data: Podcast<String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToPodcast(data._id)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var episodeInfo = RecyclerViewHelper(type = "EPISODE" , stateManager = mainActivityViewmodel.episodeStateManager ,
            owner = viewLifecycleOwner , interactionListener = this , playbackState = mainActivityViewmodel.playbackState)
        var podcastInfo = RecyclerViewHelper(type = "PODCAST" , interactionListener = interactionListener , owner = viewLifecycleOwner)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.podcastInfo = podcastInfo
        binding.episodeInfo = episodeInfo
        binding.searchResult = searchResult
        binding.podcastLastChecked = Date()

        searchResult?.let {
            mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner){metadata ->
                playingEpisodeId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                playingEpisodeId?.let { it ->
                    mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                        var a = it::class.qualifiedName
                        when(a){
                            "com.komkum.komkum.data.model.PodcastEpisode" ->{
                                var selectedEpisode = it as PodcastEpisode
                                mainActivityViewmodel.episodeStateManager.selectedItem.value = selectedEpisode
                            }
                        }
                    }
                }
            }
        }

        podcastViewmodel.getError().observe(viewLifecycleOwner){}
        podcastViewmodel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext(), {podcastViewmodel.removeOldErrors()}){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(data: PodcastEpisode, position: Int, option: Int?) {
        when(option){
            Podcast.PODCAST_ACTION_PLAY ->{
                if(playingEpisodeId == data._id) mainActivityViewmodel.play()
                else{
                    searchResult?.episodes?.let {
                        mainActivityViewmodel.prepareAndPlayPodcast(it , PlayerState.PodcastState() , position , false , it.first().podcast)
                    }
                }
                mainActivityViewmodel.episodeStateManager.selectedItem.value = data
            }
            Podcast.PODCAST_ACTION_PAUSE ->{
                mainActivityViewmodel.pause()
            }
            Podcast.PODCAST_ACTION_LIKE ->{
                binding.progressBar8.isVisible = true
                podcastViewmodel.followEpisode(data._id).observe(viewLifecycleOwner){
                    binding.progressBar8.isVisible = false
                    if(it == true){
                        data.isInFavorte = true
                        binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.like_episode_imageview)?.visibility = View.INVISIBLE
                        binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.unlike_episode_imageview)?.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.episode_added_to_library))
                    }
                    else Toast.makeText(requireContext() , "Unable to follow the podcast" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_UNLIKE ->{
                binding.progressBar8.isVisible = true
                podcastViewmodel.unfollowEpisode(data._id).observe(viewLifecycleOwner){
                    binding.progressBar8.isVisible = false
                    if(it == true){
                        data.isInFavorte = false
                        binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.like_episode_imageview)?.visibility = View.VISIBLE
                        binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.unlike_episode_imageview)?.visibility = View.INVISIBLE
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.episode_removed_from_download))
                    }
                    else Toast.makeText(requireContext() , "Unable to remove the Episode from library" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_DOWNLOAD ->{
                binding.progressBar8.isVisible = true
                mainActivityViewmodel.downloadEpisode(data).observe(viewLifecycleOwner){
                    binding.progressBar8.isVisible = false
                    if(it == true){
                        binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.download_episode_imageview)?.visibility = View.INVISIBLE
                        binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.remove_download_imageview)?.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.episode_added_to_download) , "View"){
                            var bundle = bundleOf("SELECTED_PAGE" to 3)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                    }
                    else Toast.makeText(requireContext() , "Unable to download the episode" , Toast.LENGTH_LONG).show()

                }
            }
            Podcast.PODCAST_ACTION_REMOVE_DOWNLOAD ->{
                binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.download_episode_imageview)?.visibility = View.VISIBLE
                binding.episodeSearchRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.remove_download_imageview)?.visibility = View.INVISIBLE

            }
            Podcast.PODCAST_ACTION_NONE ->  (requireActivity() as MainActivity).moveToEpisode(data)
        }
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}