package com.komkum.komkum.ui.store

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.databinding.FragmentVideoPlayerBinding
import com.komkum.komkum.util.extensions.configureActionBar


class VideoPlayerFragment : Fragment() {

    lateinit var binding : FragmentVideoPlayerBinding
    lateinit var player : SimpleExoPlayer
    var videoUrl : String? = null

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            videoUrl = it.getString("VIDEO_URL")
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVideoPlayerBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")
        videoUrl?.let {url ->
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
                            binding.videoPlayerLoadingProgressbar.isVisible = false
                        }
                        Player.STATE_BUFFERING -> binding.videoPlayerLoadingProgressbar.isVisible = true
                        Player.STATE_READY -> binding.videoPlayerLoadingProgressbar.isVisible = false
                    }
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    Toast.makeText(requireContext() , error.message , Toast.LENGTH_LONG).show()
                }
            })

            mainActivityViewmodel.pause()
            player.prepare()
            player.playWhenReady = true
        }
    }

    override fun onPause() {
        if (Util.SDK_INT < 24) player.release()

        mainActivityViewmodel.play()
        super.onPause()
    }

    override fun onStop() {
        if (Util.SDK_INT >= 24) {
            if(::player.isInitialized)
                player.release()
        }
        mainActivityViewmodel.play()
        super.onStop()
    }


}