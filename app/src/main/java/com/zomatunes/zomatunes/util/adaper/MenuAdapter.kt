package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.data.model.MenuItem
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.CustomMenuListItemBinding
import com.squareup.picasso.Picasso

class MenuAdapter(var recyclerViewHelper: RecyclerViewHelper<MenuItem> , var menuList : List<MenuItem>) : RecyclerView.Adapter<MenuAdapter.MenuViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewholder {
        var binding = CustomMenuListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return MenuViewholder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewholder, position: Int) {
       holder.bind(menuList[position])
    }

    override fun getItemCount(): Int {
       return menuList.size
    }

    inner class MenuViewholder(var binding : CustomMenuListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(menu : MenuItem){
            Picasso.get().load(menu.icon).placeholder(menu.icon).fit().centerCrop().into(binding.menuItemImateview)
            binding.menuItemTextview.text = menu.title

            with(binding.root){
                setOnClickListener {
                    recyclerViewHelper.interactionListener?.onItemClick(menu , adapterPosition , -1)
                }
            }
        }

    }
}