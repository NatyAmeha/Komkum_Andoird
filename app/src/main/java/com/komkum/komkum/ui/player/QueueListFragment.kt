package com.komkum.komkum.ui.player

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivityViewmodel

import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.FragmentQueueListBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.ItemTouchHelperCallback
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class QueueListFragment : Fragment() , IRecyclerViewInteractionListener<Song<String,String>>{

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()
    val stateManager : RecyclerviewStateManager<Song<String,String>> by lazy {
        RecyclerviewStateManager<Song<String,String>>()
    }

    lateinit var binding : FragmentQueueListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentQueueListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var info = RecyclerViewHelper("QUEUE" , stateManager , this  , viewLifecycleOwner)
        var adapter = SongAdapter(info)
        var itemTouchCallback = ItemTouchHelperCallback(adapter , info.type!!)
        var itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        adapter.addTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.queuelistRecyclerview)
        binding.queuelistRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.queuelistRecyclerview.adapter = adapter

        mainActivityViewmodel.playlistQueue.observe(viewLifecycleOwner , Observer {
            adapter.submitList(it as List<Song<String,String>>?)
        })

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner , Observer  {metadata ->
            metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)?.let {
                Log.i("queuelist" ,"triggered")
                stateManager.selectedItem.value = mainActivityViewmodel.playlistQueue.value?.get(it.toInt()) as Song<String, String>?
                binding.queuelistRecyclerview.scrollToPosition(it.toInt())
            }
        })

//        requireActivity().onBackPressedDispatcher.addCallback{
//            findNavController().popBackStack()
//        }

    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {}

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {
//        var newQueueList = mainActivityViewmodel.playlistQueue.value!!.toMutableList()
//        var a = newQueueList.removeAt(position)
//        mainActivityViewmodel.removeQueueItem(position)
//        mainActivityViewmodel.playlistQueue.value = newQueueList


    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        mainActivityViewmodel.playlistQueue.value?.toMutableList()?.let{
            var item =  it.removeAt(prevPosition)
            it.add(newPosition , item)
            mainActivityViewmodel.playlistQueue.value = it
            mainActivityViewmodel.moveQueueItem(prevPosition , newPosition)
        }

    }



}
