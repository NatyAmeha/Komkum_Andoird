package com.zomatunes.zomatunes.ui.radio

import android.R.color
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.MainActivityViewmodel
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Radio
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.RadioFragmentBinding
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RadioSpan
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans.bullet
import lt.neworld.spanner.Spans.custom


@AndroidEntryPoint
class RadioFragment : Fragment() {

    val radioViewmodel : RadioViewModel by viewModels()

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : RadioFragmentBinding
    var genre : String? = null
    var radioData : Radio? = null
    var songId : String? = null
    var artistId : String? = null

    var originalRadioSongSize : Int? = null
    var isRadioPlayed = false
    var isRadioLiked = false


    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
//            genre = it.getString("RADIO_GENRE")
                radioData = it.getParcelable("RADIO_DATA")
                songId = it.getString("SONG_ID")
                artistId = it.getString("ARTIST_ID")

                songId?.let {
                    Log.i("songradio", it)
                    radioViewmodel.getSongRadio(it)
                }

                artistId?.let {
                    Log.i("artistradio", it)
                    radioViewmodel.getArtistRadio(it)
                }
                radioData?.let {

                    radioViewmodel.getRadioStationResult(radioData!!._id!!)
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

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RadioFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).showBottomViewBeta()
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 -> binding.radioToolbar.title = ""
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
                radioData = it
                originalRadioSongSize = it.songs.size
                showRadioResult(it)
            }

        }




        binding.shuffleStationBtn.setOnClickListener {
            prepareAndPlayRadio(radioData!!.songs)
            mainActivityViewmodel.playedFrom.value = "Playing from ${radioData!!.name}(Radio)"
        }

        binding.likeRadioBtn.setOnClickListener { view ->
            radioData?.let {
                radioViewmodel.likeRadioStation(it._id!!).handleSingleDataObservation(
                    viewLifecycleOwner
                ){
                    mainActivityViewmodel.selectedRadio = radioData
                    binding.likeRadioBtn.visibility = View.INVISIBLE
                    binding.unlikeRadioBtn.visibility = View.VISIBLE
                }
            }
        }
        binding.unlikeRadioBtn.setOnClickListener {
            radioViewmodel.unlikeRadioStation(radioData!!._id!!).handleSingleDataObservation(
                viewLifecycleOwner
            ){
                mainActivityViewmodel.selectedRadio = null
                binding.likeRadioBtn.visibility = View.VISIBLE
                binding.unlikeRadioBtn.visibility = View.INVISIBLE
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (mainActivityViewmodel.selectedRadio == null) {
            menu.removeItem(R.id.save_radio_to_playlist_menu_item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.radio_menu, menu)
    }



    fun showRadioResult(radio: Radio){
        radioViewmodel.isRadioStationLiked(radio._id!!).handleSingleDataObservation(
            viewLifecycleOwner
        ){
            if(it){
                mainActivityViewmodel.selectedRadio = radioData
                binding.likeRadioBtn.visibility = View.INVISIBLE
                binding.unlikeRadioBtn.visibility = View.VISIBLE
            }else{
                mainActivityViewmodel.selectedRadio = null
                binding.likeRadioBtn.visibility = View.VISIBLE
                binding.unlikeRadioBtn.visibility = View.INVISIBLE
            }
            requireActivity().invalidateOptionsMenu()
            binding.radioLoadingProgressbar.visibility = View.GONE
            binding.radioNameTextview.text = radio.name


            var songs = radio.songs.map { song -> song.tittle!! }
            var artists = radio.songs.map { song -> song.genre!! }
            var imagelist = radio.songs.map { song -> song.thumbnailPath!!.replace(
                "localhost",
                AdapterDiffUtil.URL
            ) }.take(4)


            var songsspanList = songs.joinToString("span")
            var artistsSpanlist = artists.joinToString("span")
            var songSpanner = Spanner(songsspanList).span("span" , custom { RadioSpan(requireContext() , R.drawable.tab_item_not_active)})
            var artistSpanner = Spanner(artistsSpanlist).span("span" , custom { RadioSpan(requireContext() , R.drawable.tab_item_not_active)})

            binding.songListTextview.text = songSpanner
            binding.artistListTextview.text = artistSpanner
            binding.radioImageCustomView.loadImage(imagelist)

        }


    }

    fun prepareAndPlayRadio(songs: List<Song<String, String>>){
        if (!mainActivityViewmodel.isAudioPrepared) {
            mainActivityViewmodel.prepareAndPlaySongsBeta(
                songs,
                PlayerState.RadioState(),
                null,
                playerStateIndicator = 3
            )
        }

    }

}