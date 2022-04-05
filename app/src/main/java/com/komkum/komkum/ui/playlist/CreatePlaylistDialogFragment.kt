package com.komkum.komkum.ui.playlist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.komkum.komkum.databinding.FragmentCreatePlaylistDialogBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class CreatePlaylistDialogFragment : DialogFragment() {

    companion object{
      fun newInstance() = CreatePlaylistDialogFragment()
    }

    lateinit var binding : FragmentCreatePlaylistDialogBinding
    val playlistViewmodel : PlaylistViewModel by viewModels()


    override fun onAttach(context: Context) {
        super.onAttach(context)
//        var application = requireActivity().application as ZomaTunesApplication
//        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding  = FragmentCreatePlaylistDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }



}
