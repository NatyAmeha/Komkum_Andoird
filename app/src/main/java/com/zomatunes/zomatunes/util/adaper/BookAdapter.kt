package com.zomatunes.zomatunes.util.adaper


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Book
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.BookListItemBinding
import com.zomatunes.zomatunes.databinding.BookListItemVerticalBinding
import com.squareup.picasso.Picasso

class BookAdapter(var info : RecyclerViewHelper<Book<String>>, var bookList : List<Book<String>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        const val VERTICAL_BOOK_LIST_ITEM = 0
        const val HOROZONTAL_BOOK_LIST_ITEM = 1
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       var binding = BookListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
       var verticalBinding = BookListItemVerticalBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return when(info.listItemType){
            VERTICAL_BOOK_LIST_ITEM -> BookVerticalViewholder(verticalBinding)
            else -> BookHorizontalViewholder(binding)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(info.listItemType){
            VERTICAL_BOOK_LIST_ITEM -> (holder as BookVerticalViewholder).bind(bookList[position])
            else -> (holder as BookHorizontalViewholder).bind(bookList[position])
        }

    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    inner class BookHorizontalViewholder(var binding : BookListItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(book : Book<String>){
            binding.bookTitleTextview.text = book.name
            binding.bookAuthorTextview.text = book.authorName?.joinToString(", ")
            var imageUrl = book.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL)
            Picasso.get().load(imageUrl).fit().centerCrop().into(binding.bookCoverImageview)
            var ratingValue = book.rating?.map { rating -> rating.rate }
            if(!ratingValue.isNullOrEmpty()){
                var totalValue =  ratingValue.reduce { acc, rating ->
                    acc.plus(rating)
                }
                var averageRatingValue = totalValue.div(book.rating!!.size)
                binding.bookRatingbar.rating = averageRatingValue

            }
            if(book.format == Book.AudiobookFormat) Picasso.get().load(R.drawable.ic_baseline_headset_24)
                .placeholder(R.drawable.ic_baseline_headset_24).into(binding.bookFormatImageview)
            else Picasso.get().load(R.drawable.ic_baseline_menu_book_24).placeholder(R.drawable.ic_baseline_menu_book_24).into(binding.bookFormatImageview)

            with(binding.root){
                setOnClickListener {
                    info.interactionListener!!.onItemClick(book , adapterPosition , com.zomatunes.zomatunes.ui.album.adapter.SongAdapter.NO_OPTION)
                }
            }
        }
    }

    inner class BookVerticalViewholder(var binding : BookListItemVerticalBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(book : Book<String>){
            binding.bookTitleVTextview.text = book.name
            binding.bookAuthorVTextview.text = book.authorName?.joinToString(", ")
            binding.booKReviewVTextview.text = "${book.rating?.size} reviews"
            binding.copySoldTextview.text = "${book.downloadCount} copy sold"
            var imageUrl = book.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL)
            Picasso.get().load(imageUrl).fit().centerCrop().into(binding.bookVImageview)
            var ratingValue = book.rating?.map { rating -> rating.rate }
            if(!ratingValue.isNullOrEmpty()){
                var totalValue =  ratingValue.reduce { acc, rating ->
                    acc.plus(rating)
                }
                var averageRatingValue = totalValue.div(book.rating!!.size)
                binding.bookRatingVRatingview.rating = averageRatingValue
            }
            with(binding.root){
                setOnClickListener {
                    info.interactionListener!!.onItemClick(book , adapterPosition , com.zomatunes.zomatunes.ui.album.adapter.SongAdapter.NO_OPTION)
                }
            }
        }
    }
}