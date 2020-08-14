package com.example.ethiomusic.util

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.ui.album.adapter.AlbumAdapter
import com.example.ethiomusic.ui.album.adapter.AlbumViewPagerAdapter
import com.example.ethiomusic.ui.album.adapter.ArtistAdapter
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.util.adaper.*
import com.example.ethiomusic.util.viewhelper.ItemTouchHelperCallback
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.fixedRateTimer

@BindingAdapter("app:newalbumlist")
fun RecyclerView.addDatatoViewpager(data: List<Album<String,String>>?) {
    data?.let {
        var albumAdapter = AlbumAdapter()
        adapter = albumAdapter
        layoutManager = LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
        albumAdapter.submitList(it)
        var initPosition = 0
//        var timer = fixedRateTimer("" , false , 0L , 3000){
//            this@addDatatoViewpager.setCurrentItem((this@addDatatoViewpager.currentItem+1) % it.size , true)
//        }
    }
}


@BindingAdapter("app:albumlistnew")
fun RecyclerView.addHomeData(albumData: List<Album<String,String>>?) {
    albumData?.let {
        var albumAdapter = MediumViewAdapter<BaseModel>("ALBUM")
        adapter = albumAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        var baseData = it.map { album -> album.toBaseModel() }
        albumAdapter.submitList(baseData)
    }
}

@BindingAdapter("app:songlistnew")
fun RecyclerView.addSongDataNew(songData: List<Song<String,String>>?) {
    songData?.let {
        var songAdapter = MediumViewAdapter<BaseModel>("SONG")
        adapter = songAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        var baseDataList = it.map { song -> song.toBaseModel() }
        songAdapter.submitList(baseDataList)
    }
}

@BindingAdapter("app:playlist")
fun RecyclerView.addPlaylist(songData: List<Playlist<String>>?) {
    songData?.let {
        var playlistAdapter = MediumViewAdapter<BaseModel>("PLAYLIST")
        adapter = playlistAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        var baseDataList = it.map { playlist -> playlist.toBaseModel() }
        playlistAdapter.submitList(baseDataList)
    }
}



@BindingAdapter("app:playliststring")
fun RecyclerView.addPlaylistString(songData: List<Playlist<Song<String,String>>>?) {
    songData?.let {
        var playlistAdapter = MediumViewAdapter<BaseModel>("PLAYLIST")
        adapter = playlistAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        var baseDataList = it.map { playlist -> playlist.toBaseModel() }
        playlistAdapter.submitList(baseDataList)
    }
}


private val RecyclerView.linearLayoutManager: LinearLayoutManager
    get() = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

@BindingAdapter("app:library")
fun RecyclerView.addLibrary(libraryData: Library?) {
    var libraryAdapter = MediumViewAdapter<BaseModel>("LIBRARY")
    adapter = libraryAdapter
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    libraryData?.let {
        // code must be modified
        if(it.likedAlbums!!.isNotEmpty() && it.likedSong!!.isNotEmpty() && it.followedArtists!!.isNotEmpty()){
            var likedSong = BaseModel(baseId = it.likedSong?.get(0)?._id , baseTittle = "Liked Songs" , baseSubTittle = "${it.likedSong?.size} Songs" , baseImagePath = R.drawable.backimg.toString()  , baseType = Library.SONG_LIBRARY)
            var favAlbums = BaseModel(baseId = it.likedAlbums?.get(0)?._id , baseTittle = "Favorite Albums" , baseSubTittle = "${it.likedAlbums?.size} Albums"  , baseType = Library.ALBUM_LIBRARY)
            var favArtists = BaseModel(baseId = it.followedArtists?.get(0)?._id , baseTittle = "Favorite Artists" , baseSubTittle = "${it.followedArtists?.size} Artists" ,  baseImagePath = R.drawable.circularimg.toString() , baseType = Library.ARTIST_LIBRARY)
            var baseDataList = listOf(likedSong , favAlbums , favArtists)
            libraryAdapter.submitList(baseDataList)
        }
    }
}

