package com.komkum.komkum.util.adaper

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.ui.user.BookRecommendationFragment
import com.komkum.komkum.ui.user.MusicRecommendationFragment
import com.komkum.komkum.ui.user.PodcastRecommendationFragment
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager

class RecommendationViewpagerAdpter(fm: FragmentActivity, var userInfo : UserWithSubscription, var browseList : List<MusicBrowse>)
    : FragmentStateAdapter(fm)  {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
       return when(position){
           0 -> MusicRecommendationFragment().apply {
           arguments = bundleOf("USER" to userInfo , "BROWSE_LIST" to browseList)
           }
           1-> PodcastRecommendationFragment().apply {
               arguments = bundleOf("USER" to userInfo , "BROWSE_LIST" to browseList)
           }
//           1-> BookRecommendationFragment().apply {
//               arguments = bundleOf("USER" to userInfo , "BROWSE_LIST" to browseList)
//           }
           else -> MusicRecommendationFragment().apply {
               arguments = bundleOf("USER" to userInfo, "BROWSE_LIST" to browseList)
           }
       }
    }

}