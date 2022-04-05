package com.komkum.komkum.ui.playlist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentPlaylistListBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.adaper.DownloadStyleListItemAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
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

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlaylistListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.playlists))
        if(dataType == Playlist.FROM_PERVIOUS_FRAGMENT) binding.createPlaylistBtn.isVisible = false

        playlistViewmodel.playlists.observe(viewLifecycleOwner , Observer{playlist ->
            playlist?.let {
                if(it.isEmpty()) binding.playlistListErrorTextview.isVisible = true
                var info = RecyclerViewHelper( type = "PLAYLIST" , interactionListener = this)
                var adapter = DownloadStyleListItemAdapter<BaseModel>(info)
               binding.playlistLoadingProgressbar.visibility = View.GONE
               binding.playlistListRecyclerview.layoutManager = LinearLayoutManager(view.context)
               binding.playlistListRecyclerview.adapter = adapter
               adapter.submitList(it.sortedByDescending { playlist -> playlist.date }
                   .map { playlist -> BaseModel(baseId = playlist._id , baseTittle = playlist.name ,
                   baseSubTittle = if(playlist.type == "CHART")"Komkum span CHART"
                   else  "${playlist.creatorName ?: "Creator Name"} span ${playlist.songs?.size} songs" ,
                   baseListOfInfo = playlist.followersId , baseImagePath = if(!playlist.coverImagePath.isNullOrEmpty()) playlist.coverImagePath!![0] else null)})
            }
        })

        binding.createPlaylistBtn.setOnClickListener {
            var preference = PreferenceHelper.getInstance(requireContext())
            var creatorName = preference.get(AccountState.USERNAME , "")
            MaterialDialog(requireContext()).show {
                title(text = getString(R.string.create_playlist))
                cornerRadius(14f)
               var d = input(hint = getString(R.string.enter_playlist_name) , waitForPositiveButton = true,  allowEmpty = false){ materialDialog: MaterialDialog, charSequence: CharSequence ->
                   binding.playlistLoadingProgressbar.isVisible = true
                   val playlistName = charSequence.toString()
                    if(playlistName.isNotBlank()){
                        var playlist = Playlist<String>(_id = null ,  name = playlistName , creatorName = creatorName)
                        playlistViewmodel.createPlaylist(playlist).handleSingleDataObservation(viewLifecycleOwner){
                           binding.playlistLoadingProgressbar.isVisible = false
                            (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , it._id , null , false)
                        }
                    }
                }
                positiveButton(text = getString(R.string.create))
                negativeButton(text = getString(R.string.cancel))
            }
        }

        playlistViewmodel.getError().observe(viewLifecycleOwner){}
        playlistViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.playlistListErrorTextview.text = it
                binding.playlistListErrorTextview.isVisible = true
//                (requireActivity() as MainActivity).showSnacbar(it)
               Toast.makeText(requireContext() , it, Toast.LENGTH_LONG).show()
            }
        }
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

    override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , data.baseId , null , false)
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}