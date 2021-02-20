package com.zomatunes.zomatunes.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.BookCollection
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.book_collection_custom_view.view.*
import kotlinx.android.synthetic.main.book_collection_custom_view.view.book_1_imageview
import kotlinx.android.synthetic.main.book_collection_custom_view.view.book_2_imageview
import kotlinx.android.synthetic.main.book_collection_custom_view.view.book_3_imageview
import kotlinx.android.synthetic.main.book_collection_custom_view.view.book_4_imageview
import kotlinx.android.synthetic.main.book_collection_custom_view.view.book_collection_follower_textview
import kotlinx.android.synthetic.main.book_collection_custom_view.view.book_collection_name_textview
import kotlinx.android.synthetic.main.demo_layout.view.*

class CustomBookCollectionView @JvmOverloads
constructor(context : Context , attrSet : AttributeSet? = null , defstyle : Int = 0 , defRef : Int = 0 ) : ConstraintLayout(context , attrSet , defstyle , defRef) {
     init {
         var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
         inflater.inflate(R.layout.book_collection_custom_view , this)


     }

    fun hideText(){
        collection_description_container.visibility = View.GONE
//        book_collection_name_textview.visibility = View.GONE
//        book_collection_follower_textview.visibility = View.GONE
    }
    fun inflateData(bookCollection : BookCollection , backgroundColor : Int){

        this.setBackgroundColor(resources.getColor(backgroundColor))
        var imageList = bookCollection.books!!.map { book -> book.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL) }.toMutableList()

        if (imageList.size == 2) {
            Picasso.get().load(imageList[0]).fit().centerCrop().into(book_1_imageview)
            Picasso.get().load(imageList[1]).fit().centerCrop().into(book_2_imageview)

        } else if (imageList.size == 3) {
            Picasso.get().load(imageList[0]).fit().centerCrop().into(book_1_imageview)
            Picasso.get().load(imageList[1]).fit().centerCrop().into(book_2_imageview)
            Picasso.get().load(imageList[2]).fit().centerCrop().into(book_3_imageview)
        } else {
            Picasso.get().load(imageList[0]).fit().centerCrop().into(book_1_imageview)
            Picasso.get().load(imageList[1]).fit().centerCrop().into(book_2_imageview)
            Picasso.get().load(imageList[2]).fit().centerCrop().into(book_3_imageview)
            Picasso.get().load(imageList[3]).fit().centerCrop().into(book_4_imageview)
        }
        book_collection_name_textview.text = bookCollection.name
        book_collection_follower_textview.text = "${bookCollection.books?.size} books"
    }
}