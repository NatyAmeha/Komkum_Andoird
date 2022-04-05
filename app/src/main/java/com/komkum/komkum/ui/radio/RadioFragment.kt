package com.komkum.komkum.ui.radio

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.databinding.RadioFragmentBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RadioSpan
import com.google.android.material.appbar.AppBarLayout
import com.komkum.komkum.data.model.*
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.playlist.PlaylistViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.handleError
import dagger.hilt.android.AndroidEntryPoint
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans.custom


@AndroidEntryPoint
class RadioFragment : Fragment() {

    val radioViewmodel : RadioViewModel by viewModels()
    val playlistViewmodel : PlaylistViewModel by viewModels()

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()
    var isRadiohasSong = true

    lateinit var binding : RadioFragmentBinding
    var genre : String? = null
    var radioResult : Radio? = null
    var isRadioLiked = MutableLiveData<Boolean>()
    var songId : String? = null
    var artistId : String? = null

    var originalRadioSongSize : Int? = null
    var isRadioPlayed = false



    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
//            genre = it.getString("RADIO_GENRE")
               var radioData = it.getParcelable<Radio>("RADIO_DATA")
                songId = it.getString("SONG_ID")
                artistId = it.getString("ARTIST_ID")

                songId?.let {
                    radioResult =  radioViewmodel.getSongRadio(it)
                    radioViewmodel.radioStation.value = radioResult
                    radioResult?.let {
                        isRadioLiked.value = radioViewmodel.isRadioStationLiked(it._id ?: "simpleradioid")
                    }
                }

                artistId?.let {
                    Log.i("artistradio", it)
                    radioResult =  radioViewmodel.getArtistRadio(it)
                    radioViewmodel.radioStation.value = radioResult
                    radioResult?.let {
                        isRadioLiked.value = radioViewmodel.isRadioStationLiked(it._id ?: "simpleradioid")
                    }
                }

