package com.example.ethiomusic.ui.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.IMainActivity
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.Album
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.MediumViewPageBinding
import com.example.ethiomusic.util.adaper.AdapterDiffUtil
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso

class AlbumAdapter : ListAdapter<Album<String , String> , AlbumAdapter.AlbumViewHolder>(AdapterDiffUtil<Album<String,String>>()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        iMainActivity = recyclerView.context as MainActivity
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
      var binding = MediumViewPageBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
       holder.bind(getItem(position))
    }

    inner class AlbumViewHolder(var binding : MediumViewPageBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : Album<String,String>){
            binding.tittleTextview.text = data.name
            binding.subtittleTextview.text = data.category
            var imageurl = data.albumCoverPath?.replace("localhost" , "192.168.43.166")
            Picasso.get().load(imageurl).fit().centerCrop().into(binding.mediumThumbnailImageview)
        }
    }
}