@BindingAdapter("app:recentactivity")
fun RecyclerView.AddRecentActivities(activities: List<RecentActivity>?) {
    activities?.let {
        var miniAdapter = MiniViewAdapter<BaseModel>()
        adapter = miniAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        var baseDataList = it.map { recent -> recent.toBaseModel() }
        miniAdapter.submitList(baseDataList)
    }
}


@BindingAdapter("app:loadingvisibility")
fun View.CheckHomeLoadinVisbility(homeResource: Resource<Home>?) {
    when (homeResource) {
        is Resource.Loading -> visibility = View.VISIBLE
        is Resource.Success -> visibility = View.GONE
    }
}

@BindingAdapter("app:viewvisibility")
fun View.CheckViewVisbility(homeResource: Resource<Home>?) {
    when (homeResource) {
        is Resource.Loading -> visibility = View.GONE
        is Resource.Success -> visibility = View.VISIBLE
    }
}



//////////////////////////////////////////////////////////////////////////////////////////////////////////

@BindingAdapter("app:progressVisibility")
fun View.isVisible(data : Any?) {
     isVisible =  data == null
}

@BindingAdapter("app:isViewVisibility")
fun View.checkViewVisibility(data : Any?) {
    isVisible =  data != null
}


@BindingAdapter("app:loadImage")
fun ImageView.loadImage(imageUrl : String?) {
    imageUrl?.let {
        Picasso.get().load(imageUrl).placeholder(R.drawable.ic_audiotrack_black_24dp).into(this)
    }
}

@BindingAdapter("app:info"  , "app:list"  )
fun RecyclerView.addData(info : RecyclerViewHelper<Song<String,String>>, l : List<Song<String,String>>?){
    l?.let {
        var songAdapter = SongAdapter(info)
        this.layoutManager = LinearLayoutManager(this.context)
        if(info.type == "PLAYLIST"){
            var itemToucHelper = ItemTouchHelper(ItemTouchHelperCallback(songAdapter , info.type!!))
            songAdapter.addTouchHelper(itemToucHelper)
            itemToucHelper.attachToRecyclerView(this)
        }
        this.adapter = songAdapter
        songAdapter.submitList(it)
    }
}

@BindingAdapter("app:artistinfo" , "app:artistlist" )
fun RecyclerView.addArtistData(info : RecyclerViewHelper<Artist<String,String>>, list : List<Artist<String,String>>?){
    list?.let {
       var artistAdapter = ArtistAdapter(info)
        this.layoutManager = LinearLayoutManager(this.context , LinearLayoutManager.HORIZONTAL , false)
        this.adapter = artistAdapter
        artistAdapter.submitList(it)
    }
}



@BindingAdapter("app:albuminfo" , "app:albumlist" )
fun RecyclerView.addPlaylistData(info : RecyclerViewHelper<BaseModel>, list : List<Album<String,String>>?){
    list?.let {
        var adapter = LargeViewItemAdapter(info)
        this.layoutManager = GridLayoutManager(this.context , 2)
        this.adapter = adapter
        var baseAlbumInfo = it.map { album -> album.toBaseModel() }
        adapter.submitList(baseAlbumInfo)
    }
}



@BindingAdapter("app:playlistInfo","app:playlistList")
fun RecyclerView.getPlaylists(info : RecyclerViewHelper<Playlist<String>> ,  playlistList: List<Playlist<String>>?) {
    playlistList?.let {
        var playlistAdapter = PlaylistAdapter(info , it.filter { playlist -> !playlist.songs.isNullOrEmpty() })
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = playlistAdapter
    }
}

@BindingAdapter("app:radioList")
fun RecyclerView.addRadioSongs(lists : List<Radio>?){
    lists?.let {
        var radioAdapter = RadioAdapter()
        layoutManager = LinearLayoutManager(context)
        adapter = radioAdapter
        radioAdapter.submitList(it)
    }
}







