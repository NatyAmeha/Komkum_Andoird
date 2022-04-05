package com.komkum.komkum.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.komkum.komkum.R
import com.komkum.komkum.data.model.BookCollection
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.squareup.picasso.Picasso
import com.komkum.komkum.databinding.BookCollectionCustomViewBinding
import com.komkum.komkum.util.viewhelper.RadioSpan
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

class CustomBookCollectionView @JvmOverloads
constructor(context : Context , attrSet : AttributeSet? = null , defstyle : Int = 0 , defRef : Int = 0 ) : ConstraintLayout(context , attrSet , defstyle , defRef) {
    lateinit var binding : BookCollectionCustomViewBinding
    init {
          binding = BookCollectionCustomViewBinding.inflate(LayoutInflater.from(context) , this)
     }

    fun hideText(){
        binding.collectionDescriptionContainer.visibility = View.GONE
//        book_collection_name_textview.visibility = View.GONE
//        book_collection_follower_textview.visibility = View.GONE
    }
    fun inflateData(bookCollection : BookCollection , backgroundColor : Int){

        this.setBackgroundColor(resources.getColor(backgroundColor))
        var imageList = bookCollection.books!!.map { book -> book.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL) }.toMutableList()

        if (imageList.size == 2) {
            Picasso.get().load(imageList[0]).fit().centerCrop().into(binding.book1Imageview)
            Picasso.get().load(imageList[1]).fit().centerCrop().into(binding.book2Imageview)

        } else if (imageList.size == 3) {
            Picasso.get().load(imageList[0]).fit().centerCrop().into(binding.book1Imageview)
            Picasso.get().load(imageList[1]).fit().centerCrop().into(binding.book2Imageview)
            Picasso.get().load(imageList[2]).fit().centerCrop().into(binding.book3Imageview)
        } else {
            Picasso.get().load(imageList[0]).fit().centerCrop().into(binding.book1Imageview)
            Picasso.get().load(imageList[1]).fit().centerCrop().into(binding.book2Imageview)
            Picasso.get().load(imageList[2]).fit().centerCrop().into(binding.book3Imageview)
            Picasso.get().load(imageList[3]).fit().centerCrop().into(binding.book4Imageview)
        }
        binding.bookCollectionNameTextview.text = bookCollection.name
        var description = "By ${bookCollection.curator} span ${bookCollection.books?.size} books"
        var spannerText = Spanner(description).span("span" , Spans.custom {
            RadioSpan(context , R.drawable.tab_item_not_active , context.resources.getInteger(R.integer.bullet_margin))
        })
        binding.bookCollectionFollowerTextview.text = spannerText
    }
}