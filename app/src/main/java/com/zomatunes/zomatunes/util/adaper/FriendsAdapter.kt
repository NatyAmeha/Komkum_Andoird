package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.databinding.UserListItemBinding
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso

class FriendsAdapter(var userInfo : RecyclerViewHelper<User> , var userList : List<User>) : RecyclerView.Adapter<FriendsAdapter.FriendsViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewholder {
       var binding = UserListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return FriendsViewholder(binding)
    }

    override fun onBindViewHolder(holder: FriendsViewholder, position: Int) {
      holder.bind(userList[position])
    }

    override fun getItemCount(): Int {
       return userList.size
    }

    inner class FriendsViewholder(var binding : UserListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(user : User){
            var imageUrl = user.profileImagePath?.replace("localhost" , AdapterDiffUtil.URL)
            binding.usernameTextview.text = user.username
            binding.userDescriptionTextview.text = "4 public playlist , 1 Audiobook"
            Picasso.get().load(imageUrl).placeholder(R.drawable.circularimg).fit().centerCrop().transform(CircleTransformation()).into(binding.userProfileImageview)

            binding.root.setOnClickListener {
                userInfo.interactionListener?.onItemClick(user , adapterPosition , -1)
            }
        }
    }
}