package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.BookCollection
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.BookCollectionListItemBinding
import com.zomatunes.zomatunes.databinding.BookCollectionViewpagerListItemBinding

class BookCollectionAdapter(var info : RecyclerViewHelper<BookCollection> , var bookCollection : List<BookCollection> ,
var backgroundColor : List<Int>? = null) : RecyclerView.Adapter<BookCollectionAdapter.BookCollectionViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookCollectionViewholder {
        var binding = BookCollectionListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return BookCollectionViewholder(binding)
    }

    override fun onBindViewHolder(holder: BookCollectionViewholder, position: Int) {
        holder.bind(bookCollection[position])
    }

    override fun getItemCount(): Int {
        return bookCollection.size
    }


    inner class BookCollectionViewholder(var binding : BookCollectionListItemBinding) : RecyclerView.ViewHolder(binding.root){
        var backgroudColorList = backgroundColor ?: listOf<Int>(R.color.primaryColor , R.color.secondaryColor , R.color.sg1 ,
            R.color.sg2 , R.color.primaryLightColor)
        fun bind(bookCollection : BookCollection){
            binding.bookCollectionDescriptionTextview.text= bookCollection.description
            binding.bookCollectionCustomView.inflateData(bookCollection , backgroudColorList[adapterPosition % backgroudColorList.size])

            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(bookCollection , adapterPosition , -1) }
            }
        }
    }
}

class BookCollectionViewpagerAdapter(var info : RecyclerViewHelper<BookCollection> , var collectionMap : Map<String , List<BookCollection>>)
    : RecyclerView.Adapter<BookCollectionViewpagerAdapter.BookCollectionViewpagerViewholder>(){

    inner class BookCollectionViewpagerViewholder(var binding : BookCollectionViewpagerListItemBinding)
    : RecyclerView.ViewHolder(binding.root){
        var color = listOf(R.color.secondaryLightColor , R.color.secondaryTextColor , R.color.primaryColor , R.color.secondaryColor ,
            R.color.sg1 , R.color.sg2 , R.color.primaryLightColor ,  R.color.secondaryLightColor , R.color.secondaryTextColor)
        fun bind(key : String , collection : List<BookCollection>){
            binding.bookCollectionGenreTextview.text = "Books on ${key.toLowerCase()}"
            var adapter = BookCollectionAdapter(info , collection , color.drop(adapterPosition*2).take(2))
            binding.bookCollectionRecyclerview.layoutManager = LinearLayoutManager(binding.root.context)
            binding.bookCollectionRecyclerview.adapter = adapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookCollectionViewpagerViewholder {
        var binding = BookCollectionViewpagerListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return BookCollectionViewpagerViewholder(binding)
    }

    override fun onBindViewHolder(holder: BookCollectionViewpagerViewholder, position: Int) {
        var key = collectionMap.keys.toList()[position]
        collectionMap[key]?.let {
            holder.bind(key , it)
        }
    }

    override fun getItemCount(): Int {
        return collectionMap.size
    }
}