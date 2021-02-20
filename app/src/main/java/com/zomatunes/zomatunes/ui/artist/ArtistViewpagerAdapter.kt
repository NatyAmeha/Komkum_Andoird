package com.zomatunes.zomatunes.ui.artist

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zomatunes.zomatunes.data.model.Album
import com.zomatunes.zomatunes.data.model.Artist
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.ui.album.AlbumListFragment
import com.zomatunes.zomatunes.ui.album.ArtistAlbumListFragment
import com.zomatunes.zomatunes.ui.song.SongListFragment


class ArtistViewpagerAdapter(fragmentActivity: FragmentActivity , var artistData : Artist<Song<String,String>,Album<Song<String,String>,String>>)
    : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
      return  when(position){
           0 -> {
               SongListFragment().apply {
                   arguments = Bundle().apply { putParcelableArrayList("SONGLIST", ArrayList(artistData.singleSongs)) }
               }
           }
           1 -> ArtistAlbumListFragment().apply {
               arguments = bundleOf("ALBUMLIST" to artistData.albums)
           }
           else -> SongListFragment()
       }
    }
}