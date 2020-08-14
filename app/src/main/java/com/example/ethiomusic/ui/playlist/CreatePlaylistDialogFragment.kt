package com.example.ethiomusic.ui.playlist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivity

import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.databinding.FragmentCreatePlaylistDialogBinding
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class CreatePlaylistDialogFragment : DialogFragment() {

    companion object{
      fun newInstance() = CreatePlaylistDialogFragment()
    }

    lateinit var binding : FragmentCreatePlaylistDialogBinding

    @Inject
    lateinit var playlistFactory : PlaylistViewModelFactory
    val playlistViewmodel : PlaylistViewModel by viewModels { GenericSavedStateViewmmodelFactory(playlistFactory ,this) }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding  = FragmentCreatePlaylistDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }



}
