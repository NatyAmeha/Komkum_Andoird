package com.komkum.komkum.ui.player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel

import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.DownloadRepository
import com.komkum.komkum.databinding.FragmentPlayerBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.MenuAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.ItemTouchHelperCallback
import com.komkum.komkum.util.viewhelper.PlayerState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.komkum.komkum.OnboardingActivity
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.komkum.komkum.data.model.MenuItem
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.data.model.MenuItem.Companion.podcastEpisodeMenuItem
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.podcast.PodcastViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment() , IRecyclerViewInteractionListener<Song<String, String>> {

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()
    val podcastViewmodel : PodcastViewModel by viewModels()

    @Inject  lateinit var downloadrepo : DownloadRepository

    lateinit var binding : FragmentPlayerBinding
    lateinit var queueAdapter :  SongAdapter

//    val stateManager : RecyclerviewStateManager<Song<String, String>> by lazy {
//        RecyclerviewStateManager<Song<String,String>>()
//    }
    var selectedMediaId : String? = null
    var selectedSongIndex : Int = 0
    var playerState : Int = -1

    var playerTitle = ""
    var playerSubtittle : String? = null 
    var imagelist = listOf<String>()

    var selectedAd : Ads? = null

    var selectedMenuList = mutableSetOf<MenuItem>()

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {intent->
                // show player when opened from notification
                intent.getBoolean("OPENPLAYER" , false).let { value ->
                    if(value){
                        playerState =  intent.getInt("PLAYBACK_STATE" , -1)
                        when(playerState){
                            1 ->  mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
                            2 ->  mainActivityViewmodel.playerState.value = PlayerState.AudiobookState()
                            3 ->  mainActivityViewmodel.playerState.value = PlayerState.RadioState()
                            4 ->  mainActivityViewmodel.playerState.value = PlayerState.PodcastState()
                        }

                        mainActivityViewmodel.queueitem.observe(viewLifecycleOwner){queue ->
                            queue?.let {
                                mainActivityViewmodel.playlistQueue.value =  it.toStreamable()
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        (requireActivity() as MainActivity).hideBottomView()
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =  TransitionInflater.from(context)
            .inflateTransition(android.R.transition.slide_bottom)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayerBinding.inflate(inflater)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        configureActionBar(binding.playerToolbar , "Now Playing")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = mainActivityViewmodel

        binding.textView3.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.textView3.marqueeRepeatLimit = -1
        binding.textView3.isSelected = true

        var preference = PreferenceHelper.getInstance(requireContext())
        var subscriptionInfo = preference.get(AccountState.SUBSCRIPTON_PREFERENCE , AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE)
        preference[AccountState.IS_REDIRECTION] = true

        (requireActivity() as MainActivity).hideBottomView()

        if(mainActivityViewmodel.isLoadFromCache) binding.playerMoreOptionImageview.isVisible = false



        mainActivityViewmodel.isFavoriteSong.observe(viewLifecycleOwner){
            binding.isSongFavorite = it
        }

        mainActivityViewmodel.playerState.observe(viewLifecycleOwner) { state ->
            when(state){
                is PlayerState.PlaylistState ->{
                    binding.playerState = PlayerState.PLAYLIST
                    playerState = PlayerState.PLAYLIST
                    // queue list display and move and swipe handler
                    var info = RecyclerViewHelper("QUEUE", mainActivityViewmodel.songrecyclerviewStateManeger, this, viewLifecycleOwner)
                    queueAdapter = SongAdapter(info)
                    var itemTouchCallback = ItemTouchHelperCallback(queueAdapter, info.type!!)
                    var itemTouchHelper = ItemTouchHelper(itemTouchCallback)
                    queueAdapter.addTouchHelper(itemTouchHelper)
                    itemTouchHelper.attachToRecyclerView(binding.queueListRecyclerview)
                    binding.queueListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.queueListRecyclerview.adapter = queueAdapter

                }
                is PlayerState.RadioState ->{
                    binding.playerState = PlayerState.RADIO
                    playerState = PlayerState.RADIO
                    binding.queueSongImageViewpager.isUserInputEnabled = false
                }
                is PlayerState.DownloadState -> binding.playerMoreOptionImageview.visibility = View.INVISIBLE
                is PlayerState.AudiobookState -> binding.playerState = PlayerState.AUDIOBOOK
                is PlayerState.PodcastState -> {
                    binding.playerState = PlayerState.PODCAST_EPISODE
                    playerState = PlayerState.PODCAST_EPISODE
                }
                else ->{}

            }.exhaustive
        }


        mainActivityViewmodel.playedFrom.observe(viewLifecycleOwner){
            binding.nowPlayingTextview.text = it
        }



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            var progress = binding.playerContainer.progress
            if(progress == 1.0f) binding.playerContainer.progress = 0.0f
            else findNavController().navigateUp()
        }


//        mainActivityViewmodel.queueitem.observe(viewLifecycleOwner){queue ->
//            queue?.let {
//                mainActivityViewmodel.playlistQueue.value =  it.toSongList().toMutableList()
//            }
//        }



        mainActivityViewmodel.playlistQueue.observe(viewLifecycleOwner) {
            Log.i("queuesize" , it.size.toString())
            imagelist = it.map { song -> song.thumbnailPath!! }
            var queueViewpagerAdapter = QueueListAdapter()

            binding.queueSongImageViewpager.setPageTransformer( MarginPageTransformer(200))
            binding.queueSongImageViewpager.adapter = queueViewpagerAdapter
            queueViewpagerAdapter.submitList(imagelist)


            var queueList = it?.filter { stream -> stream.isAd == false }
            if(mainActivityViewmodel.playerState.value is PlayerState.PlaylistState)
                queueAdapter.submitList(queueList as List<Song<String, String>>?)

        }




        mainActivityViewmodel.playbackDuration.observe(viewLifecycleOwner) { duration ->
            binding.songProgressBar.max  = duration.toInt()
            return@observe
        }

        mainActivityViewmodel.playbackPosition.observe(viewLifecycleOwner ) { position ->
            binding.songProgressBar.progress  = position.toInt()
            return@observe
        }

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner){ metadata ->
            metadata?.let {mdata ->
                playerTitle = mdata.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: ""
                playerSubtittle = mdata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE) ?: ""

                mdata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)?.let { index ->
                    if(index.toInt() >= 0){
                        Log.i("playerindex" , index)
                        selectedSongIndex = index.toInt()
                        binding.queueSongImageViewpager.setCurrentItem(selectedSongIndex , false)
                    }

                    mainActivityViewmodel.playlistSelectedIndex.value = index.toInt()
                    Log.i("selectindex1" , index)
                    mainActivityViewmodel.playlistQueue.value?.let { streams ->
                        if (streams.isNotEmpty() &&  (streams.size > selectedSongIndex)) {
                            var selectedsong = streams[selectedSongIndex]
                               binding.isAd = selectedsong.isAd
                            if(selectedsong.isAd == true){
                                binding.adDisplayGroup.visibility = View.INVISIBLE
                                binding.lyricsDisplayGroup.visibility = View.INVISIBLE
                                selectedAd = selectedsong as Ads
                                binding.adData = selectedAd
                            }


                            else if(index.toInt() > 0 && streams[selectedSongIndex -1].isAd == true){
                                var prevAd = streams[index.toInt() -1] as Ads
                                Picasso.get().load(prevAd.thumbnailPath).fit().centerCrop().into(binding.adDisplayImageview)
                                binding.adTitleTextview.text = prevAd.tittle
                                binding.advertiserNameTextview.text = prevAd.ownerName?.joinToString(",")
                                binding.adDisplayGroup.visibility = View.VISIBLE
                                binding.lyricsDisplayGroup.visibility = View.INVISIBLE
                            }
                            else {
                                binding.adDisplayGroup.visibility = View.INVISIBLE
                                if(playerState == PlayerState.PLAYLIST)
                                    binding.lyricsDisplayGroup.visibility = View.VISIBLE
                                else binding.lyricsDisplayGroup.visibility = View.INVISIBLE
                            }

                            binding.adDisplayImageview.setOnClickListener {
                                var prevAd = streams[index.toInt() -1] as Ads

                                preference[PreferenceHelper.PURCHASED_FROM] = FcmService.F_EVENT_PARAM_VALUE_PURCHASED_FROM_DIGITAL_CONTENT
                                mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.FIREBASEANALYTICS_EVENT_AD_CLICK){
                                    param(FcmService.F_EVENT_PARAM_AD_SOURCE , FcmService.F_EVENT_PARAM_VALUE_DEIGITAL_CONTENT)
                                }
                                if(prevAd.adContent != null){
                                    mainActivityViewmodel.updateProductAdClick(prevAd.adContent!!).observe(viewLifecycleOwner){}
                                    (requireActivity() as MainActivity).movetoProductDetails(prevAd.adContent!!)
                                }
                                else if(prevAd.link != null){
                                    var intent = Intent(Intent.ACTION_VIEW)
                                    intent.setData(Uri.parse(prevAd.link))
                                    startActivity(intent)
                                }
                            }


                            binding.queueSongImageViewpager.isUserInputEnabled = selectedsong.isAd == false &&
                                    subscriptionInfo == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                            if(trackSongPosition) mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it[index.toInt()] as Song<String, String>?
                            var imageUrl = selectedsong.thumbnailPath!!
                            if( mainActivityViewmodel.playerState.value is PlayerState.PlaylistState){
                                Log.i("queuelist" , playerState.toString())
//                                queueAdapter.submitList(streams as List<Song<String, String>>?)
                                binding.queueListRecyclerview.scrollToPosition(selectedSongIndex+1)
                            }
                            showPlayerbackground(imageUrl)
                        }
                    }
                }

                mdata.bundle.getString("LYRICS")?.let {
                    binding.shortLyricsTextview.text = it
                    binding.lyricsTextview.text = it

                }


                mdata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.let {
                    selectedMediaId = it
                    when(playerState){
                        PlayerState.PLAYLIST -> {
                            mainActivityViewmodel.checkSongInFavorite(it)

                            // track selected song in queue list
                            mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                                var a = it::class.qualifiedName
                                when(a){
                                    "com.komkum.komkum.data.model.Song"  ->{
                                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>}
                                }
                            }
                        }
                        PlayerState.PODCAST_EPISODE ->  mainActivityViewmodel.checkEpisodeInFavorite(it)
                    }

                }
            }
        }





        binding.songProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    mainActivityViewmodel.seekTo(it.toLong())
                }
            }
        })


        binding.adAcctionBtn.setOnClickListener {
            preference[PreferenceHelper.PURCHASED_FROM] = FcmService.F_EVENT_PARAM_VALUE_PURCHASED_FROM_DIGITAL_CONTENT
            mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.FIREBASEANALYTICS_EVENT_AD_CLICK){
                param(FcmService.F_EVENT_PARAM_AD_SOURCE , FcmService.F_EVENT_PARAM_VALUE_DEIGITAL_CONTENT)
            }
            selectedAd?.let {
                if(it.adContent != null){
                    mainActivityViewmodel.updateProductAdClick(it.adContent!!).observe(viewLifecycleOwner){}
                    (requireActivity() as MainActivity).movetoProductDetails(it.adContent!!)
                }
                else if(it.link != null){
                    var intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(it.link)
                    startActivity(intent)
                }
            }
        }


        binding.playerNextBtn.setOnClickListener {
            binding.queueSongImageViewpager.setCurrentItem(binding.queueSongImageViewpager.currentItem+1 , true)
        }

        binding.playerPrevBtn.setOnClickListener {
            if(mainActivityViewmodel.isLoadFromCache)
                binding.queueSongImageViewpager.setCurrentItem(binding.queueSongImageViewpager.currentItem-1 , true)
            else {
                var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
                if(isSubscriptionValid){
                    if(mainActivityViewmodel.playerState.value is PlayerState.RadioState){}
                    else binding.queueSongImageViewpager.setCurrentItem(binding.queueSongImageViewpager.currentItem-1 , true)
                }
                else{
                    Toast.makeText(requireContext() , "You need to be a premium member to access this feature" , Toast.LENGTH_LONG).show()
                }
            }
        }



        binding.rewindImageview.setOnClickListener {
            mainActivityViewmodel.rewind()
        }

        binding.forwardImageview.setOnClickListener {
            mainActivityViewmodel.forward()
        }

        binding.bookPlayerPrev.setOnClickListener {
            binding.queueSongImageViewpager.setCurrentItem(binding.queueSongImageViewpager.currentItem-1 , true)
        }

        binding.bookPlayerNext.setOnClickListener {
            binding.queueSongImageViewpager.setCurrentItem(binding.queueSongImageViewpager.currentItem + 1 , true)
        }

        binding.imageView13.setOnClickListener { findNavController().navigateUp() }


        binding.removeSongImageView.setOnClickListener {

            var userId = preference.getString(AccountState.USER_ID , "")
            if(!userId.isNullOrBlank()){
                mainActivityViewmodel.playlistQueue.value?.let {
                var index = selectedSongIndex
                if(index == it.size -1) return@setOnClickListener

                var currentPlaylist =  it.toMutableList()
                var removedSong = currentPlaylist?.removeAt(index) as Song<String,String>
                mainActivityViewmodel.removeQueueItem(index)
                mainActivityViewmodel.showPlayerCard = false
                mainActivityViewmodel.playlistQueue.value = currentPlaylist
                if(mainActivityViewmodel.playlistQueue.value!!.size-1 > binding.queueSongImageViewpager.currentItem)
                    binding.queueSongImageViewpager.setCurrentItem(binding.queueSongImageViewpager.currentItem + 1 , true)

//                mainActivityViewmodel.playlistQueue.value = currentPlaylist
//                mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_MEDIA)
//                mainActivityViewmodel.play(index.toLong())

                mainActivityViewmodel.selectedRadio?.let {
                    var radioUpdateInfo  =  UserRadioInfo(it._id!! , unlikedTag = removedSong.tags)
                    mainActivityViewmodel.selectedRadio?.songs = currentPlaylist as MutableList<Song<String,String>>
                    mainActivityViewmodel.updateRadioStation(radioUpdateInfo).handleSingleDataObservation(viewLifecycleOwner){
                        (requireActivity() as MainActivity).showSnacbar("Station updated based on your preference")
                    }
                   return@setOnClickListener
                }
                Toast.makeText(requireContext() , "Like the station first to tune the station" , Toast.LENGTH_LONG).show()
            }
            }
            else requireActivity().sendIntent(OnboardingActivity::class.java)
        }

        binding.likeSongImageview.setOnClickListener { selectedMediaId?.let {mediaId ->
            var userId = preference.getString(AccountState.USER_ID , "")
            if(!userId.isNullOrBlank()){
                binding.playerLoadingProgressbar.isVisible = true
                mainActivityViewmodel.addToFavoriteList(
                    LibraryService.CONTENT_TYPE_SONG,
                    listOf(mediaId)
                ).observe(viewLifecycleOwner){
                    if (mainActivityViewmodel.playerState.value is PlayerState.RadioState) {
                        var selectedSong = mainActivityViewmodel.getSelectedSongInfo(mediaId) as Song<String, String>
                        mainActivityViewmodel.selectedRadio?.let { radio ->
                            var radioUpdateInfo =
                                UserRadioInfo(radio._id!!, likedTag = selectedSong.tags)
                            mainActivityViewmodel.updateRadioStation(radioUpdateInfo)
                                .handleSingleDataObservation(viewLifecycleOwner) {
                                    (requireActivity() as MainActivity).showSnacbar("Station updated based on your preference")
                                }
                        }
                    }
                    else
                        Snackbar.make(binding.playerContainer, "Song added to Favorites", Snackbar.LENGTH_SHORT).show()
                    binding.playerLoadingProgressbar.isVisible = false
                }
            }
            else requireActivity().sendIntent(OnboardingActivity::class.java)

        }}

        binding.unlikeSongImageview.setOnClickListener {
            selectedMediaId?.let {
                mainActivityViewmodel.removeFromFavorite(LibraryService.CONTENT_TYPE_SONG, listOf(it))
                    Snackbar.make(binding.playerContainer, "Song removed from Favorites", Snackbar.LENGTH_SHORT).show()
            }
        }


        binding.queueSongImageViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Log.i("scrolled" , "$positionOffset")

            }
            override fun onPageSelected(position: Int) {
                if (position > selectedSongIndex) {
                    selectedSongIndex = position
                    mainActivityViewmodel.next()
                } else if (position < selectedSongIndex) {
                    Log.i("scrolledleft" , "$position")

                    selectedSongIndex = position
                    mainActivityViewmodel.prev()
                }
            }
        })



