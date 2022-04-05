package com.komkum.komkum.util.adaper


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.AddSongListitemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager


class AddSongAdapter(var playlists : List<Playlist<Song<String,String>>>) :
    RecyclerView.Adapter< AddSongAdapter.AddSongViewholder>() {

    var selectedSongs = mutableListOf<Song<String,String>>()
    var stateManager = RecyclerviewStateManager<Song<String , String>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSongViewholder {
        var binding = AddSongListitemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AddSongViewholder(binding)
    }

    override fun getItemCount(): Int {
       return playlists.size
    }

    override fun onBindViewHolder(holder: AddSongViewholder, position: Int) {
       holder.bind(playlists[position])
    }

//    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
//        Toast.makeText(recyclerView.context , "time to update playlist" , Toast.LENGTH_SHORT).show()
//        super.onDetachedFromRecyclerView(recyclerView)
//    }

    inner class AddSongViewholder(var binding : AddSongListitemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(playlist: Playlist<Song<String,String>>){
            var listener  = object : IRecyclerViewInteractionListener<Song<String,String>>{
                override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
                    selectedSongs.add(data)
                    var adapter = (binding.songListRecyclerview.adapter as SongAdapter)
                    var songList  = adapter.currentList.toMutableList()
                    songList.remove(data)
                    adapter.submitList(songList)
                    Toast.makeText(binding.root.context , data.tittle , Toast.LENGTH_SHORT).show()
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}

                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

            var songInfo = RecyclerViewHelper<Song<String,String>>(type = "ABs", interactionListener = listener)


//            var finalSongs = playlist.songs!!.toMutableList()
//            finalSongs.removeAll(selectedSongs)

            binding.songTypeTextview.text = playlist.name
            binding.songInfo = songInfo
            binding.songLIst = playlist.songs
        }
    }


}