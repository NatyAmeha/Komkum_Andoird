package com.komkum.komkum.ui.search

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController

import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.SearchFragmentBinding
import com.komkum.komkum.util.adaper.SearchViewpagerAdapter
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.tabs.TabLayoutMediator
import com.mancj.materialsearchbar.MaterialSearchBar
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.ui.home.BaseBottomTabFragment
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseBottomTabFragment() , MaterialSearchBar.OnSearchActionListener , IRecyclerViewInteractionListener<MusicBrowse> {


    val viewModel: SearchViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()
    private lateinit var binding : SearchFragmentBinding

    var isSearchbarFocused = MutableLiveData(false)


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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = resources.getColor(R.color.light_background)
        }
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())

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
                            0 -> tab.text = getString(R.string.music)
                            1 -> tab.text = getString(R.string.podcast)
                            2 -> tab.text = getString(R.string.books)
                        }
                    }.attach()
                }

                mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner) { metadata ->
                    var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                    songId?.let {
                        mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                            var a = it::class.qualifiedName
                            Log.i("typemediainfo" , "song")
                            when(a){
                                "com.example.com.komkum.komkum.data.model.Song"  -> {
                                    it.tittle?.let { it1 -> Log.i("typemedia" , it1) }
                                    mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>}
                            }
                        }
                    }
                }
            }
        }

        viewModel.getError().observe(viewLifecycleOwner){}
        viewModel.error.observe(viewLifecycleOwner){error ->
            binding.searchResultProgressbar.isVisible = false
            error?.handleError(requireContext() , {viewModel.removeOldErrors()}){
                binding.errorView.errorImageview.visibility = View.INVISIBLE
                binding.errorView.gotoDownloadBtn.isVisible = false
                binding.errorView.errorTextview.text = "Something went wrong.Please try again"
                binding.errorView.tryagainBtn.setOnClickListener {
                    binding.searchResultProgressbar.isVisible = true
                    viewModel.getSearchSuggestions()
                }
                binding.errorView.root.isVisible= true
            }
        }

    }

    override fun onDestroyView() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = resources.getColor(R.color.light_secondaryDarkColor)
        }
        super.onDestroyView()
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
