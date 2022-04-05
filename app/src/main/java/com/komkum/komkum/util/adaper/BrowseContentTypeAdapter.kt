package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Search
import com.komkum.komkum.databinding.BrowseContentSelectorListItemBinding
import com.komkum.komkum.ui.search.BookSearchFragment
import com.komkum.komkum.ui.search.MusicSearchFragment
import com.squareup.picasso.Picasso
import com.komkum.komkum.ui.search.PodcastSearchFragment

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
        return 3
    }

    override fun createFragment(position: Int): Fragment {
       return when(position){
           0-> MusicSearchFragment().apply {
               arguments = bundleOf("SEARCH_RESULT" to allSearchResult , "BROWSE_LIST" to browseLists)
           }
           1 -> PodcastSearchFragment().apply {
               arguments = bundleOf("SEARCH_RESULT" to allSearchResult , "BROWSE_LIST" to browseLists)
           }
//           2-> BookSearchFragment().apply {
//               arguments = bundleOf("SEARCH_RESULT" to allSearchResult , "BROWSE_LIST" to browseLists)
//           }
           else -> MusicSearchFragment()
       }
    }

}