package com.zomatunes.zomatunes.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.MusicBrowse
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.Search
import com.zomatunes.zomatunes.databinding.BrowseContentSelectorListItemBinding
import com.zomatunes.zomatunes.ui.search.BookSearchFragment
import com.zomatunes.zomatunes.ui.search.MusicSearchFragment
import com.squareup.picasso.Picasso

class BrowseContentTypeAdapter(var info : RecyclerViewHelper<MusicBrowse>, var browsecontentType : List<MusicBrowse>) : RecyclerView.Adapter<BrowseContentTypeAdapter.BrowseViewpagerViewholder>() {
    var gradientList = listOf(R.drawable.gradient4 , R.drawable.subscription_gradient , R.drawable.gradent2 , R.drawable.gradient3 , R.drawable.gradient5)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseViewpagerViewholder {
       var binding = BrowseContentSelectorListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return BrowseViewpagerViewholder(binding)
    }

    override fun onBindViewHolder(holder: BrowseViewpagerViewholder, position: Int) {
        holder.bind(browsecontentType[position])
    }

    override fun getItemCount(): Int {
       return browsecontentType.size
    }

    inner class BrowseViewpagerViewholder(var binding : BrowseContentSelectorListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(contentType : MusicBrowse){
            binding.container.setBackgroundResource(gradientList[adapterPosition % gradientList.size])
           binding.browseTitleTextview.text = contentType.category?.toLowerCase()
            Picasso.get().load(contentType.imagePath?.replace("localhost" ,AdapterDiffUtil.URL)).fit().centerCrop().into(binding.browseImageView)
        }
    }
}

class SearchViewpagerAdapter(var fm: FragmentActivity , var allSearchResult : Search , var browseLists : List<MusicBrowse>) : FragmentStateAdapter(fm){
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
       return when(position){
           0-> MusicSearchFragment().apply {
               arguments = bundleOf("SEARCH_RESULT" to allSearchResult , "BROWSE_LIST" to browseLists)
           }
           1-> BookSearchFragment().apply {
               arguments = bundleOf("SEARCH_RESULT" to allSearchResult , "BROWSE_LIST" to browseLists)
           }
           else -> MusicSearchFragment()
       }
    }

}