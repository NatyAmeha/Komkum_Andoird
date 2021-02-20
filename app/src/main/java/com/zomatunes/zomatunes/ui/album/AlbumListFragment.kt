package com.zomatunes.zomatunes.ui.album

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.data.model.Album

import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.AlbumListFragmentBinding
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_demo.*
import kotlinx.android.synthetic.main.album_list_fragment.selection_menu
import kotlinx.android.synthetic.main.multi_selection_menu.*
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

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                toolbarTitle = it.getString("TOOLBAR_TITTLE")
                dataType = it.getInt("DATA_TYPE")
                albumList = it.getParcelableArrayList("ALBUM_LIST")
                when(dataType){
                    AlbumListViewModel.LOAD_NEW_ALBUM ->{ albumListViewModel.getnewAlbums() }
                    AlbumListViewModel.LOAD_USER_FAV_ALBUM ->{ albumListViewModel.userfavoriteAlbums() }
                    AlbumListViewModel.LOAD_POPULAR_ALBUM ->{ albumListViewModel.getPopularAlbums("popularsecularalbum") }
                    AlbumListViewModel.OTHER -> albumListViewModel.albumListData.value = albumList
                    else -> {}
                }
            }
        }
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
        binding.viewmodel = albumListViewModel


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (albumListViewModel.stateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
             else findNavController().popBackStack()
        }

        fav_icon.setOnClickListener {
            var albumId = albumListViewModel.stateManager.multiselectedItems.value!!.map { basemodel -> basemodel.baseId!! }
            (mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_ALBUM , albumId) as LiveData<Resource<Boolean>>)
                .handleSingleDataObservation(viewLifecycleOwner){
                    if(it){
                        Toast.makeText(view.context, "Albums added to favorite", Toast.LENGTH_SHORT).show()
                        deactivateMultiSelectionModel()
                    }
                    else deactivateMultiSelectionModel()
                }
        }

        add_icon.setOnClickListener {
            var data = albumListViewModel.stateManager.multiselectedItems.value!!
            deactivateMultiSelectionModel()
            (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_ALBUM)
        }

        download_icon.setOnClickListener {
            var albumIds = albumListViewModel.stateManager.multiselectedItems.value!!.map { basemodel -> basemodel.baseId!! }
            albumListViewModel.downloadAlbums(albumIds , viewLifecycleOwner)
            Toast.makeText(view.context, "Albums added to Download", Toast.LENGTH_SHORT).show()
            deactivateMultiSelectionModel()
        }

//        select_all_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) albumListViewModel.stateManager._multiselectedItems.value = albumListViewModel.albumListData.value
//            else albumListViewModel.stateManager._multiselectedItems.value = emptyList()
//        }
    }


    fun deactivateMultiSelectionModel() {
        albumListViewModel.stateManager.changeState(RecyclerviewState.SingleSelectionState())
        selection_menu.visibility = View.GONE
        this.toControllerActivity().showBottomView()
    }


    override fun onItemClick(
        data: BaseModel,
        position: Int,
        option: Int?
    ) {
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
            toControllerActivity().hideBottomView()
            selection_menu.visibility = View.VISIBLE
        }

    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }
}
