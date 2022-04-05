package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.User
import com.komkum.komkum.databinding.UserListItemBinding
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso

class UserAdapter(var userInfo : RecyclerViewHelper<User>, var userList : List<User>) : RecyclerView.Adapter<UserAdapter.FriendsViewholder>() {

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
            binding.userDescriptionTextview.text = "Public playlist, Audiobooks and Ebooks"
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_person_black_24dp).fit().centerCrop().transform(CircleTransformation()).into(binding.userProfileImageview)

            binding.root.setOnClickListener {
                userInfo.interactionListener?.onItemClick(user , adapterPosition , -1)
            }
        }
    }
}