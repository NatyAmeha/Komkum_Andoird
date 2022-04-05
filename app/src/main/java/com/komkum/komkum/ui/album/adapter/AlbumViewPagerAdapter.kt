package com.komkum.komkum.ui.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.data.model.Album
import com.komkum.komkum.databinding.MediumViewPageBinding

import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso

class AlbumViewPagerAdapter() : ListAdapter<Album<String,String> , AlbumViewPagerAdapter.ViewHolder>(AdapterDiffUtil<Album<String,String>>()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        iMainActivity = recyclerView.context as MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = MediumViewPageBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(getItem(position))
    }


    //view holder
    inner class ViewHolder(var binding: MediumViewPageBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : Album<String,String>){
            binding.tittleTextview.text = data.name
            binding.subtittleTextview.text = data.category
            var imageurl = data.albumCoverPath?.replace("localhost" , AdapterDiffUtil.URL)
            Picasso.get().load(imageurl).transform(RoundImageTransformation()).fit().centerCrop().into(binding.mediumThumbnailImageview)

//            with(binding.root){
//                this.setOnClickListener {
//                    if(adapterPosition >= currentList.size -1){ }
//                    iMainActivity.movefrombaseFragmentToAlbumFragment(data._id , false , true)
//                }
//                this.album_add_imageview.setOnClickListener {
//                    Toast.makeText(context , "Addition pending" , Toast.LENGTH_SHORT).show()
////                    iMainActivity.moveToAddtoFragment(listOf(data) , AddToFragment.ALBUM_INSERTION)
//                }
//                this.album_like_imageview.setOnClickListener {
//                    Toast.makeText(context , "Addition pending" , Toast.LENGTH_SHORT).show()
//                }
//                this.album_download_imageview.setOnClickListener {
//                    Toast.makeText(context , "Addition pending" , Toast.LENGTH_SHORT).show()
//                }
//
//
//            }
        }
    }
}