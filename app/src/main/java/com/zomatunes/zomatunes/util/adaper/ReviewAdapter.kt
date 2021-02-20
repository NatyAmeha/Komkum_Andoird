package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.data.model.Rating
import com.zomatunes.zomatunes.databinding.ReviewListItemBinding

class ReviewAdapter : ListAdapter<Rating , ReviewAdapter.ReviewViewholder>(ReviewAdapterDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewholder {
        var binding = ReviewListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ReviewViewholder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewholder, position: Int) {
       holder.bind(getItem(position))
    }


    inner class ReviewViewholder(var binding : ReviewListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(rating: Rating){
            binding.nameTextview.text = rating.username
            binding.reviewerRatingbar.rating = rating.rate
            binding.reviwerCommentTextview.text = rating.comment
        }
    }
}


class ReviewAdapterDiffUtil : DiffUtil.ItemCallback<Rating>(){
    override fun areItemsTheSame(oldItem: Rating, newItem: Rating): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: Rating, newItem: Rating): Boolean {
        return oldItem == newItem
    }

}