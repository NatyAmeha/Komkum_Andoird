package com.zomatunes.zomatunes.ui.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.FragmentPlaylistListBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.adaper.DownloadStyleListItemAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistListFragment : Fragment()  , IRecyclerViewInteractionListener<BaseModel> {

    lateinit var binding : FragmentPlaylistListBinding
    val playlistViewmodel : PlaylistViewModel by viewModels()
    var dataType = -1

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                 dataType = it.getInt("DATA_TYPE")
                when(dataType){
                    Playlist.LOAD_USER_PLAYLIST -> playlistViewmodel.getUserPlaylist()
                    Playlist.FROM_PERVIOUS_FRAGMENT -> {
                        var playlists = it.getParcelableArrayList<Playlist<String>>("PLAYLISTS")
                        playlistViewmodel.playlists.value = playlists
                    }
                    else -> {}
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlaylistListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "Playlists")
        if(dataType == Playlist.FROM_PERVIOUS_FRAGMENT) binding.createPlaylistBtn.isVisible = false

        playlistViewmodel.playlists.observe(viewLifecycleOwner , Observer{playlist ->
            playlist?.let {
                var info = RecyclerViewHelper( type = "PLAYLIST" , interactionListener = this)
                var adapter = DownloadStyleListItemAdapter<BaseModel>(info)
               binding.playlistLoadingProgressbar.visibility = View.GONE
               binding.playlistListRecyclerview.layoutManager = LinearLayoutManager(view.context)
               binding.playlistListRecyclerview.adapter = adapter
               adapter.submitList(it.map { playlist -> BaseModel(baseId = playlist._id , baseTittle = playlist.name ,
                   baseSubTittle = if(playlist.type == "CHART")"By ZomaTunes span CHART"
                   else  "${playlist.creatorName ?: "Creator Name"} span ${playlist.songs?.size} songs" ,
                   baseListOfInfo = playlist.followersId)})
            }
        })

        binding.createPlaylistBtn.setOnClickListener {
            var preference = PreferenceHelper.getInstance(requireContext())
            var creatorName = preference.get(AccountState.USERNAME , "")
            MaterialDialog(requireContext()).show {
                title(text = "Create Playist")
                cornerRadius(14f)
                input(hint = "Enter playlist name" , waitForPositiveButton = true,  allowEmpty = false){ materialDialog: MaterialDialog, charSequence: CharSequence ->
                    val playlistName = charSequence.toString()
                    if(!playlistName.isBlank()){
                        var playlist = Playlist<String>(_id = null ,  name = playlistName , creatorName = creatorName)
                        playlistViewmodel.createPlaylist(playlist).handleSingleDataObservation(viewLifecycleOwner){
                            (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , it._id , null , false)
                        }
                    }
                }
                positiveButton(text = "Create")
                negativeButton(text = "Cancel")
            }
        }
    }

    override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , data.baseId , null , false)
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}