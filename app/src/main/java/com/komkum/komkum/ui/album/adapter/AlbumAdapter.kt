package com.komkum.komkum.ui.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Album
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.MediumViewPageBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
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