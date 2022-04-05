package com.komkum.komkum.ui.podcast

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.PodcastEpisode
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentPodcastBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.PodcastEpisodeAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.showDialog
import com.komkum.komkum.util.extensions.showShareMenu
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PodcastFragment : Fragment() , IRecyclerViewInteractionListener<PodcastEpisode> {


    lateinit var binding : FragmentPodcastBinding

    val podcastViewmodel : PodcastViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()
    var podcastId : String? = null

    var podcast : Podcast<PodcastEpisode>? = null
    var playingEpisodeId : String? = null
    var totalFollowers = 0
    var sortSelection = 0

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                podcastId = it.getString("PODCAST_ID")
                podcastViewmodel.getPodcast(podcastId!!)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPodcastBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 ->  binding.toolbar.title = ""
                verticalOffset == -appBarLayout.totalScrollRange -> {
                    binding.toolbar.title = binding.podcastTitleTextview.text.toString()
                }
//                verticalOffset > -appBarLayout.totalScrollRange -> binding.customPlaylistImageview.visibility = View.VISIBLE
            }
        })

        var info = RecyclerViewHelper(type = "EPISODE" , stateManager = mainActivityViewmodel.episodeStateManager ,
            owner = viewLifecycleOwner , interactionListener = this , playbackState = mainActivityViewmodel.playbackState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.info = info


        podcastViewmodel.podcastResult.observe(viewLifecycleOwner){
            binding.podcastViewmodel = podcastViewmodel
            binding.podcatLastChecked = it.lastChecked
            podcast = it
            totalFollowers = it.podcastListeners?.size ?: 0
            configureActionBar(binding.toolbar , it.name)
            Picasso.get().load(it.image).placeholder(R.drawable.podcast_placeholder)
                .fit().centerCrop().into(binding.podcastImageview)
            binding.podcastTitleTextview.text = it.name
            binding.podcastPublishernameTextview.text = it.publisherName
            binding.pocastFollowerNumberTextview.text = "$totalFollowers ${getString(R.string.followers)}"
            binding.totalEpisodeTextview.text = "${it.episods?.size ?: 0} Episodes"
//            mainActivityViewmodel.showBackgroundPalete(it.image!!.replace("localhost" , AdapterDiffUtil.URL) , binding.appbar , requireActivity())

            if(it.isInFavorte == true){
                binding.podcastUnfollowBtn.visibility = View.VISIBLE
                binding.podcastFollowBtn.visibility = View.INVISIBLE
            }
            else {
                binding.podcastUnfollowBtn.visibility = View.INVISIBLE
                binding.podcastFollowBtn.visibility = View.VISIBLE
            }


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

            showBackgroundPalete(it.image!!.replace("localhost" , AdapterDiffUtil.URL))

        }

        binding.podcastDescriptionTextView.setOnClickListener {
            if(binding.podcastDescriptionTextView.maxLines == 4) binding.podcastDescriptionTextView.maxLines = 100
            else binding.podcastDescriptionTextView.maxLines = 4
        }

        binding.podcastUnfollowBtn.setOnClickListener {
            binding.podcastLoadingProgressbar.isVisible= true
            podcastViewmodel.unfollowPodcast(podcastId!!).observe(viewLifecycleOwner){
                binding.podcastLoadingProgressbar.isVisible= false
                if(it == true){
                    podcast?.let {
                        totalFollowers -= 1
                        binding.pocastFollowerNumberTextview.text = "${totalFollowers} Followers"
                    }

                    binding.podcastUnfollowBtn.visibility = View.INVISIBLE
                    binding.podcastFollowBtn.visibility = View.VISIBLE
                    (requireActivity() as MainActivity).showSnacbar("Podcast removed from Library")
                }
                else Toast.makeText(requireContext() , "Error ocured" , Toast.LENGTH_LONG).show()
            }
        }


        binding.podcastFollowBtn.setOnClickListener {
            binding.podcastLoadingProgressbar.isVisible= true
            podcastViewmodel.followPodcast(podcastId!!).observe(viewLifecycleOwner){
                binding.podcastLoadingProgressbar.isVisible= false
                if(it == true){
                    podcast?.let {
                        it.isInFavorte = true
                        totalFollowers += 1
                        binding.pocastFollowerNumberTextview.text = "${totalFollowers} Followers"
                    }
                    binding.podcastUnfollowBtn.visibility = View.VISIBLE
                    binding.podcastFollowBtn.visibility = View.INVISIBLE
                    (requireActivity() as MainActivity).showSnacbar("Podcast added to Library")
                }
                else Toast.makeText(requireContext() , "Unable to follow the podcast" , Toast.LENGTH_LONG).show()
            }
        }

        binding.sortImageview.setOnClickListener {
            var optionList = listOf("Newest first" , "Oldest first" ,  "Top episodes" , "Downloads"   , )
            var adapter = binding.episodeRecyclerview.adapter as PodcastEpisodeAdapter
           MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
               title(text = "Sort by")
               var dialog = listItemsSingleChoice(items = optionList , initialSelection = sortSelection){dialog, index, text ->
                   sortSelection = index
                  var sortedEpisodes =  when(index){
                      0 -> podcast?.episods?.reversed()
                      1 -> podcast?.episods
                      2-> podcast?.episods?.sortedByDescending { episode -> episode.likeCount }
                      3 ->  podcast?.episods?.filter { episode -> episode.isInDownloads == true }
                      else -> podcast?.episods
                   }
                   if (sortedEpisodes != null) {
                       adapter.episodes = sortedEpisodes
                       adapter.notifyDataSetChanged()
                   }
               }
           }
        }

        podcastViewmodel.getError().observe(viewLifecycleOwner){}
        podcastViewmodel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext(), {podcastViewmodel.removeOldErrors()}){
                binding.podcastErrorTextview.visibility = View.VISIBLE
                podcastViewmodel.error.value = null
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.podcast_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            R.id.go_to_publisher_menu_item ->{
                podcast?.let { (requireActivity() as MainActivity).moveToPodcastPublisher(it.publisher!!) }
                true
            }
            R.id.share_podcast_menu_item -> {
                podcast?.let {
                    var podcastLink = "api.komkum.com/podcast/${it._id}"
                    var message = "I'm listening to ${it.name} Podcast on Komkum. Check it out! $podcastLink"
                    requireActivity().showShareMenu(it.name!! , message)
                }
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: PodcastEpisode, position: Int, option: Int?) {
        when(option){
            Podcast.PODCAST_ACTION_PLAY ->{
                if(playingEpisodeId == data._id) mainActivityViewmodel.play()
                else{
                    var sortedEpisodes =  when(sortSelection){
                        0 -> podcast?.episods?.reversed()
                        1 -> podcast?.episods
                        2-> podcast?.episods?.sortedByDescending { episode -> episode.likeCount }
                        3 ->  podcast?.episods?.filter { episode -> episode.isInDownloads == true }
                        else -> podcast?.episods
                    }
                    sortedEpisodes?.let {
                        mainActivityViewmodel.prepareAndPlayPodcast(it , PlayerState.PodcastState() , position , false , podcast!!._id)
                    }
                }
                mainActivityViewmodel.episodeStateManager.selectedItem.value = data
            }
            Podcast.PODCAST_ACTION_PAUSE ->{
                mainActivityViewmodel.pause()
            }
            Podcast.PODCAST_ACTION_LIKE ->{
                binding.podcastLoadingProgressbar.isVisible = true
                podcastViewmodel.followEpisode(data._id).observe(viewLifecycleOwner){
                    binding.podcastLoadingProgressbar.isVisible = false
                    if(it == true){
                        data.isInFavorte = true
                        binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.like_episode_imageview)?.visibility = View.INVISIBLE
                        binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.unlike_episode_imageview)?.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar("Episode added to Library")
                    }
                    else Toast.makeText(requireContext() , "Unable to follow the podcast" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_UNLIKE ->{
                binding.podcastLoadingProgressbar.isVisible = true
                podcastViewmodel.unfollowEpisode(data._id).observe(viewLifecycleOwner){
                    binding.podcastLoadingProgressbar.isVisible = false
                    if(it == true){
                        data.isInFavorte = false
                        binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.like_episode_imageview)?.visibility = View.VISIBLE
                        binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.unlike_episode_imageview)?.visibility = View.INVISIBLE
                        (requireActivity() as MainActivity).showSnacbar("Episode removed from Library")
                    }
                    else Toast.makeText(requireContext() , "Unable to remove the Episode from library" , Toast.LENGTH_LONG).show()
                }
            }
            Podcast.PODCAST_ACTION_DOWNLOAD ->{
                binding.podcastLoadingProgressbar.isVisible = true
                mainActivityViewmodel.downloadEpisode(data).observe(viewLifecycleOwner){
                    binding.podcastLoadingProgressbar.isVisible = false
                    if(it == true){
                        binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.download_episode_imageview)?.visibility = View.INVISIBLE
                        binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.remove_download_imageview)?.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar("Episode added to download" , "View"){
                            var bundle = bundleOf("SELECTED_PAGE" to 3)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                    }
                    else Toast.makeText(requireContext() , "Unable to download the episode" , Toast.LENGTH_LONG).show()

                }
            }
            Podcast.PODCAST_ACTION_REMOVE_DOWNLOAD ->{
                binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.download_episode_imageview)?.visibility = View.VISIBLE
                binding.episodeRecyclerview.findViewHolderForAdapterPosition(position)?.itemView?.findViewById<ImageView>(R.id.remove_download_imageview)?.visibility = View.INVISIBLE

            }
            Podcast.PODCAST_ACTION_NONE ->  (requireActivity() as MainActivity).moveToEpisode(data)
        }
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


    fun showBackgroundPalete(imageUrl : String ){
        var target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    Palette.from(it).generate {
                        it?.darkMutedSwatch?.let {
                            var grDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb, 0x00000000))
                            var grDrawable2 = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb,0x00000000 , 0x00000000))

//                               binding.queueSongImageViewpager.setBackgroundResource(R.drawable.item_selected)
                            binding.appbar.background = grDrawable
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                requireActivity().window.statusBarColor = it.rgb
                            }
                        }
                    }
                }

            }
            override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {}
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        Picasso.get().load(imageUrl).into(target)
    }

}