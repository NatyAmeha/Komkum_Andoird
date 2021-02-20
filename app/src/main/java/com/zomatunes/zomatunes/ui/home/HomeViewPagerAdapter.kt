package com.zomatunes.zomatunes.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zomatunes.zomatunes.ui.artist.ArtistsHomeFragment
import com.zomatunes.zomatunes.ui.download.DownloadListFragment
import com.zomatunes.zomatunes.ui.library.LibraryFragment

class HomeViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> ArtistsHomeFragment()
            2 -> LibraryFragment()
            3 -> DownloadListFragment()
            else -> HomeFragment()
        }
    }


}