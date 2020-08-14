package com.example.ethiomusic.ui.library

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivity

import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.LibraryFragmentBinding
import com.example.ethiomusic.ui.playlist.PlaylistFragment
import com.example.ethiomusic.util.adaper.DownloadStyleListItemAdapter
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import javax.inject.Inject

class LibraryFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> {


    @Inject
    lateinit var libraryFactory : LibraryViewmodelFacotry
    val libraryViewmodel : LibraryViewModel by viewModels{GenericSavedStateViewmmodelFactory(libraryFactory , this)}

    lateinit var binding : LibraryFragmentBinding


    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LibraryFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = libraryViewmodel

        libraryViewmodel.getUserPlaylist()

        libraryViewmodel.playlistData.observe(viewLifecycleOwner , Observer{playlist ->
           playlist?.let {
               var info = RecyclerViewHelper( type = "PLAYLIST" , interactionListener = this)
               var adapter = DownloadStyleListItemAdapter<BaseModel>(info)
               binding.playlistRecyclerview.layoutManager = LinearLayoutManager(view.context)
               binding.playlistRecyclerview.adapter = adapter
               adapter.submitList(it.map { playlist -> BaseModel(baseId = playlist._id , baseTittle = playlist.name , baseSubTittle = "${playlist.songs?.size} songs") })

           }
        })

        binding.createplaylistBtn.setOnClickListener {
            var dialog = MaterialDialog(requireContext()).show {
                title(text = "Create Playist")
                cornerRadius(14f)
                input(hint = "Enter playlist name" , waitForPositiveButton = true,  allowEmpty = false){ materialDialog: MaterialDialog, charSequence: CharSequence ->
                    val playlistName = charSequence.toString()
                    if(!playlistName.isBlank()){
                        var playlist = Playlist<String>(_id = null ,  name = playlistName)
                        libraryViewmodel.createPlaylist(playlist).handleSingleDataObservation(viewLifecycleOwner){
                            (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , it._id , null , false)
                        }
                    }
                }
                positiveButton(text = "Create")
                negativeButton(text = "Cancel")
            }
        }
    }

    override fun onItemClick(
        data: BaseModel,
        position: Int,
        option: Int?
    ) {
        (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , data.baseId , null , false)
    }

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }

}
