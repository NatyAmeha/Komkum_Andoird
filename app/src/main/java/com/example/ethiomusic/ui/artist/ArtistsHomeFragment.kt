package com.example.ethiomusic.ui.artist

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivity

import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.FragmentArtistsHomeBinding
import com.example.ethiomusic.util.adaper.DownloadStyleListItemAdapter
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import javax.inject.Inject


class ArtistsHomeFragment : Fragment() , IRecyclerViewInteractionListener<Artist<String,String>> {

    @Inject
    lateinit var artistViewmodelFactory : ArtistViewmodelFactory
    val artistviewmodel : ArtistViewModel by viewModels { GenericSavedStateViewmmodelFactory(artistViewmodelFactory , this) }

    val rStatemanager : RecyclerviewStateManager<Artist<String,String>> by lazy {
        RecyclerviewStateManager<Artist<String,String>>()
    }

    lateinit var binding : FragmentArtistsHomeBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArtistsHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var listInfo = RecyclerViewHelper("ARTIST" , rStatemanager , this , viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = artistviewmodel
        binding.listinfo = listInfo


        var adapter = DownloadStyleListItemAdapter(listInfo)
        binding.yourartistRecyclerview.layoutManager = LinearLayoutManager(view.context)
        binding.yourartistRecyclerview.adapter = adapter

    }



    override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).moveToArtist(data._id)
    }

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }


}
