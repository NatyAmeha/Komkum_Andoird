package com.example.ethiomusic.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ethiomusic.ui.artist.ArtistFragment
import com.example.ethiomusic.ui.artist.ArtistsHomeFragment
import com.example.ethiomusic.ui.download.DownloadListFragment
import com.example.ethiomusic.ui.library.LibraryFragment
import com.example.ethiomusic.ui.playlist.PlaylistFragment

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