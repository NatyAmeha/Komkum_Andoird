package com.example.ethiomusic.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.LargeViewListItemBinding
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso

class LargeViewItemAdapter<T : BaseModel>(var info : RecyclerViewHelper<T>) : ListAdapter<T , LargeViewItemAdapter<T>.LargeViewItemViewHolder>(AdapterDiffUtil<T>()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LargeViewItemViewHolder {
       var binding = LargeViewListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return LargeViewItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LargeViewItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LargeViewItemViewHolder(var binding : LargeViewListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : T){
            binding.tittleTextview.text = data.baseTittle
            binding.subtittleTextview.text = data.baseSubTittle
            var imageUrl = data.baseImagePath?.replace("localhost" , "192.168.43.166")
            Picasso.get().load(imageUrl).placeholder(R.drawable.backimage).fit().centerCrop().into(binding.imageView3)

            info.owner?.let {
                info.stateManager?.multiselectedItems?.observe(it, Observer{ selectedItems ->
                    binding.albumSelectedImageview.isVisible = selectedItems.contains(data)
                })
            }


            setupOnclickListener(data)
        }

        fun setupOnclickListener(data : T){
            with(binding.root){
                setOnClickListener {
                    info.interactionListener?.onItemClick(data, adapterPosition,null)
                }

                setOnLongClickListener {
                    info.interactionListener?.activiateMultiSelectionMode()
                    info.interactionListener?.onItemClick(data, adapterPosition,null)
                    true
                }
            }

        }
    }
}