package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.text.toUpperCase
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Review
import com.komkum.komkum.databinding.ReviewListItemBinding
import com.komkum.komkum.util.extensions.convertoLocalDate

class ReviewAdapter : ListAdapter<Review , ReviewAdapter.ReviewViewholder>(ReviewAdapterDiffUtil()) {

    var backgroundList = listOf(R.drawable.circle , R.drawable.circle_2 , R.drawable.circle_3)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewholder {
        var binding = ReviewListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ReviewViewholder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewholder, position: Int) {
       holder.bind(getItem(position))
    }


    inner class ReviewViewholder(var binding : ReviewListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(review: Review){
            binding.reviewerAvatarview.displayAvatar(review.reviewerImage , review.reviewerName?.first().toString() , 15f , backgroundColor = backgroundList[absoluteAdapterPosition % backgroundList.size] )
            binding.nameTextview.text = review.reviewerName
            binding.reviewerRatingbar.rating = review.rating
            if(!review.comment.isNullOrEmpty()) binding.reviwerCommentTextview.text = review.comment
            else binding.reviwerCommentTextview.isVisible = false
            binding.reviewDateTextview.text = review.date?.convertoLocalDate(longMonthName = false)
//            review.tags?.forEach { tag ->
//                var chip = Chip(binding.root.context)
//                chip.text = tag
//                binding.reviewTagChipgroup.addView(chip)
//            }
            binding.reviwerCommentTextview.setOnClickListener {
                if(binding.reviwerCommentTextview.maxLines == 4) binding.reviwerCommentTextview.maxLines = 1000
                else binding.reviwerCommentTextview.maxLines = 4
            }
        }
    }
}


class ReviewAdapterDiffUtil : DiffUtil.ItemCallback<Review>(){
    override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem == newItem
    }

}