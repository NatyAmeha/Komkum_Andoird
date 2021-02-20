package com.zomatunes.zomatunes.ui.search

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController

import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.SearchFragmentBinding
import com.zomatunes.zomatunes.util.adaper.SearchViewpagerAdapter
import com.zomatunes.zomatunes.util.extensions.handleError
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.tabs.TabLayoutMediator
import com.mancj.materialsearchbar.MaterialSearchBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() , MaterialSearchBar.OnSearchActionListener , IRecyclerViewInteractionListener<MusicBrowse> {


    val viewModel: SearchViewModel by viewModels()

    private lateinit var binding : SearchFragmentBinding

    var isSearchbarFocused = MutableLiveData(false)
    var musicImageview = R.drawable.backimage
    var bookImageview = R.drawable.backimg


    init {
        lifecycleScope.launchWhenCreated {
            viewModel.getSearchSuggestions()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SearchFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner  = viewLifecycleOwner
        binding.showSuggestion = false
        binding.viewmodel = viewModel
//        configureActionBar(binding.searchToolbar)
//        this.toControllerActivity().hideplayer()

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
//            if(binding.searchContainer.visibility == View.VISIBLE){
//                binding.searchContainer.visibility = View.GONE
//                binding.browseRecyclerview.visibility = View.VISIBLE
//            }else this.handleOnBackPressed()
//        }



        binding.searchview.setOnSearchActionListener(this)


        viewModel.searchResult.observe(viewLifecycleOwner){
            it?.let {search ->
                viewModel.musicBrowseList.observe(viewLifecycleOwner){
                    binding.searchResultProgressbar.visibility = View.GONE
                    var searchAdapter = SearchViewpagerAdapter(requireActivity() , search , it ?: emptyList())
                    binding.searchResultViewpager.adapter = searchAdapter
                    TabLayoutMediator(binding.searchResultTablayout , binding.searchResultViewpager){tab , position ->
                        when(position){
                            0 -> tab.text = "Musics"
                            1 -> tab.text = "Books"
                        }
                    }.attach()
                }
            }
        }

        viewModel.getError().observe(viewLifecycleOwner){}
        viewModel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.musicSearchErrorTextview.isVisible = true
                binding.searchResultProgressbar.isVisible = false
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.search_menu , menu)
//        var searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        var searchView = menu.findItem(R.id.main_app_bar_search).actionView as SearchView
//
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
//        searchView.setIconifiedByDefault(true)
//        searchView.queryHint = "Search songs, playlists, podcasts and more"
    }

//    override fun onQueryTextSubmit(query: String?): Boolean {
//        return false
//    }

//    override fun onQueryTextChange(newText: String?): Boolean {
//        if(newText.isNullOrEmpty()){
//            binding.searchResultProgressbar.visibility = View.VISIBLE
//            viewModel.getSearchSuggestions()
//        }
//        newText?.let {
//            binding.searchResultProgressbar.visibility = View.VISIBLE
//            binding.showSuggestion = false
//            viewModel.getSearchResult(it)
//        }
//        return false
//    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var bundle = bundleOf("BROWSE" to data)
        when(data.contentType){
            MusicBrowse.CONTENTY_TYPE_MUSIC ->  findNavController().navigate(R.id.browseMusicFragment , bundle)
            MusicBrowse.CONTENT_TYPE_BOOK -> findNavController().navigate(R.id.bookBrowseFragment , bundle)
        }

    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    override fun onSearchStateChanged(p0: Boolean) {

        isSearchbarFocused.value = p0
    }

    override fun onSearchConfirmed(p0: CharSequence?) {
//        if(p0.isNullOrEmpty()){
//            binding.searchResultProgressbar.visibility = View.VISIBLE
//            viewModel.getSearchSuggestions()
//        }
        p0?.let {
            binding.searchResultProgressbar.visibility = View.VISIBLE
            binding.showSuggestion = false
            viewModel.getSearchResult(it.toString())
        }
    }

    override fun onButtonClicked(p0: Int) {
    }


}
