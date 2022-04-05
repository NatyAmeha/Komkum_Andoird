package com.komkum.komkum.util

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.ui.album.adapter.AlbumAdapter
import com.komkum.komkum.ui.album.adapter.ArtistAdapter
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.adaper.*
import com.komkum.komkum.util.constants.AppConstant
import com.komkum.komkum.util.viewhelper.ItemTouchHelperCallback
import com.squareup.picasso.Picasso
import java.util.*

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
            var likedSong = BaseModel(baseId = it.likedSong?.get(0)?._id , baseTittle = "Liked Songs" , baseSubTittle = "${it.likedSong?.size} Songs" , baseImagePath = R.drawable.music_placeholder.toString()  , baseType = Library.SONG_LIBRARY)
            var favAlbums = BaseModel(baseId = it.likedAlbums?.get(0)?._id , baseTittle = "Favorite Albums" , baseSubTittle = "${it.likedAlbums?.size} Albums"  , baseType = Library.ALBUM_LIBRARY)
            var favArtists = BaseModel(baseId = it.followedArtists?.get(0)?._id , baseTittle = "Favorite Artists" , baseSubTittle = "${it.followedArtists?.size} Artists" ,  baseImagePath = R.drawable.ic_person_black_24dp.toString() , baseType = Library.ARTIST_LIBRARY)
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
        Picasso.get().load(imageUrl.replace("localhost" , AdapterDiffUtil.URL)).fit().centerCrop().placeholder(R.drawable.music_placeholder).into(this)
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
        var songREsult = if(info.type == "ALBUM") it.sortedBy { song -> song.trackNumber } else it
        songAdapter.submitList(songREsult)
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

@BindingAdapter("app:radioInfoo","app:radioList")
fun RecyclerView.addRadioList(info : RecyclerViewHelper<Radio> ,  radioList: List<Radio>?) {
    radioList?.let {

        var radioAdapter = RadioAdapter(info)
        when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL ->  layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> layoutManager = GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 -> layoutManager = GridLayoutManager(context , 3)
        }
        adapter = radioAdapter
        radioAdapter.submitList(radioList)


    }
}


@BindingAdapter("app:radioInfo","app:radioviewpagerlist")
fun ViewPager2.addRadioListToViewpager(info : RecyclerViewHelper<Radio> , lists : List<Radio>?){
    lists?.let {
        var radioMap = lists.groupBy { radio -> radio.radioQueryTag?.category!! }
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


@BindingAdapter("app:podcastInfo" , "app:podcastList")
fun RecyclerView.addPodcastList(info : RecyclerViewHelper<Podcast<String>>, list : List<Podcast<String>>?){
    list?.let {
        var podcastAdapter = PodcastAdapter(info ,it)
        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 -> GridLayoutManager(context , 3)
            else -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        adapter = podcastAdapter
    }
}


@BindingAdapter("app:episodeInfo" , "app:episodeList" , "app:podcastChecked")
fun RecyclerView.addEpisodeList(info : RecyclerViewHelper<PodcastEpisode>?, list : List<PodcastEpisode>? , podcastChecked : Date? = Date()){
    info?.let {
        list?.let {
            var episodeAdapter = PodcastEpisodeAdapter(info , it.reversed(), podcastChecked)
            layoutManager = LinearLayoutManager(context)
            adapter = episodeAdapter
        }
    }
}


@BindingAdapter("app:commentInfo" , "app:commentList")
fun RecyclerView.addCommentList(info : RecyclerViewHelper<EpisodeComment<String>>, list : List<EpisodeComment<String>>?){
    list?.let {
        var commentAdapter = EpisodeCommentAdapter(info)
        layoutManager = LinearLayoutManager(context)
        adapter = commentAdapter
    }
}

////////////////////////////////////////////////// product bindng adapters /////////////////////////////////////////

@BindingAdapter("app:productInfo" , "app:productList")
fun RecyclerView.addProductList(info : RecyclerViewHelper<Product> , productList : List<Product>?){
    productList?.let {
        var productAdapter = ProductAdapter(info , it)
        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            else -> LinearLayoutManager(context)
        }
        adapter = productAdapter
    }
}


@BindingAdapter("app:teamproductInfo" , "app:teamproductList" , "app:additionalQty")
fun RecyclerView.addProductListInTeamDetails(info : RecyclerViewHelper<Product> , productList : List<Product>? , additionalQty : Int = 0){
    productList?.let {
        var productAdapter = ProductAdapter(info , it , additionalQty)
        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            else -> LinearLayoutManager(context)
        }
        adapter = productAdapter
    }
}


