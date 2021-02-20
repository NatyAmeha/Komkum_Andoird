package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.HorizontalPlaylistListItemBinding
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.squareup.picasso.Picasso

class PlaylistAdapter(var recyclerviewInfo : RecyclerViewHelper<Playlist<String>> , var playlistList : List<Playlist<String>>) : RecyclerView.Adapter<PlaylistAdapter.HorizontalPlaylistViewHolder>(){

    companion object{
        const val HORIZONTAL_LIST_ITEM = 0
        const val VERTICAL_LIST_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalPlaylistViewHolder {
        var horizontalBinding = HorizontalPlaylistListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
         return HorizontalPlaylistViewHolder(horizontalBinding)
    }

    override fun getItemCount(): Int {
      return playlistList.size
    }

    override fun onBindViewHolder(holder: HorizontalPlaylistViewHolder, position: Int) {
        holder.bind(playlistList[position])
    }

    inner class HorizontalPlaylistViewHolder(var binding : HorizontalPlaylistListItemBinding) : RecyclerView.ViewHolder(binding.root){
         fun bind(playlist : Playlist<String>){
             binding.tittleTextview.text = playlist.name
//             binding.subtittleTextview.text = "playlist creator name"
             binding.descriptionTextview.text =  if(playlist.type == "CHART") "50 songs" else  "${playlist.songs?.size} songs"
            if(!playlist.coverImagePath.isNullOrEmpty()){
                 var imageUri =  playlist.coverImagePath!!.distinct().map {  image -> image.replace("localhost" , AdapterDiffUtil.URL)}
                 var imageview = binding.mediumThumbnailImageview
                 imageview.loadImage(imageUri.take(4))

//                 Picasso.get().load(imageUri).fit().centerCrop().into(binding.mediumThumbnailImageview)
            }
            else{
                binding.mediumThumbnailImageview.loadImage(listOf(R.drawable.backimage.toString()))
            }
             with(binding.root){
                 setOnClickListener {
                     recyclerviewInfo.interactionListener?.onItemClick(playlist , adapterPosition , SongAdapter.NO_OPTION)
                 }
             }
         }
    }
}