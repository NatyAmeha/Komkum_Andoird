package com.komkum.komkum.ui.home

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.ui.artist.ArtistsHomeFragment
import com.komkum.komkum.ui.book.BookHomeFragment
import com.komkum.komkum.ui.download.DownloadListFragment
import com.komkum.komkum.ui.library.LibraryFragment
import com.komkum.komkum.ui.podcast.PodcastHomeFragment

@ExperimentalPagerApi
class HomeViewPagerAdapter(fragmentActivity: FragmentActivity , var listOfgames : List<Ads>? , selectedPage : Int = 0) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment().apply {
                arguments = bundleOf("GAMES" to listOfgames)
            }
            1 -> PodcastHomeFragment().apply {
                arguments = bundleOf("GAMES" to listOfgames)
            }
            2 -> BookHomeFragment().apply {
                arguments = bundleOf("GAMES" to listOfgames)
            }
            else -> HomeFragment().apply {
                arguments = bundleOf("GAMES" to listOfgames)
            }
        }
    }


}