@BindingAdapter("app:orderedItemInfo" , "app:orderedItemList")
fun RecyclerView.addOrderedItemList(info : RecyclerViewHelper<OrderedItem<Product>> , items : List<OrderedItem<Product>>?){
    items?.let {
        var orderedItemAdapter = OrderedItemAdapter(info , it)
        layoutManager = LinearLayoutManager(context)
        adapter = orderedItemAdapter
    }
}


@BindingAdapter("app:mainTeamInfo" , "app:mainteamList")
fun RecyclerView.addToMainTeamList(info : RecyclerViewHelper<Team<Product>> , teamList : List<Team<Product>>?){
    teamList?.let {
        var sortedTEams =  it.sortedBy { it.teamSize?.minus(it.members?.size ?: 0) }
        var teamAdapter = TeamAdapter(info , sortedTEams)

        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_VERTICAL -> LinearLayoutManager(context)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
            else -> GridLayoutManager(context , 2)
        }
        adapter = teamAdapter
    }
}


@BindingAdapter("app:packageInfo" , "app:packageList")
fun RecyclerView.addPackageList(info : RecyclerViewHelper<ProductPackage<Product>>, packageList : List<ProductPackage<Product>>?){
    packageList?.let {
        var packageAdapter = PackageAdapter(info , packageList)
        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL -> LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
            else -> LinearLayoutManager(context , LinearLayoutManager.HORIZONTAL , false)
        }
        adapter = packageAdapter

    }
}


@BindingAdapter("app:teamWithProductInfo" , "app:teamList")
fun RecyclerView.addTeamWithProductList(info : RecyclerViewHelper<Team<Product>> , teamList : List<Team<Product>>?){
    teamList?.let {
       var sortedTEams =  it.sortedBy { it.teamSize?.minus(it.members?.size ?: 0) }
        var teamAdapter = TeamAdapter(info , sortedTEams)

        layoutManager = when(info.layoutOrientation){
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 -> GridLayoutManager(context , 2)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_VERTICAL -> LinearLayoutManager(context)
            else -> GridLayoutManager(context , 2)
    }
        adapter = teamAdapter
    }
}


@BindingAdapter("app:teamInfo" , "app:teamList")
fun RecyclerView.addTeamList(info : RecyclerViewHelper<Team<String>> , teamList : List<Team<String>>?){
    teamList?.let {
        var teamAdapter = TeamAdapter(info , it)
        layoutManager =  LinearLayoutManager(context)
        adapter = teamAdapter
    }
}

@BindingAdapter("app:teamMemberInfo" , "app:teamMemberList" , "app:totalPrice")
fun RecyclerView.addTeamMemberList(info : RecyclerViewHelper<TeamMember> , teamList : List<TeamMember>? , totalProductPrice : Int = 0){
    teamList?.let {
        var teamAdapter = TeamMemberAdapter(info , it , totalProductPrice)
        layoutManager =  LinearLayoutManager(context)
        adapter = teamAdapter
    }
}



@BindingAdapter("app:categoryInfo" , "app:categoryList" , "app:type")
fun RecyclerView.addProductCategory(info : RecyclerViewHelper<Category>, categoryList : List<ProductCategory>? , type : String? = null){
    categoryList?.let {

        var categoryList = mutableListOf<Category>()
        it.forEach { dep -> categoryList.addAll(dep.subcategory) }
        var depOrCategory  = if(type =="dep") it else categoryList
        var productBrowseAdapter = ProductBrowseAdapter(info , depOrCategory)
        layoutManager = when(info.layoutOrientation) {
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3 -> GridLayoutManager(context , 4)
            RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_HORIZONTAL_GRID_2 -> GridLayoutManager(context, 2,  LinearLayoutManager.HORIZONTAL, false)
            else -> LinearLayoutManager(context)
        }
        adapter = productBrowseAdapter
    }
}


@BindingAdapter("app:addressInfo" , "app:addressList")
fun RecyclerView.addAddressList(info : RecyclerViewHelper<Address>, addressList : List<Address>?){
    addressList?.let {
        var addressAdapter = AddressAdapter(info , it)
        layoutManager = LinearLayoutManager(context)
        adapter = addressAdapter
    }
}






