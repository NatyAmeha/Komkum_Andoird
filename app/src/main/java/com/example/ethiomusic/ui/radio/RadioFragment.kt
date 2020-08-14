package com.example.ethiomusic.ui.radio

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivityViewmodel
import com.example.ethiomusic.data.model.Radio
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.RadioFragmentBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.squareup.picasso.Picasso
import javax.inject.Inject

class RadioFragment : Fragment() {

    @Inject
    lateinit var radioViewmodelFactory : RadioViewmodelFactory
    val radioViewmodel : RadioViewModel by viewModels { GenericSavedStateViewmmodelFactory(radioViewmodelFactory , this)  }

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : RadioFragmentBinding
    var genre : String? = null
    var radioData : Radio? = null
    var originalRadioSongSize : Int? = null
    var isRadioPlayed = false
    var isRadioLiked = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            genre = it.getString("RADIO_GENRE")
            radioData = it.getParcelable("RADIO_DATA")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var appComponent = (requireActivity().application as EthioMusicApplication).appComponent
        appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RadioFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.radioToolbar , "")


        radioData?.let {
            radioViewmodel.getRadioStationResult(it._id!!).handleSingleDataObservation(viewLifecycleOwner){
                radioData = it
                mainActivityViewmodel.selectedRadio = it
                originalRadioSongSize = it.songs.size
                showRadioResult(it)
            }
        }


        binding.shuffleStationBtn.setOnClickListener {
            prepareAndPlayRadio(radioData!!.songs)
        }

        binding.likeRadioBtn.setOnClickListener {view ->
            radioData?.let {
                radioViewmodel.likeRadioStation(it._id!!).handleSingleDataObservation(viewLifecycleOwner){
                    binding.likeRadioBtn.visibility = View.INVISIBLE
                    binding.unlikeRadioBtn.visibility = View.VISIBLE
                }
            }
        }
        binding.unlikeRadioBtn.setOnClickListener {
            radioViewmodel.unlikeRadioStation(radioData!!._id!!).handleSingleDataObservation(viewLifecycleOwner){
                binding.likeRadioBtn.visibility = View.VISIBLE
                binding.unlikeRadioBtn.visibility = View.INVISIBLE
            }
        }
    }



    fun showRadioResult(radio: Radio){
        radioViewmodel.isRadioStationLiked(radio._id!!).handleSingleDataObservation(viewLifecycleOwner){
            if(it){
                binding.likeRadioBtn.visibility = View.INVISIBLE
                binding.unlikeRadioBtn.visibility = View.VISIBLE
            }else{
                binding.likeRadioBtn.visibility = View.VISIBLE
                binding.unlikeRadioBtn.visibility = View.INVISIBLE
            }
            binding.radioLoadingProgressbar.visibility = View.GONE
            binding.radioNameTextview.text = radio.name


            var songs = radio.songs.map { song -> song.tittle!! }
            var artists = radio.songs.map { song -> song.genre!! }
            var imagelist = radio.songs.map { song -> song.thumbnailPath!!.replace("localhost" , "192.168.43.166") }.take(4)

            binding.songListTextview.text = songs.joinToString(" #" , postfix = " ")
            binding.artistListTextview.text = artists.joinToString(" #" , postfix = " ")
            if(imagelist.size >=4){
                Picasso.get().load(imagelist[0]).into(binding.radioImage1Imageview)
                Picasso.get().load(imagelist[1]).into(binding.radioImage2Imageview)
                Picasso.get().load(imagelist[2]).into(binding.radioImage3Imageview)
                Picasso.get().load(imagelist[3]).into(binding.radioImage4Imageview)
            }
        }


    }

    fun prepareAndPlayRadio(songs : List<Song<String,String>>){
        if (!mainActivityViewmodel.isAudioPrepared) {
            mainActivityViewmodel.playlistQueue.value = songs
            mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_MEDIA_TYPE)
        }
        mainActivityViewmodel.playerState.value = PlayerState.RadioState()
        mainActivityViewmodel.play()

    }

}