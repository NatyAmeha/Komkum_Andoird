package com.example.ethiomusic.util.adaper


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.AddSongListitemBinding
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener


class AddSongAdapter(var info : RecyclerViewHelper<Playlist<String>>) :
    ListAdapter<Playlist<Song<String,String>>, AddSongAdapter.AddSongViewholder>(AdapterDiffUtil<Playlist<Song<String,String>>>()) {

    var selectedSongs = mutableListOf<Song<String,String>>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSongViewholder {
        var binding = AddSongListitemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AddSongViewholder(binding)
    }

    override fun onBindViewHolder(holder: AddSongViewholder, position: Int) {
       holder.bind(getItem(position))
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        Toast.makeText(recyclerView.context , "time to update playlist" , Toast.LENGTH_SHORT).show()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    inner class AddSongViewholder(var binding : AddSongListitemBinding) : RecyclerView.ViewHolder(binding.root){
        lateinit var adapter : SongAdapter
        fun bind(playlist: Playlist<Song<String,String>>){
            var listener  = object : IRecyclerViewInteractionListener<Song<String,String>>{
                override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
                    selectedSongs.add(data)
                    var a  = playlist.songs!!.toMutableList()
                    a.remove(data)
                    adapter.submitList(a)
                  Toast.makeText(binding.root.context , data.tittle , Toast.LENGTH_SHORT).show()
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}

                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

            var songInfo = RecyclerViewHelper(owner = info.owner , interactionListener = listener)
            adapter = SongAdapter(songInfo)

            binding.songTypeTextview.text = playlist.name
            binding.songListRecyclerview.layoutManager = LinearLayoutManager(binding.root.context)
            binding.songListRecyclerview.adapter = adapter
            adapter.submitList(playlist.songs!!)
        }
    }
}