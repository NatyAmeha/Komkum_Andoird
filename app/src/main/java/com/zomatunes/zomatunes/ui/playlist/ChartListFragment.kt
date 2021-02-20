package com.zomatunes.zomatunes.ui.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.FragmentChartListBinding
import com.zomatunes.zomatunes.util.adaper.PlaylistAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChartListFragment : Fragment() , IRecyclerViewInteractionListener<Playlist<String>> {

    lateinit var binding : FragmentChartListBinding

    val playlistViewmodel : PlaylistViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChartListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "Charts")
        var chartInfo = RecyclerViewHelper("CHART" , interactionListener = this ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.playlistViewmodel = playlistViewmodel
        binding.playlistInfo = chartInfo
    }



    override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_PLAYLIST , data._id!! , null , false)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}