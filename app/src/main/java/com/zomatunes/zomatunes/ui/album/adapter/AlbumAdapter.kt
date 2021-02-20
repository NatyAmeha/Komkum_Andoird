package com.zomatunes.zomatunes.ui.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.IMainActivity
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Album
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.MediumViewPageBinding
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.viewhelper.RoundImageTransformation
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
            var imageurl = data.albumCoverPath?.replace("localhost" , AdapterDiffUtil.URL)
            Picasso.get().load(imageurl).fit().centerCrop().into(binding.mediumThumbnailImageview)
        }
    }
}