package com.komkum.komkum.ui.artist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivity

import com.komkum.komkum.data.model.Artist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentArtistsHomeBinding
import com.komkum.komkum.util.adaper.DownloadStyleListItemAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistsHomeFragment : Fragment() , IRecyclerViewInteractionListener<Artist<String,String>> {

    val artistviewmodel : ArtistViewModel by viewModels()

    val rStatemanager : RecyclerviewStateManager<Artist<String,String>> by lazy {
        RecyclerviewStateManager<Artist<String,String>>()
    }

    lateinit var binding : FragmentArtistsHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArtistsHomeBinding.inflate(inflater , container , false)
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

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }


}
