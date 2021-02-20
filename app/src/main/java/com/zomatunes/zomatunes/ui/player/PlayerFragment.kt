package com.zomatunes.zomatunes.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import androidx.annotation.Px
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.getRecyclerView
import com.afollestad.materialdialogs.list.listItems
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.MainActivityViewmodel

import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.databinding.FragmentPlayerBinding
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.MenuAdapter
import com.zomatunes.zomatunes.util.extensions.exhaustive
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.extensions.toSongList
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.ItemTouchHelperCallback
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import android.view.MenuItem as MenuItem1

@AndroidEntryPoint
class PlayerFragment : Fragment() , IRecyclerViewInteractionListener<Song<String, String>> {

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

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
    var inflateQuelist = true

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
                        }

                        mainActivityViewmodel.queueitem.observe(viewLifecycleOwner , Observer { queue ->
                            queue?.let {
                                mainActivityViewmodel.playlistQueue.value =  it.toSongList()
                            }
                        })
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
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
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

        this.toControllerActivity().hideBottomView()


        mainActivityViewmodel.playerState.observe(viewLifecycleOwner , Observer { state ->
            when(state){
                is PlayerState.PlaylistState ->{
                    binding.queueSongImageViewpager.isEnabled = true
                    binding.queueSongImageViewpager.isUserInputEnabled = true
                    binding.forwardImageview.visibility = View.INVISIBLE
                    binding.rewindImageview.visibility = View.INVISIBLE
                    binding.removeSongImageView.visibility = View.INVISIBLE
                    binding.lyricsContainer.visibility = View.VISIBLE

                    binding.playerNextBtn.visibility = View.VISIBLE
                    binding.playerPrevBtn.visibility = View.VISIBLE
                    binding.queueListImageView.visibility = View.VISIBLE

                    binding.bookPlayerNext.visibility = View.INVISIBLE
                    binding.bookPlayerPrev.visibility = View.INVISIBLE

                    mainActivityViewmodel.isFavoriteSong.observe(viewLifecycleOwner , Observer{
                        if(it){
                            binding.likeSongImageview.visibility = View.INVISIBLE
                            binding.unlikeSongImageview.visibility = View.VISIBLE
                        }
                        else{
                            binding.likeSongImageview.visibility = View.VISIBLE
                            binding.unlikeSongImageview.visibility = View.INVISIBLE
                        }
                    })

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
                is PlayerState.RadioState -> {
                    binding.queueSongImageViewpager.isEnabled = false
                    binding.queueSongImageViewpager.isUserInputEnabled = false
                    binding.songProgressBar.isEnabled = false
                    binding.songProgressBar.isClickable = false
                    binding.rewindImageview.visibility = View.VISIBLE
                    binding.playerPrevBtn.visibility = View.INVISIBLE

                    binding.playerNextBtn.visibility = View.VISIBLE
                    binding.forwardImageview.visibility = View.INVISIBLE

                    binding.removeSongImageView.visibility = View.VISIBLE
                    binding.queueListImageView.visibility = View.INVISIBLE

                    binding.bookPlayerNext.visibility = View.INVISIBLE
                    binding.bookPlayerPrev.visibility = View.INVISIBLE

                    mainActivityViewmodel.isFavoriteSong.observe(viewLifecycleOwner , Observer{
                        if(it){
                            binding.likeSongImageview.visibility = View.INVISIBLE
                            binding.unlikeSongImageview.visibility = View.VISIBLE
                        }
                        else{
                            binding.likeSongImageview.visibility = View.VISIBLE
                            binding.unlikeSongImageview.visibility = View.INVISIBLE
                        }
                    })
                }
                is PlayerState.DownloadState -> {
                    binding.playerMoreOptionImageview.visibility = View.INVISIBLE
                }
                is PlayerState.AudiobookState -> {
                    binding.bookPlayerNext.visibility = View.VISIBLE
                    binding.bookPlayerPrev.visibility = View.VISIBLE
                    binding.forwardImageview.visibility = View.VISIBLE
                    binding.rewindImageview.visibility = View.VISIBLE
                    binding.button5.visibility = View.INVISIBLE
                    binding.shortLyricsTextview.visibility = View.INVISIBLE
                    binding.divider10.visibility = View.INVISIBLE
                    binding.playerMoreOptionImageview.visibility = View.INVISIBLE

                    binding.removeSongImageView.visibility = View.INVISIBLE
                    binding.queueListImageView.visibility = View.INVISIBLE
                    binding.playerPrevBtn.visibility = View.INVISIBLE
                    binding.playerNextBtn.visibility = View.INVISIBLE
                    binding.likeSongImageview.visibility = View.INVISIBLE
                    binding.unlikeSongImageview.visibility = View.INVISIBLE
                }
                is PlayerState.PodcastState -> {}
            }.exhaustive
        })


        mainActivityViewmodel.playedFrom.observe(viewLifecycleOwner , Observer{
            binding.nowPlayingTextview.text = it
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            var progress = binding.playerContainer.progress
            if(progress == 1.0f) binding.playerContainer.progress = 0.0f
            else findNavController().navigateUp()
        }



        mainActivityViewmodel.playlistQueue.observe(viewLifecycleOwner, Observer {
            imagelist = it.map { song -> song.thumbnailPath!! }
            var queueViewpagerAdapter = QueueListAdapter(imagelist)

            binding.queueSongImageViewpager.setPageTransformer( MarginPageTransformer(200))
            binding.queueSongImageViewpager.adapter = queueViewpagerAdapter
            var firstItem = it.firstOrNull()
            if (firstItem != null) {
                when(firstItem::class){
                    Song::class ->{
                        Log.i("iscalled" , "yes")
                       if(mainActivityViewmodel.playerState.value is PlayerState.PlaylistState) queueAdapter.submitList(it as List<Song<String, String>>?)
                    }
                }
            }
        })


        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
            metadata?.let {mdata ->
                playerTitle = mdata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                playerSubtittle = mdata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

                mdata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)?.let { index ->
                    mainActivityViewmodel.playlistSelectedIndex.value = index.toInt()
                    Log.i("selectindex1" , index)
                    mainActivityViewmodel.playlistQueue.value?.let {
                        if(it.isNotEmpty() && mainActivityViewmodel.playerState.value is PlayerState.PlaylistState){
                            Log.i("queuelist" , playerState.toString())
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it[index.toInt()] as Song<String, String>?
                            binding.queueListRecyclerview.scrollToPosition(index.toInt()+1)
                        }
                    }


                    if(index.toInt() >= 0){
                        selectedSongIndex = index.toInt()
                        binding.queueSongImageViewpager.setCurrentItem(selectedSongIndex , false)

                    }
                    if (!mainActivityViewmodel.playlistQueue.value.isNullOrEmpty() &&  (mainActivityViewmodel.playlistQueue.value!!.size > selectedSongIndex)) {
                        var selectedsong = mainActivityViewmodel.playlistQueue.value!![selectedSongIndex]
                        var imageUrl = selectedsong.thumbnailPath!!.replace("localhost", AdapterDiffUtil.URL)
                        showPlayerbackground(imageUrl)
                    }
                }

                mdata.bundle.getString("LYRICS")?.let {
                    binding.shortLyricsTextview.text = it
                    binding.lyricsTextview.text = it

                }

                mdata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.let {
                    selectedMediaId = it
                    mainActivityViewmodel.isSongInFavorite(it)
                }
            }

        })


