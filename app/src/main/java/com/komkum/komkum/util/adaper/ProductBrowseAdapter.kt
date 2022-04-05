package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.Category
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.ProductBrowseListItemBinding
import com.komkum.komkum.util.viewhelper.CircleTransformation

class ProductBrowseAdapter(var info : RecyclerViewHelper<Category> , var categoryList : List<Category>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var binding = ProductBrowseListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ProductBrowseViewholder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProductBrowseViewholder).bind(categoryList[position])
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class ProductBrowseViewholder(var binding : ProductBrowseListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(category : Category){
            Picasso.get().load(category.image).fit().centerCrop().transform(CircleTransformation()).into(binding.categoryImageview)
            binding.categoryTextview.text = category.name

            with(binding.root){
                setOnClickListener {
                    info?.interactionListener?.onItemClick(category , absoluteAdapterPosition , -1)
                }
            }
        }
    }


}