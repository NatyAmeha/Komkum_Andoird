package com.zomatunes.zomatunes.util

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.ui.album.adapter.AlbumAdapter
import com.zomatunes.zomatunes.ui.album.adapter.ArtistAdapter
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.util.adaper.*
import com.zomatunes.zomatunes.util.constants.AppConstant
import com.zomatunes.zomatunes.util.constants.AppConstant.AdapterConstant.*
import com.zomatunes.zomatunes.util.viewhelper.HorizontalMarginItemDecoration
import com.zomatunes.zomatunes.util.viewhelper.ItemTouchHelperCallback
import com.squareup.picasso.Picasso
import java.lang.Math.abs

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
        var miniAdapter = MiniViewAdapter<BaseModel>(type = "RECENT")
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
        Picasso.get().load(imageUrl.replace("localhost" , AdapterDiffUtil.URL)).placeholder(R.drawable.ic_audiotrack_black_24dp).into(this)
    }
}

@BindingAdapter("app:loadImagebeta" , "app:placeholder")
fun ImageView.loadImageBeta(imageUrl : String? , placeholder : Int) {
    imageUrl?.let {
        Picasso.get().load(imageUrl.replace("localhost" , AdapterDiffUtil.URL)).placeholder(placeholder).into(this)
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
        this.layoutManager =when(info.layoutOrientation){
            AppConstant.AdapterConstant.GRID_ORIENTATION -> GridLayoutManager(context , 3)
            else ->  LinearLayoutManager(this.context , LinearLayoutManager.HORIZONTAL , false)
        }
        this.adapter = artistAdapter
        artistAdapter.submitList(it)
    }
}

@BindingAdapter("app:authorInfo" , "app:authorList")
fun RecyclerView.addAuthorList(info : RecyclerViewHelper<Author<String,String>> , list : List<Author<String,String>>?){
    list?.let {
        var authorAdapter = AuthorAdapter(info , list)
        when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL ->  layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 -> layoutManager = GridLayoutManager(context , 3)
        }
        adapter = authorAdapter
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
        var pl =
            if(info.type == "PLAYLIST") it.filter { playlist -> !playlist.songs.isNullOrEmpty() }
            else it
        var playlistAdapter = PlaylistAdapter(info , pl)
        when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL ->  layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> layoutManager = GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 -> layoutManager = GridLayoutManager(context , 3)
        }
        adapter = playlistAdapter
    }
}

@BindingAdapter("app:radioInfo","app:radioList")
fun RecyclerView.addRadioSongs(info : RecyclerViewHelper<Radio> , lists : List<Radio>?){
    lists?.let {
        var radioAdapter = RadioAdapter(info)
        layoutManager = LinearLayoutManager(context)
        adapter = radioAdapter
        radioAdapter.submitList(it)
    }
}


@BindingAdapter("app:radioInfo","app:radioviewpagerlist")
fun ViewPager2.addRadioListToViewpager(info : RecyclerViewHelper<Radio> , lists : List<Radio>?){
    lists?.let {
        var radioMap = lists.groupBy { radio -> radio.radioQueryTag.category!! }
        var radioAdapter = RadioViewpagerAdapter(info , radioMap)
        adapter = radioAdapter
    }
    clipToPadding = false
    offscreenPageLimit = 1

// Add a PageTransformer that translates the next and previous items horizontally
// towards the center of the screen, which makes them visible
    val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
    val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
    val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
    val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
        page.translationX = -pageTranslationX * position
        // Next line scales the item's height. You can remove it if you don't want this effect
//        page.scaleY = 1 - (0.25f * abs(position))
        // If you want a fading effect uncomment the next line:
        // page.alpha = 0.25f + (1 - abs(position))
    }
    setPageTransformer(pageTransformer)
}

@BindingAdapter("bookInfo" , "app:bookViewpagerList")
fun ViewPager2.addBookCollectionToViewpager(info: RecyclerViewHelper<BookCollection> , collection: List<BookCollection>?){
    collection?.let {
        var collectionMap = collection.groupBy { collection -> collection.genre!![0] }
        var collectionAdapter = BookCollectionViewpagerAdapter(info , collectionMap)
        adapter = collectionAdapter
    }


    offscreenPageLimit = 1

// Add a PageTransformer that translates the next and previous items horizontally
// towards the center of the screen, which makes them visible
    val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
    val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
    val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
    val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
        page.translationX = -pageTranslationX * position
        // Next line scales the item's height. You can remove it if you don't want this effect
//        page.scaleY = 1 - (0.25f * abs(position))
        // If you want a fading effect uncomment the next line:
        // page.alpha = 0.25f + (1 - abs(position))
    }
    setPageTransformer(pageTransformer)

// The ItemDecoration gives the current (centered) item horizontal margin so that
// it doesn't occupy the whole screen width. Without it the items overlap
//    val itemDecoration = HorizontalMarginItemDecoration(context, R.dimen.viewpager_current_item_horizontal_margin)
//    addItemDecoration(itemDecoration)

}


@BindingAdapter("app:bookInfo" , "app:audiobookList")
fun RecyclerView.addBookList(info : RecyclerViewHelper<Book<String>>, list : List<Book<String>>?){
    list?.let {
        var bookAdapter = BookAdapter(info , it)
        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 -> GridLayoutManager(context , 3)
            else -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        adapter = bookAdapter
    }
}

@BindingAdapter("app:bookCollectionInfo" , "app:bookCollection")
fun RecyclerView.addBookCollection(info : RecyclerViewHelper<BookCollection> , collection : List<BookCollection>?){
    collection?.let {
        var bookCollectionAdapter = BookCollectionAdapter(info , it)
        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
            else -> LinearLayoutManager(context)
        }
        adapter = bookCollectionAdapter
    }
}

@BindingAdapter("app:browseInfo" , "app:browseStringList")
fun RecyclerView.addBrowseString(info : RecyclerViewHelper<MusicBrowse> , browseStringList : List<MusicBrowse>?){
    browseStringList?.let {
        val browseAdapter = BrowseAdapter(info , it)
        layoutManager = GridLayoutManager(context , 2)
        adapter = browseAdapter
    }

}







