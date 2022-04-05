package com.komkum.komkum.ui.store

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.databinding.FragmentAdDisplayBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdDisplayFragment : Fragment() {

    lateinit var binding : FragmentAdDisplayBinding
    lateinit var player : SimpleExoPlayer

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    var adInfo : Ads? = null
    var questionIndex : Int = 0
    var teamId : String? = null
    var totalResult : Int = 0
    var isUserPlayed : Boolean? = null

    var isAdCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            adInfo = it.getParcelable("AD_INFO")
            questionIndex = it.getInt("QUESTION_INDEX")
            teamId = it.getString("TEAM_ID")
            totalResult = it.getInt("RESULT")
            isUserPlayed = it.getBoolean("IS_USER_PLAYED")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAdDisplayBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> findNavController().popBackStack(R.id.gameTeamFragment , false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       adInfo?.let {
           it.video?.let {url ->
               player =  SimpleExoPlayer.Builder(requireContext()).build()
                   .also {
                       binding.videoPlayer.player = it
                       val mediaItem = MediaItem.fromUri(url)
                       it.setMediaItem(mediaItem)
                   }
               player.addListener(object : Player.Listener {
                   override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                       when(playbackState){
                           Player.STATE_ENDED -> {
                               binding.adVideoLoadingProgressbar.isVisible = false
                               isAdCompleted = true
                               binding.continueBtn.alpha = 1f
                               mainActivityViewmodel.play()
                           }
                           Player.STATE_BUFFERING -> binding.adVideoLoadingProgressbar.isVisible = true
                           Player.STATE_READY -> binding.adVideoLoadingProgressbar.isVisible = false
                       }
                   }
               })
               // pause if(music, podcast or audiobook currently playing)
               mainActivityViewmodel.pause()
               player.prepare()
               player.playWhenReady = true
           }
           binding.advertiserNameTextview.text = it.tittle
           binding.adDescriptionTextview.text =  it.description
       }

        binding.continueBtn.setOnClickListener {
            if(isAdCompleted){
                adInfo?.let {adInf ->
                    teamId?.let {
                        if(questionIndex == 0) (requireActivity() as MainActivity).moveToStartGame(adInf , questionIndex , it , totalResult)
                        else (requireActivity() as MainActivity).moveToQuiz(adInf , questionIndex , it, totalResult, isUserPlayed)
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().popBackStack(R.id.gameTeamFragment , false)
        }
    }
    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            mainActivityViewmodel.play()
            player.release()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            mainActivityViewmodel.play()
            if(::player.isInitialized)
                player.release()
        }
    }

}