//        binding.addSongImageview.setOnClickListener {
//            selectedSongIndex?.let { it1 ->
//                var song = mainActivityViewmodel.playlistQueue.value!![it1]
//                var data = BaseModel(baseId = song._id, baseTittle = song.album, baseSubTittle = song.tittle, baseImagePath = song.thumbnailPath, baseDescription = song.mpdPath, baseListOfInfo = song.artists)
//                (requireActivity() as MainActivity).moveToAddtoFragment(listOf(data), LibraryService.CONTENT_TYPE_SONG)
//            }
//        }

//        requireActivity().onBackPressedDispatcher.addCallback {
//            var activity = requireActivity() as MainActivity
//            activity.findNavController(R.id.nav_host_fragment).popBackStack()
//        }
        binding.playerMoreOptionImageview.setOnClickListener {
            mainActivityViewmodel.playerState.value?.let {
                selectedMenuList = if (it is PlayerState.PodcastState) {
                        var menuITem = requireContext().podcastEpisodeMenuItem.toMutableList()
                        if (mainActivityViewmodel.isEpisodeInFavorite.value == true) {
                            menuITem.removeIf { item -> item.order == MenuItem.LIKE_EPISODE }
                            menuITem.add(
                                1,
                                MenuItem(
                                    R.drawable.ic_baseline_favorite_24,
                                    this.getString(R.string.remove_from_favorite),
                                    MenuItem.UNLIKE_EPISODE
                                )
                            )
                            menuITem.toMutableSet()
                        } else {
                            menuITem.removeIf { item -> item.order == MenuItem.UNLIKE_EPISODE }
                            menuITem.add(
                                1,
                                MenuItem(
                                    R.drawable.ic_round_favorite_border_24,
                                    this.getString(R.string.add_to_favorite),
                                    MenuItem.LIKE_EPISODE
                                ),
                            )
                            menuITem.toMutableSet()
                        }
                }
                else if(mainActivityViewmodel.isLoadFromCache){
                    var menuItem= requireContext().musicMenuItem.toMutableList()
//                    menuItem.removeAt(0)
                    //temporary code
                    menuItem.clear()
                    menuItem.add(MenuItem(R.drawable.ic_baseline_info_24 , this.getString(R.string.song_info) ,
                        MenuItem.SONG_INFO
                    ))
                    menuItem.toMutableSet()
                }
                else requireContext().musicMenuItem
            }
            // show options dialog
            var dialog =  MaterialDialog(binding.root.context, BottomSheet(LayoutMode.WRAP_CONTENT))
                . cancelOnTouchOutside(true)
                .cornerRadius(literalDp = 16f)
                .customView(R.layout.custom_menu_layout , scrollable = true)

            var interactionListener = object : IRecyclerViewInteractionListener<MenuItem>{
                override fun onItemClick(data: MenuItem, position: Int, option: Int?) {
                    when (data.order) {
                        MenuItem.DOWNLOAD_SONG -> {
                            if(mainActivityViewmodel.userRepo.userManager.hasValidSubscription()){
                                mainActivityViewmodel.downloadSong(mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String, String> , viewLifecycleOwner)
                                (requireActivity() as MainActivity).showSnacbar("Song Added to Downloads" , "View"){
                                    findNavController().navigate(R.id.downloadListFragment)
                                }
                            }
                            else findNavController().navigate(R.id.subscriptionFragment2)

                        }
                        MenuItem.SONG_RADIO ->{
                            var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String , String>
                            (requireActivity() as MainActivity).movetoSingleRadioStation(songId = song._id)
                        }
                        MenuItem.ARTIST_RADIO -> {
                            var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String , String>
                            if(song.artists!!.size > 1){
                                MaterialDialog(requireContext()).show {
                                    listItems(items = song.artists){dialog, index, text ->
                                        (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = song.artists!![index])
                                    }
                                }
                            }
                            else (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = song.artists!![0])
                        }
                        MenuItem.GO_TO_ARTIST ->{
//                            (requireActivity() as MainActivity).closeBottomSheet()
                            var selectedSong = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String,String>
                            if(selectedSong.artists!!.size > 1){
                                MaterialDialog(requireContext()).show {
                                    title(text = "Choose Artist")
                                    listItems(items = selectedSong.artistsName){dialog, index, text ->
                                        (requireActivity() as MainActivity).moveToArtist(selectedSong.artists!![index])
                                    }
                                }
                            }
                            else selectedSong.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }
                        }
                        MenuItem.GO_TO_ALBUM->{
                            var selectedSong = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String,String>
                            selectedSong.album?.let {
//                                (requireActivity() as MainActivity).closeBottomSheet()
                                (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(it , false , false)
                            }
                        }
                        MenuItem.SONG_INFO ->{
                            var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String, String>
                            mainActivityViewmodel.showSongOption(song)
                        }

                        MenuItem.ADD_TO -> {
                            var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String, String>
                            Log.i("songmy" , song.toString())
                            mainActivityViewmodel.showPlayerCard = false
                            (requireActivity() as MainActivity).moveToAddtoFragment(listOf(song._id), LibraryService.CONTENT_TYPE_SONG , mutableListOf(song))
                        }

                        MenuItem.DOWNLOAD_EPISODE -> {
                            var selectedEpisode = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as PodcastEpisode
                            podcastViewmodel.downloadEpisode(selectedEpisode).observe(viewLifecycleOwner){
                                if(it == true){
                                    (requireActivity() as MainActivity).showSnacbar("Episode added to download" , "View"){
                                        var bundle = bundleOf("SELECTED_PAGE" to 3)
                                        findNavController().navigate(R.id.downloadListFragment , bundle)
                                    }
                                }
                            }
                        }
                        MenuItem.LIKE_EPISODE ->{
                            var selectedEpisode = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as PodcastEpisode
                            podcastViewmodel.followEpisode(selectedEpisode._id).observe(viewLifecycleOwner){
                                if(it == true){
                                    var a = selectedMenuList.removeIf { it.order == MenuItem.LIKE_EPISODE }
                                    var options = selectedMenuList.toMutableList()
                                    options.add(1,MenuItem(R.drawable.ic_baseline_favorite_24, "Remove from favorite" , MenuItem.UNLIKE_EPISODE))
                                    selectedMenuList = options.toMutableSet()
                                    (requireActivity() as MainActivity).showSnacbar("Episode added to Library")
                                    mainActivityViewmodel.isEpisodeInFavorite.value = true
                                }
                                else Toast.makeText(requireContext() , "Error occured" , Toast.LENGTH_LONG).show()
                            }
                        }
                        MenuItem.UNLIKE_EPISODE ->{
                            var selectedEpisode = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as PodcastEpisode
                            podcastViewmodel.unfollowEpisode(selectedEpisode._id).observe(viewLifecycleOwner){
                                if(it == true){
                                    selectedMenuList.removeIf { it.order == MenuItem.UNLIKE_EPISODE }
                                    var options = selectedMenuList.toMutableList()
                                    options.add(1 ,MenuItem(R.drawable.ic_round_favorite_border_24, "Add to favorite" , MenuItem.LIKE_EPISODE),)
                                    selectedMenuList = options.toMutableSet()
                                    (requireActivity() as MainActivity).showSnacbar("Episode Removed from Library")
                                    mainActivityViewmodel.isEpisodeInFavorite.value = false
                                }
                                else Toast.makeText(requireContext() , "Error occured" , Toast.LENGTH_LONG).show()
                            }
                        }
                        MenuItem.GO_TO_EPISODE ->{
                            var selectedEpisode = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as PodcastEpisode
                            (requireActivity() as MainActivity).moveToEpisode(selectedEpisode , false)
                        }
                        MenuItem.GO_TO_PODCAST ->{
                            var selectedEpisode = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as PodcastEpisode
                            selectedEpisode?.let{(requireActivity() as MainActivity).moveToPodcast(it.podcast!!)}
                        }
                    }
                    dialog.dismiss()
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}

            }

            var reyclerviewHelper = RecyclerViewHelper("MENU" , interactionListener = interactionListener)

            var customview =  dialog.getCustomView()
            Picasso.get().load(imagelist[selectedSongIndex]).placeholder(R.drawable.music_placeholder).fit().centerCrop().into(customview.findViewById<ImageView>(R.id.menu_imageview))
            customview.findViewById<TextView>(R.id.menu_title_textview).text = playerTitle
            customview.findViewById<TextView>(R.id.menu_sub_title_textview).text = playerSubtittle
            customview.findViewById<RecyclerView>(R.id.menu_recyclerview).layoutManager = LinearLayoutManager(requireContext())
            customview.findViewById<RecyclerView>(R.id.menu_recyclerview).adapter = MenuAdapter(reyclerviewHelper , selectedMenuList.toMutableList())

            dialog.show()

        }

        podcastViewmodel.getError().observe(viewLifecycleOwner){}
        podcastViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            this@PlayerFragment.activity?.window?.statusBarColor = resources.getColor(R.color.light_secondaryDarkColor)
        }
        super.onDestroyView()
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {}
    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {
//        mainActivityViewmodel.removeQueueItem(position)
//
//        trackSongPosition = true
//        isQueueMovied = true
//        var prevList =  mainActivityViewmodel.playlistQueue.value?.removeAt(position)
//
//        mainActivityViewmodel.showPlayerCard = false
        var newImageList =  mainActivityViewmodel.playlistQueue.value?.map { song -> song.thumbnailPath!! }

//        (binding.queueListRecyclerview.adapter as  SongAdapter).submitList(mainActivityViewmodel.playlistQueue.value as? List<Song<String ,String>>)
//        (binding.queueSongImageViewpager.adapter as QueueListAdapter).submitList(newImageList)
//        mainActivityViewmodel.playlistQueue.value = prevList

    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
//        Log.i("moved" , prevPosition.toString() +"   "+ newPosition.toString())
//        mainActivityViewmodel.moveQueueItem(prevPosition , newPosition)
//        mainActivityViewmodel.showPlayerCard = false
//
//        var list = mainActivityViewmodel.playlistQueue.value
//        var deletedItem = list?.removeAt(prevPosition)
//        list?.add(newPosition , deletedItem!!)
//         isQueueMovied = true
//        mainActivityViewmodel.playlistQueue.value = list
    }

    override fun onDetach() {
        (requireActivity() as MainActivity).showBottomViewBeta()
        super.onDetach()
    }


    fun showPlayerbackground(imageUrl : String){
        var target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    Palette.from(it).generate {
                        it?.mutedSwatch?.let {
                            binding.playerContainer.setBackgroundColor(it.rgb)
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
