package com.komkum.komkum.ui.podcast

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.*
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentPodcastEpisodeBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.EpisodeCommentAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.showDialog
import com.komkum.komkum.util.extensions.showShareMenu
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class PodcastEpisodeFragment : Fragment() , IRecyclerViewInteractionListener<EpisodeComment<String>> {

    lateinit var binding : FragmentPodcastEpisodeBinding
    lateinit var commentAdapter : EpisodeCommentAdapter
    val podcastViewModel : PodcastViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    var episodeInfo =  MutableLiveData<PodcastEpisode>()
    var playingEpisodeId : String? = null
    var isEpisodeDownloaded : Boolean = false
    var loadFromCache : Boolean = false
    var episodeLikeCount = 0

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                it.getParcelable<PodcastEpisode>("EPISODE_INFO")?.let{
                    episodeInfo.value =  it
                }
                loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
                it.getString("EPISODE_ID")?.let {
                    Toast.makeText(requireActivity() , it , Toast.LENGTH_LONG).show()
                    episodeInfo.value = podcastViewModel.getPodcastEpisode(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentPodcastEpisodeBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when (verticalOffset) {
                0 -> {
                    binding.toolbar.title = ""
                }
                -appBarLayout.totalScrollRange -> {
                    binding.toolbar.title = binding.episodetitleTextview.text.toString()
                }
                //                verticalOffset > -appBarLayout.totalScrollRange -> binding.customPlaylistImageview.visibility = View.VISIBLE
            }
        })

        var info = RecyclerViewHelper("COMMENT" , interactionListener = this , owner = viewLifecycleOwner)
        var pref = PreferenceHelper.getInstance(requireContext())
        var profileImage = pref.get(AccountState.PROFILE_IMAGE , "placeholder")
        var userName = pref.get(AccountState.USERNAME , "")
        var userId = pref.get(AccountState.USER_ID , "")

        episodeInfo.observe(viewLifecycleOwner) {it ->
            binding.episodeProgressbar.isVisible = false
            it?.let { episodeInf ->
                binding.appbar.isVisible = true
                binding.container.isVisible = true
                configureActionBar(binding.toolbar , episodeInf.tittle)
                commentAdapter = EpisodeCommentAdapter(info)
                if(isEpisodeDownloaded){
                    binding.episodeDownloadImageview.visibility = View.INVISIBLE
                    binding.episodeRemoveDownloadImageview.visibility = View.VISIBLE
                }

                if(userId.isBlank()){
                    binding.commenterProfileImageview.isVisible = false
                    binding.addCommentTextview.isVisible = false
                }


                if(loadFromCache){
//                binding.episodeLikeImageview.isEnabled = false
                    binding.commenterProfileImageview.isVisible = false
                    binding.episodeCommentNumberTextview.isVisible = false
                    binding.addCommentTextview.isVisible = false
                    binding.episodeLikeCountTextview.isVisible = false

                    binding.episodeDownloadImageview.visibility = View.INVISIBLE
                    binding.episodeRemoveDownloadImageview.visibility = View.VISIBLE
                }
                else{
                    podcastViewModel.isEpisodeDownloaded(episodeInf._id).observe(viewLifecycleOwner){
                        if(it){
                            binding.episodeDownloadImageview.visibility = View.INVISIBLE
                            binding.episodeRemoveDownloadImageview.visibility = View.VISIBLE
                        }
                    }
                }

                binding.episodetitleTextview.text = episodeInf.tittle
                episodeLikeCount = episodeInf.likeCount ?: 0

                binding.episodeLikeCountTextview.text = episodeLikeCount.toString()
                binding.episodeDurationTextview.text = episodeInf.duration?.toLong()?.let { DateUtils.formatElapsedTime(it) }

                Picasso.get().load(episodeInf.image).placeholder(R.drawable.music_placeholder)
                    .fit().centerCrop().into(binding.podcastEpisodeImageview)

                if(!userName.isBlank()) binding.commenterProfileImageview.displayAvatar(profileImage , userName.first().toString())

                episodeInf.dateCreated?.let {
                    val cal = Calendar.getInstance()
                    cal.time = it
                    binding.episodeDateTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}  ${cal.get(Calendar.DATE)}"
                }

                binding.episodeDescriptionTextview.text = episodeInf.description

                if(episodeInf.isInFavorte == true){
                    binding.episodeLikeImageview.visibility = View.INVISIBLE
                    binding.episodeUnlikeImageview.visibility = View.VISIBLE
                }
                else{
                    binding.episodeLikeImageview.visibility = View.VISIBLE
                    binding.episodeUnlikeImageview.visibility = View.INVISIBLE
                }

                binding.episodeCommentNumberTextview.text = "Comments   ${episodeInf.comments?.size}"
                binding.episodeCommentRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                binding.episodeCommentRecyclerview.adapter = commentAdapter

                if(!episodeInf.comments.isNullOrEmpty())
                    commentAdapter.submitList(episodeInf.comments!!.sortedByDescending { comment -> comment.date })


                mainActivityViewmodel.showBackgroundPalete(episodeInf.thumbnailPath!! , binding.appbar , requireActivity())

                mainActivityViewmodel.playbackState.observe(viewLifecycleOwner) {
                    playingEpisodeId = mainActivityViewmodel.nowPlaying.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                    if(playingEpisodeId == episodeInf._id){
                        when(it.state){
                            PlaybackStateCompat.STATE_PLAYING -> {
                                binding.episodePlayFab.visibility = View.INVISIBLE
                                binding.episodePauseFab.visibility = View.VISIBLE
                            }
                            PlaybackStateCompat.STATE_PAUSED -> {
                                binding.episodePlayFab.visibility = View.VISIBLE
                                binding.episodePauseFab.visibility = View.INVISIBLE
                            }
                            else ->{
                                binding.episodePlayFab.visibility = View.VISIBLE
                                binding.episodePauseFab.visibility = View.INVISIBLE
                            }
                        }
                    }

                }

                binding.episodeDescriptionTextview.setOnClickListener {
                    if(binding.episodeDescriptionTextview.maxLines == 5)
                        binding.episodeDescriptionTextview.maxLines = 1000
                    else binding.episodeDescriptionTextview.maxLines = 5
                }

                binding.episodePlayFab.setOnClickListener{
                    if(playingEpisodeId == episodeInf._id) mainActivityViewmodel.play()
                    else mainActivityViewmodel.prepareAndPlayPodcast(listOf(episodeInf) , PlayerState.PodcastState() , null , loadFromCache)
                }

                binding.episodePauseFab.setOnClickListener {
                    mainActivityViewmodel.pause()
                    binding.episodePlayFab.visibility = View.VISIBLE
                    binding.episodePauseFab.visibility = View.INVISIBLE
                }

                binding.episodeLikeImageview.setOnClickListener {
                    binding.episodeProgressbar.isVisible = true
                    podcastViewModel.followEpisode(episodeInf._id).observe(viewLifecycleOwner){result ->
                        binding.episodeProgressbar.isVisible = false
                        if(result == true){
                            episodeInf.isInFavorte = true
                            episodeLikeCount+=1
                            binding.episodeLikeCountTextview.text = episodeLikeCount.toString()
                            binding.episodeUnlikeImageview.visibility = View.VISIBLE
                            binding.episodeLikeImageview.visibility = View.INVISIBLE
                            (requireActivity() as MainActivity).showSnacbar("Episode added to Library")
                        }
                        else Toast.makeText(requireContext() , "Error occured" , Toast.LENGTH_LONG).show()
                    }
                }

                binding.episodeShareImageview.setOnClickListener {
                    var episodeLink = "https://komkum.com/episode/${episodeInf._id}"
                    var message = "I'm hooked on ${episodeInf.podcastName} Podcast - ${episodeInf.tittle}. Check out this episode! $episodeLink"
                    requireActivity().showShareMenu(episodeInf.tittle!! , message)
                }

                binding.addCommentTextview.setOnClickListener {
                    var dialog =  MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT))
                        . cancelOnTouchOutside(true)
                        .cornerRadius(literalDp = 0f)
                        .customView(R.layout.comment_input_layout , scrollable = true)

                    var customview =  dialog.getCustomView()
                    Picasso.get().load(profileImage).placeholder(R.drawable.music_placeholder)
                        .fit().centerCrop().transform(CircleTransformation()).into(customview.findViewById<ImageView>(R.id.commentor_imageview))
                    var sendButton = customview.findViewById<ImageView>(R.id.comment_add_imageview)
                    var commentTExtview =  customview.findViewById<EditText>(R.id.comment_edit_text)

                    commentTExtview.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if(count > 0) sendButton.visibility = View.VISIBLE
                            else sendButton.visibility = View.INVISIBLE
                        }
                        override fun afterTextChanged(p0: Editable?) {}

                    })

                    sendButton.setOnClickListener {
                        binding.episodeProgressbar.isVisible = true
                        var userId = pref.get(AccountState.USER_ID , "")
                        var userName = pref.get(AccountState.USERNAME , "")
                        var commentText = commentTExtview.text.toString()
                        var commentInfo = EpisodeComment<String>(text = commentText , userId = userId , username = userName , profileImage = profileImage)
                        podcastViewModel.commentEpisode(episodeInf._id , commentInfo).observe(viewLifecycleOwner){
                            binding.episodeProgressbar.isVisible = false
                            if(it == true){
                                episodeInf.comments?.add(0 , commentInfo)
                                commentAdapter.submitList(episodeInf.comments)
                                dialog.dismiss()
                                Toast.makeText(requireContext() , "Comment added" , Toast.LENGTH_LONG).show()
                            }
                            else Toast.makeText(requireContext() , "Unable to add comment" , Toast.LENGTH_LONG).show()
                        }
                    }
                    dialog.show()
                }
                binding.episodeUnlikeImageview.setOnClickListener {
                    binding.episodeProgressbar.isVisible = true
                    podcastViewModel.unfollowEpisode(episodeInf._id).observe(viewLifecycleOwner){
                        binding.episodeProgressbar.isVisible = false
                        if(it == true){
                            episodeInf.isInFavorte = false
                            episodeLikeCount -= 1
                            binding.episodeLikeCountTextview.text = episodeLikeCount.toString()
                            binding.episodeUnlikeImageview.visibility = View.INVISIBLE
                            binding.episodeLikeImageview.visibility = View.VISIBLE
                            (requireActivity() as MainActivity).showSnacbar("Episode removed from Library")
                        }
                        else Toast.makeText(requireContext() , "Error occured" , Toast.LENGTH_LONG).show()
                    }
                }

                binding.episodeDownloadImageview.setOnClickListener {
                    podcastViewModel.downloadEpisode(episodeInf).observe(viewLifecycleOwner){
                        if(it == true){
                            episodeInf.isInDownloads = true
                            binding.episodeDownloadImageview.visibility = View.INVISIBLE
                            binding.episodeRemoveDownloadImageview.visibility = View.VISIBLE

                            (requireActivity() as MainActivity).showSnacbar("Episode added to download" , "View"){
                                var bundle = bundleOf("SELECTED_PAGE" to 3)
                                findNavController().navigate(R.id.downloadListFragment , bundle)
                            }
                        }
                    }
                }

                binding.episodeRemoveDownloadImageview.setOnClickListener {
                    (requireActivity() as MainActivity).showDialog("Delete" , "You won't be able to play this offline" , getString(R.string.remove)){
                        podcastViewModel.removeDownloadedEpisode(episodeInf._id).observe(viewLifecycleOwner){
                            if(it){
                                episodeInf.isInDownloads = false
                                binding.episodeRemoveDownloadImageview.visibility = View.INVISIBLE
                                binding.episodeDownloadImageview.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        podcastViewModel.getError().observe(viewLifecycleOwner){}
        podcastViewModel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext() , {podcastViewModel.removeOldErrors()}){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: EpisodeComment<String>, position: Int, option: Int?) {

    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}