package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Author
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.MiniViewListItemBinding
import com.zomatunes.zomatunes.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso

class AuthorAdapter(var info : RecyclerViewHelper<Author<String , String>> , var authorList : List<Author<String , String>>) : RecyclerView.Adapter<AuthorAdapter.AuthorViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewholder {
        var binding = MiniViewListItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return AuthorViewholder(binding)
    }

    override fun onBindViewHolder(holder: AuthorViewholder, position: Int) {
        holder.bind(authorList[position])
    }

    override fun getItemCount(): Int {
       return authorList.size
    }

    inner class AuthorViewholder(var binding : MiniViewListItemBinding) : RecyclerView.ViewHolder(binding.root){
       fun bind(author : Author<String , String>){
           binding.tittleTextview.text = author.name
           binding.subtittleTextview.text = "${author.audiobooks?.size?.plus(author.books?.size ?: 0) ?: "visit"} Titles"
           var profileimage = author.profileImagePath?.replace("localhost" , AdapterDiffUtil.URL)
           Picasso.get().load(profileimage).transform(CircleTransformation()).placeholder(R.drawable.circularimg).fit().centerInside().into(binding.thumbnailImageview)

           with(binding.root){
               setOnClickListener { info.interactionListener?.onItemClick(author , adapterPosition , -1) }
           }
       }
    }
}