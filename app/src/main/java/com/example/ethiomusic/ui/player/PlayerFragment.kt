package com.example.ethiomusic.ui.player

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.list.listItems
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.MainActivityViewmodel

import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.DownloadRepository
import com.example.ethiomusic.databinding.FragmentPlayerBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.exhaustive
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class PlayerFragment : Fragment() {

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    @Inject  lateinit var downloadrepo : DownloadRepository

    lateinit var binding : FragmentPlayerBinding

    var selectedMediaId : String? = null
    var selectedSongIndex : Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(false)
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

                    binding.playerNextBtn.visibility = View.VISIBLE
                    binding.playerPrevBtn.visibility = View.VISIBLE
                    binding.repeatImageview.visibility = View.VISIBLE

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
                    binding.repeatImageview.visibility = View.INVISIBLE
                }
                is PlayerState.DownloadState -> {}
                is PlayerState.PodcastState -> {}
            }.exhaustive
        })


        mainActivityViewmodel.playlistQueue.observe(viewLifecycleOwner, Observer {
            var imagelist = it.map { song -> song.thumbnailPath!! }
            var queueAdapter = QueueListAdapter(imagelist)
            binding.queueSongImageViewpager.adapter = queueAdapter
        })


        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->

            metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)?.let {
                mainActivityViewmodel.playlistSelectedIndex.value = it.toInt()
                if(it.toInt() >= 0){
                    selectedSongIndex = it.toInt()
                    binding.queueSongImageViewpager.setCurrentItem(it.toInt() , false)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    if (!mainActivityViewmodel.playlistQueue.value.isNullOrEmpty()) {
                        var selectedsong = mainActivityViewmodel.playlistQueue.value!![selectedSongIndex]
                        var imageUrl = selectedsong.thumbnailPath!!.replace("localhost", "192.168.43.166")
                        var url = URL(imageUrl)
                        try {
                            var bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            Palette.from(bitmap).generate {
                                it?.darkMutedSwatch?.let {
                                    var grDrawable = GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        intArrayOf(it.rgb , it.rgb)
                                    )
                                    binding.playerContainer.setBackgroundColor(it.rgb)
                                    requireActivity().window.statusBarColor = it.rgb
                                }
                            }
                        } catch (ex: Exception) {

                        }
                    }
                }
            }

            metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.let {
                selectedMediaId = it
                mainActivityViewmodel.isSongInFavorite(it)
            }
        })



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
            binding.queueSongImageViewpager.currentItem -= 1
        }

        binding.rewindImageview.setOnClickListener {
            mainActivityViewmodel.rewind()
        }

        binding.removeSongImageView.setOnClickListener {
            mainActivityViewmodel.playlistQueue.value?.let {
                var index = selectedSongIndex
                if(index == it.size -1) return@setOnClickListener

                var currentPlaylist =  it.toMutableList()
                currentPlaylist?.removeAt(index)
                mainActivityViewmodel.playlistQueue.value = currentPlaylist
                mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_MEDIA_TYPE)
                mainActivityViewmodel.play(index.toLong())
                mainActivityViewmodel.selectedRadio?.songs = currentPlaylist
                mainActivityViewmodel.updateRadioStation()?.handleSingleDataObservation(viewLifecycleOwner){}
            }


        }

        binding.likeSongImageview.setOnClickListener { selectedMediaId?.let {
            mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG, listOf(it))
                    Snackbar.make(binding.playerContainer ,  "Song added to Favorites", Snackbar.LENGTH_SHORT).show()
                    }
        }
        binding.unlikeSongImageview.setOnClickListener {
            selectedMediaId?.let {
                mainActivityViewmodel.removeFromFavorite(LibraryService.CONTENT_TYPE_SONG, listOf(it))
                    Snackbar.make(binding.playerContainer, "Song removed from Favorites", Snackbar.LENGTH_SHORT).show()
            }
        }


        queue_song_image_viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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





        binding.playerMoreOptionsImageview.setOnClickListener {
            MaterialDialog(binding.root.context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                cancelOnTouchOutside(true)
                cornerRadius(literalDp = 14f)
                var gridItem = listOf(
                    BasicGridItem(R.drawable.ic_baseline_queue_music_24 , "Download"),
                    BasicGridItem(R.drawable.ic_baseline_person_pin_24 , "Artist Radio"),
                    BasicGridItem(R.drawable.ic_baseline_queue_music_24 , "Song Radio"),
                    BasicGridItem(R.drawable.ic_baseline_person_pin_24 , " Artists"),
                    BasicGridItem(R.drawable.ic_baseline_queue_music_24 , "Album"),
                    BasicGridItem(R.drawable.ic_baseline_person_pin_24 , "Song Info")
                )
                gridItems(items = gridItem){dialog, index, item ->
                    when (index) {
                        0 -> {
                            Snackbar.make(binding.playerContainer , "Album removed from Favorites" , Snackbar.LENGTH_SHORT).show()
                            mainActivityViewmodel.downloadSong(mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex))
                        }
                        3 ->{
//                            (requireActivity() as MainActivity).closeBottomSheet()
                            (requireActivity() as MainActivity).moveToArtist(mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex).artists!![0])
                        }
                        4->{
                            mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex).album?.let {
//                                (requireActivity() as MainActivity).closeBottomSheet()
                                (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(it , false , false)
                            }
                        }
                        5 ->{
                            var song = mainActivityViewmodel.getSelectedSongInfo(selectedSongIndex)
                            var info  = song.songCredit?.map { entry -> "${entry.key}  :  ${entry.value}" }?.toMutableList()
                            var a = listOf(song.tags.toString() , song.genre.toString()).toMutableList()
                          info?.let { a.addAll(it) }
                            a.let {
                                MaterialDialog(requireContext()).show {
                                    cornerRadius(literalDp = 10f)
                                    title(text = song.tittle)
                                    listItems(items = it)
                                }
                            }
                        }
                    }
                }

            }

        }


    }

    override fun onDetach() {
        this.toControllerActivity().showBottomView()
        super.onDetach()
    }
}
