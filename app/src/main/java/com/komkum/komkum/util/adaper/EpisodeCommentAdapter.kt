package com.komkum.komkum.util.adaper

import com.komkum.komkum.data.model.EpisodeComment
import com.komkum.komkum.databinding.CommentListItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.komkum.komkum.R
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.util.extensions.convertoLocalDate
import com.komkum.komkum.util.viewhelper.CircleTransformation
import java.util.*


class EpisodeCommentAdapter(var info : RecyclerViewHelper<EpisodeComment<String>>)
    : ListAdapter<EpisodeComment<String> , EpisodeCommentAdapter.EpisodeCommentViewholer>(CommentDiffUtil()) {

    var backgroundList = listOf(R.drawable.circle , R.drawable.circle_2 , R.drawable.circle_3)

    inner class EpisodeCommentViewholer(var binding : CommentListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(comment : EpisodeComment<String>){
            binding.commentTextview.text = comment.text
            binding.commenterTextview.text = comment.username
            val cal = Calendar.getInstance()
            cal.time = comment.date
            binding.commentDateTextview.text = comment.date?.convertoLocalDate(longMonthName = false)
            binding.commentAvatarview.displayAvatar(comment.profileImage , comment.username?.first().toString() , 14f , backgroundColor = backgroundList[absoluteAdapterPosition % backgroundList.size])

            binding.commentTextview.setOnClickListener {
                if(binding.commentTextview.maxLines == 5) binding.commentTextview.maxLines = 1000
                else binding.commentTextview.maxLines = 5
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeCommentViewholer {
        var binding = CommentListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return EpisodeCommentViewholer(binding)
    }

    override fun onBindViewHolder(holder: EpisodeCommentViewholer, position: Int) {
       holder.bind(getItem(position))
    }


}

class CommentDiffUtil : DiffUtil.ItemCallback<EpisodeComment<String>>(){
    override fun areItemsTheSame(oldItem: EpisodeComment<String>, newItem: EpisodeComment<String>): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: EpisodeComment<String>, newItem: EpisodeComment<String>): Boolean {
        return oldItem == newItem
    }


}