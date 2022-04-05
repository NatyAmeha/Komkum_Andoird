package com.komkum.komkum.ui.artist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R

import com.komkum.komkum.data.model.Artist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentArtistListBinding
import com.komkum.komkum.util.constants.AppConstant
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ArtistListFragment : Fragment() , IRecyclerViewInteractionListener<Artist<String,String>>{

    val artistviewmodel : ArtistViewModel by viewModels()

    lateinit var binding : FragmentArtistListBinding

    var title : String? = null
    var dataType : Int? = null
    var artistList : List<Artist<String , String>>? = null

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                title = it.getString("TOOLBAR_TITLE")
                dataType = it.getInt("DATA_TYPE")
                artistList = it.getParcelableArrayList("ARTIST_LIST")

                when(dataType){
                    Artist.LOAD_FAVORITE_ARTIST -> {
                        (requireActivity() as MainActivity).hidePlayerCard()
                        artistviewmodel.getUserFavoriteArtists()
                    }
                    Artist.OTHER -> artistviewmodel.artistsList.value = artistList
                    else -> {}
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).hideBottomView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArtistListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.artistListToolbar , title)
        var info = RecyclerViewHelper(type = "Artist" , interactionListener = this , layoutOrientation = AppConstant.AdapterConstant.GRID_ORIENTATION)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = artistviewmodel
        binding.listInfo = info

        if (dataType == Artist.OTHER) binding.findMoreArtistBtn.isVisible = false

        artistviewmodel.artistsList.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()) binding.artistListErrorTextview.isVisible = true
        }

        binding.findMoreArtistBtn.setOnClickListener {
            var bundle = bundleOf("FROM_FAVORITE" to true)
            findNavController().navigate(R.id.action_artistListFragment_to_artisSelectiontListFragment2 , bundle)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).moveToArtist(data._id)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}