//        mainActivityViewmodel.playerState.value?.let {
//            if(it is (PlayerState.PlaylistState) || (it is PlayerState.RadioState)){
//                mainActivityViewmodel.isFavoriteSong.observe(viewLifecycleOwner , Observer{
//                    if(it){
//                        binding.likeSongImageview.visibility = View.INVISIBLE
//                        binding.unlikeSongImageview.visibility = View.VISIBLE
//                    }
//                    else{
//                        binding.likeSongImageview.visibility = View.VISIBLE
//                        binding.unlikeSongImageview.visibility = View.INVISIBLE
//                    }
//                })
//            }
//        }



        binding.songProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let {
                    mainActivityViewmodel.seekTo(it.toLong())
                }
            }
        })



        binding.playerNextBtn.setOnClickListener {
            binding.queueSongImageViewpager.currentItem += 1
        }

        binding.playerPrevBtn.setOnClickListener {
            if(mainActivityViewmodel.playerState.value is PlayerState.RadioState){}
            else binding.queueSongImageViewpager.currentItem -= 1
        }

        binding.rewindImageview.setOnClickListener {
            mainActivityViewmodel.rewind()
        }

        binding.forwardImageview.setOnClickListener {
            mainActivityViewmodel.forward()
        }

        binding.bookPlayerPrev.setOnClickListener {
            binding.queueSongImageViewpager.currentItem -= 1
        }

        binding.bookPlayerNext.setOnClickListener {
            binding.queueSongImageViewpager.currentItem += 1
        }

        binding.removeSongImageView.setOnClickListener {
            mainActivityViewmodel.playlistQueue.value?.let {
                var index = selectedSongIndex
                if(index == it.size -1) return@setOnClickListener

                var currentPlaylist =  it.toMutableList()
                var removedSong = currentPlaylist?.removeAt(index) as Song<String,String>
                mainActivityViewmodel.removeQueueItem(index)
                mainActivityViewmodel.showPlayerCard = false
                mainActivityViewmodel.playlistQueue.value = currentPlaylist
                if(mainActivityViewmodel.playlistQueue.value!!.size-1 > binding.queueSongImageViewpager.currentItem)
                    binding.queueSongImageViewpager.currentItem += 1

//                mainActivityViewmodel.playlistQueue.value = currentPlaylist
//                mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_MEDIA)
//                mainActivityViewmodel.play(index.toLong())

                mainActivityViewmodel.selectedRadio?.let {
                    var radioUpdateInfo  =  UserRadioInfo(it._id!! , unlikedTag = removedSong.tags)
                    mainActivityViewmodel.selectedRadio?.songs = currentPlaylist as MutableList<Song<String,String>>
                    mainActivityViewmodel.updateRadioStation(radioUpdateInfo).handleSingleDataObservation(viewLifecycleOwner){
                        (requireActivity() as MainActivity).showSnacbar("Station updated based on your preference")
                    }
                }

            }
        }

        binding.likeSongImageview.setOnClickListener { selectedMediaId?.let {
            mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG, listOf(it))
            if(mainActivityViewmodel.playerState.value is PlayerState.RadioState){
                var selectedSong = mainActivityViewmodel.getSelectedSongInfo(it) as Song<String , String>
                mainActivityViewmodel.selectedRadio?.let { radio ->
                    var radioUpdateInfo  =  UserRadioInfo(radio._id!! , likedTag = selectedSong.tags)
                    mainActivityViewmodel.updateRadioStation(radioUpdateInfo).handleSingleDataObservation(viewLifecycleOwner){
                        (requireActivity() as MainActivity).showSnacbar("Station updated based on your preference")
                    }
                }
            }
            else Snackbar.make(binding.playerContainer ,  "Song added to Favorites", Snackbar.LENGTH_SHORT).show()
        }}

        binding.unlikeSongImageview.setOnClickListener {
            selectedMediaId?.let {
                mainActivityViewmodel.removeFromFavorite(LibraryService.CONTENT_TYPE_SONG, listOf(it))
                    Snackbar.make(binding.playerContainer, "Song removed from Favorites", Snackbar.LENGTH_SHORT).show()
            }
        }


        binding.queueSongImageViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if (position > selectedSongIndex) {
                    selectedSongIndex = position
                    mainActivityViewmodel.next()
                } else if (position < selectedSongIndex) {
                    selectedSongIndex = position
                    mainActivityViewmodel.prev()
                }
                else{

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

        binding.nowPlayingTextview.setOnClickListener{
            findNavController().navigate(R.id.queueListFragment)
        }





        binding.playerMoreOptionImageview.setOnClickListener {
            if(!mainActivityViewmodel.isLoadFromCache){
                var dialog =  MaterialDialog(binding.root.context, BottomSheet(LayoutMode.WRAP_CONTENT))
                    . cancelOnTouchOutside(true)
                    .cornerRadius(literalDp = 14f)

                    .customView(R.layout.custom_menu_layout , scrollable = true)

                var interactionListener = object : IRecyclerViewInteractionListener<MenuItem>{
                    override fun onItemClick(data: MenuItem, position: Int, option: Int?) {
                        when (position) {
                            0 -> {
                                mainActivityViewmodel.downloadSong(mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String, String> , viewLifecycleOwner)
                                (requireActivity() as MainActivity).showSnacbar("Song Added to Downloads" , "View"){
                                    findNavController().navigate(R.id.downloadListFragment)
                                }
                            }
                            1 ->{
                                var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String , String>
                                (requireActivity() as MainActivity).movetoSingleRadioStation(songId = song._id)
                            }
                            2 -> {
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
                            4 ->{
//                            (requireActivity() as MainActivity).closeBottomSheet()
                                var selectedSong = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String,String>
                                if(selectedSong.artists!!.size > 1){
                                    MaterialDialog(requireContext()).show {
                                        listItems(items = selectedSong.artists){dialog, index, text ->
                                            (requireActivity() as MainActivity).moveToArtist(selectedSong.artists!![index])
                                        }
                                    }
                                }
                                else selectedSong.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }
                            }
                            3->{
                                var selectedSong = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String,String>
                                selectedSong.album?.let {
//                                (requireActivity() as MainActivity).closeBottomSheet()
                                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(it , false , false)
                                }
                            }
                            5 ->{
                                var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex) as Song<String, String>
                                var info  = song.songCredit?.map { entry -> "${entry.key}  :  ${entry.value}" }?.toMutableList()
                                var album = if(song.album != null) "Album - ${song.album}" else null
                                var a = listOf("title -  ${song.tittle}" , "artist - ${song.artistsName?.joinToString(", ")}",
                                    album  , "Genre - ${song.genre}" , "Release date - ${song.dateCreated.toString()}", "Language - ${song.language?.joinToString(", ")}",
                                    "${song.monthlyStreamCount} Monthly Stream | ${song.monthlyDownloadCount} Monthly Download").toMutableList()
                                info?.let { a.addAll(it) }
                                a.let {
                                    MaterialDialog(requireContext()).show {
                                        cornerRadius(literalDp = 10f)
                                        title(text = "Song Info")
                                        listItems(items = it as List<CharSequence>)
                                    }
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                    override fun activiateMultiSelectionMode() {}
                    override fun onSwiped(position: Int) {}
                    override fun onMoved(prevPosition: Int, newPosition: Int) {}

                }

                var reyclerviewHelper = RecyclerViewHelper<MenuItem>("MENU" , interactionListener = interactionListener)

                var customview =  dialog.getCustomView()
                Picasso.get().load(imagelist[selectedSongIndex].replace("localhost" , AdapterDiffUtil.URL)).placeholder(R.drawable.backimg).fit().centerCrop().into(customview.findViewById<ImageView>(R.id.menu_imageview))
                customview.findViewById<TextView>(R.id.menu_title_textview).text = playerTitle
                customview.findViewById<TextView>(R.id.menu_sub_title_textview).text = playerSubtittle
                customview.findViewById<RecyclerView>(R.id.menu_recyclerview).layoutManager = LinearLayoutManager(requireContext())
                customview.findViewById<RecyclerView>(R.id.menu_recyclerview).adapter = MenuAdapter(reyclerviewHelper    , MenuItem.onlineMenuItem)

                dialog.show()
            }
        }
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {}
    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {
        var newQueueList = mainActivityViewmodel.playlistQueue.value!!.toMutableList()
        var a = newQueueList.removeAt(position)
        mainActivityViewmodel.removeQueueItem(position)
        mainActivityViewmodel.showPlayerCard = false
        inflateQuelist = false
        mainActivityViewmodel.playlistQueue.value = newQueueList

    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        mainActivityViewmodel.playlistQueue.value?.toMutableList()?.let{
            var item =  it.removeAt(prevPosition)
            it.add(newPosition , item)
            mainActivityViewmodel.moveQueueItem(prevPosition , newPosition)
            mainActivityViewmodel.showPlayerCard = false
            inflateQuelist = false
            mainActivityViewmodel.playlistQueue.value = it

        }
    }

    override fun onDetach() {
        this.toControllerActivity().showBottomView()
        super.onDetach()
    }


    fun showPlayerbackground(imageUrl : String){
        var target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    Palette.from(it).generate {
                        it?.darkMutedSwatch?.let {
                            binding.playerContainer.setBackgroundColor(it.rgb)
                            requireActivity().window.statusBarColor = it.rgb
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
