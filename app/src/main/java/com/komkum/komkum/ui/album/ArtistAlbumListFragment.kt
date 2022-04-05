package com.komkum.komkum.ui.album

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.komkum.komkum.MainActivity

import com.komkum.komkum.data.model.Album
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.databinding.FragmentArtistAlbumListBinding
import com.komkum.komkum.util.adaper.MiniViewAdapter
import com.komkum.komkum.util.extensions.toControllerActivity
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistAlbumListFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel>  {

    lateinit var binding : FragmentArtistAlbumListBinding

    val rstateManager : RecyclerviewStateManager<BaseModel> by lazy {
        RecyclerviewStateManager<BaseModel>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArtistAlbumListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            var albumList = it.getParcelableArrayList<Album<String,String>>("ALBUMLIST")?.toList()
            var adapter = MiniViewAdapter<BaseModel>(rstateManager, this, this, "ALBUM")
            binding.artistAlbumRecyclerview.layoutManager = GridLayoutManager(view.context , 3)
            binding.artistAlbumRecyclerview.adapter = adapter

            var baselist = albumList?.map { album ->
                album.toBaseModel().apply { this.baseSubTittle = "${album.songs?.size.toString()} songs" }
            }
            adapter.submitList(baselist)
        }
    }

    override fun onItemClick(
        data: BaseModel,
        position: Int,
        option: Int?
    ) {
        if(rstateManager.state.value is RecyclerviewState.MultiSelectionState){
            var size = rstateManager.addOrRemoveItem(data)
//            selectec_count_textview.text = if(size == 0) "Select All" else size.toString()
        }
        else{
            (requireActivity() as MainActivity).movefromArtisttoAlbum(data.baseId!!)
        }
    }

    override fun activiateMultiSelectionMode() {
        rstateManager.changeState(RecyclerviewState.MultiSelectionState())
//        this.toControllerActivity().hideBottomView()
//        selection_menu.visibility = View.VISIBLE
    }

    override fun onSwiped(position: Int) {


    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }


}
