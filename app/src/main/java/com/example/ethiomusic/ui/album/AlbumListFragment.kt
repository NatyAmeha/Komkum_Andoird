package com.example.ethiomusic.ui.album

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
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.*

import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.AlbumListFragmentBinding
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewState
import kotlinx.android.synthetic.main.album_list_fragment.selection_menu
import kotlinx.android.synthetic.main.multi_selection_menu.*
import javax.inject.Inject

class AlbumListFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> {
    lateinit var binding : AlbumListFragmentBinding

    @Inject
    lateinit var viewmodelFactory: AlbumListViewmodelFactory
    val albumListViewModel: AlbumListViewModel by viewModels{GenericSavedStateViewmmodelFactory(viewmodelFactory , this)}

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    var requestType : Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AlbumListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var info = RecyclerViewHelper("ALBUM" , albumListViewModel.stateManager , this , viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = albumListViewModel
        binding.listInfo = info

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (albumListViewModel.stateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
             else findNavController().popBackStack()
        }

        arguments?.getString("TOOLBAR_TITTLE")?.let {configureActionBar(binding.albumToolbar , it)}

        arguments?.getInt("DATA_TYPE")?.let {
            requestType = it
            when(requestType){
                AlbumListViewModel.LOAD_POPULR_GOSPEL_ALBUM ->{ albumListViewModel.getPopularAlbums("populargospelalbum") }
                AlbumListViewModel.LOAD_USER_FAV_ALBUM ->{ albumListViewModel.userfavoriteAlbums() }
                AlbumListViewModel.LOAD_POPULAR_SECULAR_ALBUM ->{ albumListViewModel.getPopularAlbums("popularsecularalbum") }
                else -> {}
            }
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
