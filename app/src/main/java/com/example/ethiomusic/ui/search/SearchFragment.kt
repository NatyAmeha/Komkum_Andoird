package com.example.ethiomusic.ui.search

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivity

import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.MusicBrowse
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.SearchFragmentBinding
import com.example.ethiomusic.ui.account.subscription.PaymentDialogFragment
import com.example.ethiomusic.ui.album.adapter.ArtistAdapter
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.util.adaper.BrowseCategoryAdapter
import com.example.ethiomusic.util.adaper.MiniViewAdapter
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.handleListDataObservation
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.multi_selection_menu.*
import javax.inject.Inject

class SearchFragment : Fragment() , SearchView.OnQueryTextListener , IRecyclerViewInteractionListener<MusicBrowse> {

    @Inject
    lateinit var searchViewmodelFactory: SearchViewmodelFactory
    val viewModel: SearchViewModel by viewModels{GenericSavedStateViewmmodelFactory(searchViewmodelFactory , this)}

    private lateinit var binding : SearchFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SearchFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner  = viewLifecycleOwner
        binding.showSuggestion = false
        binding.viewmodel = viewModel
        configureActionBar(binding.searchToolbar)
        this.toControllerActivity().hideplayer()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if(binding.searchContainer.visibility == View.VISIBLE){
                binding.searchContainer.visibility = View.GONE
                binding.browseRecyclerview.visibility = View.VISIBLE
            }else this.handleOnBackPressed()
        }

        viewModel.getSearchSuggestions()

        viewModel.musicBrowseString.handleListDataObservation(viewLifecycleOwner){
            var categorizedBrwosingString = it.groupBy { browse -> browse.category }
            var browseInfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner)
            var browseCategoryAdapter = BrowseCategoryAdapter(browseInfo , categorizedBrwosingString)
            binding.browseRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.browseRecyclerview.adapter = browseCategoryAdapter
        }



        viewModel.searchResult.handleSingleDataObservation(viewLifecycleOwner){
            binding.searchResultProgressbar.visibility = View.GONE
            var songAdapter = SongAdapter()
            binding.songSearchRecyclerview.layoutManager = LinearLayoutManager(view.context)
            binding.songSearchRecyclerview.adapter = songAdapter
            it.songs?.let {
                var songList = if(it.size > 4) it.subList(0,4); else it
                songAdapter.submitList(songList)
            }

            var albumListener = object :IRecyclerViewInteractionListener<BaseModel>{
                override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.baseId!! , false , false)
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}

            }



            var albumAdapter = MiniViewAdapter(type = "ALBUM" , interaction = albumListener  )
            binding.albumSearchRecyclerview.layoutManager = GridLayoutManager(view.context , 3)
            binding.albumSearchRecyclerview.adapter = albumAdapter
            it.albums?.let {
                var albumList = if(it.size > 6) it.subList(0,6); else it
                var baseInfo = albumList.map { album -> album.toBaseModel() }
                albumAdapter.submitList(baseInfo)
            }

            var listener = object :IRecyclerViewInteractionListener<Artist<String , String>>{
                override fun onItemClick(
                    data: Artist<String, String>,
                    position: Int,
                    option: Int?
                ) {
                    (requireActivity() as MainActivity).moveToArtist(data._id)
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}

                override fun onMoved(prevPosition: Int, newPosition: Int) {}

            }
            var artistInfo = RecyclerViewHelper(interactionListener = listener , owner = viewLifecycleOwner)
            var artistAdapter = ArtistAdapter(artistInfo)
            binding.artistSearchRecyclerview.layoutManager = GridLayoutManager(requireContext() , 3)
            binding.artistSearchRecyclerview.adapter = artistAdapter
            it.artists?.let {
                var artistList = if(it.size > 6) it.subList(0,6); else it
                artistAdapter.submitList(artistList)
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_menu , menu)
        var searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView = menu.findItem(R.id.main_app_bar_search).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.setIconifiedByDefault(true)
        searchView.queryHint = "Search songs, playlists, podcasts and more"
        searchView.setOnQueryTextListener(this)
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.browseRecyclerview.visibility = View.INVISIBLE
                binding.searchContainer.visibility = View.VISIBLE
            }
        }


    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText.isNullOrEmpty()){
            binding.searchResultProgressbar.visibility = View.VISIBLE
            viewModel.getSearchSuggestions()
        }
        newText?.let {
            binding.searchResultProgressbar.visibility = View.VISIBLE
            binding.showSuggestion = false
            viewModel.getSearchResult(it)
        }
        return false
    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var bundle = bundleOf("BROWSE" to data)
        findNavController().navigate(R.id.browseMusicFragment , bundle)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}
