package com.komkum.komkum.ui.podcast

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.PodcastEpisode
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentEpisodeListBinding
import com.komkum.komkum.databinding.FragmentPodcastListBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleListDataObservation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class EpisodeListFragment : Fragment() , IRecyclerViewInteractionListener<PodcastEpisode> {
    lateinit var binding : FragmentEpisodeListBinding
    val podcastViewmodel : PodcastViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    var playingEpisodeId : String? = null
    var episodes : List<PodcastEpisode>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentEpisodeListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , this.getString(R.string.your_episodes))
        var info = RecyclerViewHelper(type = "EPISODE" , stateManager = mainActivityViewmodel.episodeStateManager ,
            owner = viewLifecycleOwner , interactionListener = this , playbackState = mainActivityViewmodel.playbackState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.info = info

        podcastViewmodel.userFavoriteEpisodes.handleListDataObservation(viewLifecycleOwner){
            binding.podcastViewmodel = podcastViewmodel
            binding.podcatLastChecked = Date()

            episodes = it

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
            error.handleError(requireContext()){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onItemClick(data: PodcastEpisode, position: Int, option: Int?) {
        when(option){
            Podcast.PODCAST_ACTION_PLAY ->{
                if(playingEpisodeId == data._id) mainActivityViewmodel.play()
                else{
                    episodes?.let {
                        mainActivityViewmodel.prepareAndPlayPodcast(it , PlayerState.PodcastState() , position , false )
                    }
                }
                mainActivityViewmodel.episodeStateManager.selectedItem.value = data
            }
            Podcast.PODCAST_ACTION_PAUSE ->{
                mainActivityViewmodel.pause()
            }
            Podcast.PODCAST_ACTION_LIKE ->{
                podcastViewmodel.followEpisode(data._id).observe(viewLifecycleOwner){
                    if(it == true){
                        data.isInFavorte = true
                        binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.like_episode_imageview)?.visibility = View.INVISIBLE
                        binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.unlike_episode_imageview)?.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar("Episode added to Library")
                    }
                    else Toast.makeText(requireContext() , "Unable to follow the podcast" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_UNLIKE ->{
                podcastViewmodel.unfollowEpisode(data._id).observe(viewLifecycleOwner){
                    if(it == true){
                        data.isInFavorte = false
                        binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.like_episode_imageview)?.visibility = View.VISIBLE
                        binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.unlike_episode_imageview)?.visibility = View.INVISIBLE
                        (requireActivity() as MainActivity).showSnacbar("Episode removed from Library")
                    }
                    else Toast.makeText(requireContext() , "Unable to remove the Episode from library" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_DOWNLOAD ->{
                mainActivityViewmodel.downloadEpisode(data).observe(viewLifecycleOwner){
                    if(it == true){
                        binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.download_episode_imageview)?.visibility = View.INVISIBLE
                        binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.remove_download_imageview)?.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar("Episode added to download" , "View"){
                            var bundle = bundleOf("SELECTED_PAGE" to 3)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                    }
                    else Toast.makeText(requireContext() , "Unable to download the episode" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_REMOVE_DOWNLOAD ->{
                binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.download_episode_imageview)?.visibility = View.VISIBLE
                binding.episodeListRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.remove_download_imageview)?.visibility = View.INVISIBLE

            }
            Podcast.PODCAST_ACTION_NONE ->  (requireActivity() as MainActivity).moveToEpisode(data)
        }
    }

    override fun activiateMultiSelectionMode() {
        TODO("Not yet implemented")
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }


}