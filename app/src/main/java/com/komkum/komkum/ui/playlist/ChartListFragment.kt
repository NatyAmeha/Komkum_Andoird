package com.komkum.komkum.ui.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentChartListBinding
import com.komkum.komkum.util.adaper.PlaylistAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChartListFragment : Fragment() , IRecyclerViewInteractionListener<Playlist<String>> {

    lateinit var binding : FragmentChartListBinding

    val playlistViewmodel : PlaylistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChartListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.charts))
        var chartInfo = RecyclerViewHelper("CHART" , interactionListener = this ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.playlistViewmodel = playlistViewmodel
        binding.playlistInfo = chartInfo
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



    override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_CHART , data._id!! , null , false)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}