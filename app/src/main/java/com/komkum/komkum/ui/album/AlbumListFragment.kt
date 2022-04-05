package com.komkum.komkum.ui.album

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.*
import com.komkum.komkum.data.model.Album

import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.AlbumListFragmentBinding
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.extensions.toControllerActivity
import com.komkum.komkum.util.isVisible
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class AlbumListFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> {
    lateinit var binding : AlbumListFragmentBinding


    val albumListViewModel: AlbumListViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()


    var requestType : Int? = null
    var toolbarTitle : String? = null
    var dataType : Int? = null
    var albumList : List<Album<String,String>>? = null

    var isFavAlbumList = false

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                toolbarTitle = it.getString("TOOLBAR_TITTLE")
                dataType = it.getInt("DATA_TYPE")
                albumList = it.getParcelableArrayList("ALBUM_LIST")
                when(dataType){
                    AlbumListViewModel.LOAD_NEW_ALBUM ->{ albumListViewModel.getnewAlbums() }
                    AlbumListViewModel.LOAD_USER_FAV_ALBUM ->{
                        isFavAlbumList = true
                        albumListViewModel.userfavoriteAlbums()
                    }
                    AlbumListViewModel.LOAD_POPULAR_ALBUM ->{ albumListViewModel.getPopularAlbums("popularsecularalbum") }
                    AlbumListViewModel.OTHER -> albumListViewModel.albumListData.value = albumList
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
        binding = AlbumListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.albumToolbar , toolbarTitle)
        var info = RecyclerViewHelper("ALBUM" , albumListViewModel.stateManager , this , viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listInfo = info
        binding.isAddNotEnabled = true
        binding.isFavAlbumList = isFavAlbumList
        binding.viewmodel = albumListViewModel


        albumListViewModel.albumListData.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                binding.albumListErrorTextview.isVisible = true
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (albumListViewModel.stateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
             else findNavController().popBackStack()
        }


        binding.selectionMenu.favIcon.setOnClickListener {
            var albumId = albumListViewModel.stateManager.multiselectedItems.value!!.map { basemodel -> basemodel.baseId!! }
            binding.albumListLoadingProgressBar.isVisible = true
            mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_ALBUM , albumId).observe(viewLifecycleOwner){
                binding.albumListLoadingProgressBar.isVisible = false
                Toast.makeText(view.context, getString(R.string.added_to_library), Toast.LENGTH_SHORT).show()
            }
            deactivateMultiSelectionModel()

        }

        binding.selectionMenu.addIcon.setOnClickListener {
//            deactivateMultiSelectionModel()
//            // modified in the future
//            albumListViewModel.stateManager.multiselectedItems.value?.let {
//                //modi
//                var albumIds = it.map { baseModel -> baseModel.baseId }
//                var albumSongs = emptyList<Song<String,S>>()
//                    albumListViewModel.albumListData.value
//            }
//
//            (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_ALBUM)
        }

        binding.selectionMenu.downloadIcon.setOnClickListener {
            var albumIds = albumListViewModel.stateManager.multiselectedItems.value!!.map { basemodel -> basemodel.baseId!! }
            albumListViewModel.downloadAlbums(albumIds , viewLifecycleOwner)
            Toast.makeText(view.context, getString(R.string.added_to_downloads), Toast.LENGTH_SHORT).show()
            deactivateMultiSelectionModel()
        }

//        select_all_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) albumListViewModel.stateManager._multiselectedItems.value = albumListViewModel.albumListData.value
//            else albumListViewModel.stateManager._multiselectedItems.value = emptyList()
//        }
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

    fun deactivateMultiSelectionModel() {
        albumListViewModel.stateManager.changeState(RecyclerviewState.SingleSelectionState())
        binding.selectionMenu.root.visibility = View.GONE
        (requireActivity() as MainActivity).showBottomViewBeta()
    }


    override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
       if(albumListViewModel.stateManager.state.value is RecyclerviewState.MultiSelectionState){
           var size = albumListViewModel.stateManager.addOrRemoveItem(data)
       }
        else{
           (requireActivity() as MainActivity).moveFromAlbumListtoAlbumFragment(data.baseId!!)
       }
    }

    override fun activiateMultiSelectionMode() {
        if(requestType != AlbumListViewModel.LOAD_USER_FAV_ALBUM){
            albumListViewModel.stateManager.changeState(RecyclerviewState.MultiSelectionState())
            (requireActivity() as MainActivity).hideBottomView()
            binding.selectionMenu.root.visibility = View.VISIBLE
        }

    }

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}