                radioData?.let {
                   radioResult =  radioViewmodel.getRadioStationResult(it._id!!)
                    radioViewmodel.radioStation.value = radioResult
                    radioResult?.let {
                        isRadioLiked.value = radioViewmodel.isRadioStationLiked(it._id!!)
                    }
                }


            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RadioFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).showBottomViewBeta()
        var preference = PreferenceHelper.getInstance(requireContext())
        preference[AccountState.IS_REDIRECTION] = true

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 ->{
                    binding.radioToolbar.title = ""
                }
                verticalOffset == -appBarLayout.totalScrollRange -> {
                    binding.appbarContainer.visibility = View.INVISIBLE
                    binding.radioToolbar.title = binding.radioNameTextview.text.toString()
                }
                verticalOffset > -appBarLayout.totalScrollRange -> binding.appbarContainer.visibility = View.VISIBLE
            }
        })

        radioViewmodel.radioStation.observe(viewLifecycleOwner){
            it?.let {
                configureActionBar(binding.radioToolbar, it.name)
                radioResult = it
                binding.radioListinersCountTextview.isVisible =  radioResult?.listenersId?.size ?: 0 > 0
                binding.radioListinersCountTextview.text = "${radioResult?.listenersId?.size ?: 0} ${getString(R.string.listeners)}"
                originalRadioSongSize = it.songs?.size
                showRadioResult(it)
            }
        }

        binding.shuffleStationBtn.setOnClickListener {
            radioResult!!.songs?.let { it1 -> prepareAndPlayRadio(it1) }
            mainActivityViewmodel.playedFrom.value = "${radioResult!!.name}(Radio)"
        }

        binding.likeRadioBtn.setOnClickListener { view ->
            radioResult?.let { radioRe ->
                binding.radioLoadingProgressbar.isVisible = true
                var baseId = if(radioRe.stationType == Radio.STATION_TYPE_SONG) radioRe.baseSongId
                             else if(radioRe.stationType == Radio.STATION_TYPE_ARTIST) radioRe.baseArtistId
                             else null
                var radioLikeInfo = RadioLikeInfo(radioRe.name!! , radioRe.stationType!! , baseId)
                radioViewmodel.likeRadioStation(radioLikeInfo).handleSingleDataObservation(viewLifecycleOwner){
                    binding.radioLoadingProgressbar.isVisible = false
                    if(it != null){
                        isRadioLiked.value = true
                        radioRe._id = it
                        mainActivityViewmodel.selectedRadio = radioRe
                        binding.likeRadioBtn.visibility = View.INVISIBLE
                        binding.unlikeRadioBtn.visibility = View.VISIBLE
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_library))
                    }
                    else (requireActivity() as MainActivity).showSnacbar("Unable to add to favorite")
                }
            }
        }
        binding.unlikeRadioBtn.setOnClickListener {
            radioViewmodel.unlikeRadioStation(radioResult!!._id!!).handleSingleDataObservation(viewLifecycleOwner){
                if(it){
                    isRadioLiked.value = !it
                    mainActivityViewmodel.selectedRadio = null
                    binding.likeRadioBtn.visibility = View.VISIBLE
                    binding.unlikeRadioBtn.visibility = View.INVISIBLE
                    (requireActivity() as MainActivity).showSnacbar(getString(R.string.removed_from_library))
                }
                else (requireActivity() as MainActivity).showSnacbar("Unable to remove from favorite")
            }
        }


        radioViewmodel.getError().observe(viewLifecycleOwner){}
        radioViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.radioLoadingProgressbar.isVisible = false
            error?.handleError(requireContext() , {radioViewmodel.removeOldErrors()}){
                binding.radioErrorTextview.isVisible = true
                binding.radioLoadingProgressbar.isVisible = false
                radioViewmodel.error.value = null
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (mainActivityViewmodel.selectedRadio == null || !isRadiohasSong) {
            menu.removeItem(R.id.save_radio_to_playlist_menu_item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.radio_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

            R.id.save_radio_to_playlist_menu_item -> {
                radioResult?.let {
                    var songsId = it.songs?.map { song -> song._id }
                    var playlist = Playlist(_id = null ,  name = "${it.name}" , creatorName = "Komkum" , songs = songsId , forceUpdate = true)
                    playlistViewmodel.createPlaylist(playlist).handleSingleDataObservation(viewLifecycleOwner){
                        (requireActivity() as MainActivity).showSnacbar("Playlist added to Library")
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    fun showRadioResult(radio: Radio){
        Log.i("radioquery", isRadioLiked.toString())
        isRadioLiked.observe(viewLifecycleOwner){
            if (it) {
                mainActivityViewmodel.selectedRadio = radioResult
                binding.likeRadioBtn.visibility = View.INVISIBLE
                binding.unlikeRadioBtn.visibility = View.VISIBLE
            } else {
                mainActivityViewmodel.selectedRadio = null
                binding.likeRadioBtn.visibility = View.VISIBLE
                binding.unlikeRadioBtn.visibility = View.INVISIBLE
            }
        }

        isRadiohasSong = !radio.songs.isNullOrEmpty()
        requireActivity().invalidateOptionsMenu()

        binding.radioLoadingProgressbar.visibility = View.GONE
        binding.radioNameTextview.text = radio.name


        var songs = radio.songs?.map { song -> song.tittle!! }
        if (songs.isNullOrEmpty()) binding.radioErrorTextview.isVisible = true
        else {
            binding.appbarContainer.isVisible = true
            binding.radioNestedscrollview.isVisible = true

            var artists = mutableListOf<String>()
            radio.songs?.forEach { song -> artists.addAll(song.artistsName!!) }
            var imagelist = radio.songs?.map { song -> song.thumbnailPath!!.replace("localhost", AdapterDiffUtil.URL) }?.distinct()?.take(4)


            var songsspanList = songs.joinToString("span")
            var artistsSpanlist = artists.distinct().joinToString("span")
            var songSpanner = Spanner(songsspanList).span("span", custom { RadioSpan(requireContext(), R.drawable.tab_item_not_active , resources.getInteger(R.integer.bullet_margin)) })
            var artistSpanner = Spanner(artistsSpanlist).span(
                "span",
                custom { RadioSpan(requireContext(), R.drawable.tab_item_not_active , resources.getInteger(R.integer.bullet_margin)) })

            binding.songListTextview.text = songSpanner
            binding.songSizeBtn.text = "${songs.size} Songs"
            binding.artistListTextview.text = artistSpanner
            binding.artistSizeBtn.text = "${artists.distinct().size} Artists"
            binding.radioImageCustomView.loadImage(imagelist ?: emptyList())

            binding.songSizeBtn.setOnClickListener {
                (requireActivity() as MainActivity).movetoSongListFragment(null , radio.songs , "Radio songs")
            }
        }
    }

    fun prepareAndPlayRadio(songs: List<Song<String, String>>){
        mainActivityViewmodel.prepareAndPlaySongsBeta(songs, PlayerState.RadioState(), null, playerStateIndicator = 3)
        mainActivityViewmodel.playedFrom.value = "Radio"
    }
}