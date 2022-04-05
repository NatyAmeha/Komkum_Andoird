package com.komkum.komkum.ui.artist

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.*
import android.view.MenuItem
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.*
import com.komkum.komkum.R


import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.ArtistFragmentBinding
import com.komkum.komkum.ui.album.adapter.ArtistAdapter
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.MiniViewAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import com.google.android.material.appbar.AppBarLayout
import com.komkum.komkum.ui.account.AccountState
import com.squareup.picasso.Picasso
import com.komkum.komkum.ui.album.AlbumListViewModel
import com.komkum.komkum.ui.component.imageComposable
import com.komkum.komkum.ui.component.leaderboardList
import com.komkum.komkum.ui.component.leaderboardWithHeader
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.extensions.showListDialog
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ArtistFragment : Fragment() , IRecyclerViewInteractionListener<Song<String, String>> {


    private val artistViewmodel : ArtistViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : ArtistFragmentBinding

    var artistData : ArtistMetaData? = null

    var artistMetadataResult = MutableLiveData<ArtistMetaData>()
    var donationLeaderboard = MutableLiveData<List<Donation>>()
    var similarArtistResult = MutableLiveData<List<Artist<String , String>>>()

    var artistId : String? = null
    var singleSongs : List<Song<String,String>> = emptyList()
    var isArtistInFavorite = false
    var follwerCount = 0


    init {
        lifecycleScope.launch {
            whenCreated{
                arguments?.getString("ARTISTID")?.let {
                    artistId = it
                    isArtistInFavorite = artistViewmodel.isArtistInUserFavorite(it) ?: false
                    artistMetadataResult.value =  artistViewmodel.getArtistMetadata(artistId!!).data ?: null
                    similarArtistResult.value = artistViewmodel.getSimilarArtists(it).data ?: null
                    Log.i("itemitem" , isArtistInFavorite.toString())
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
//        var application = requireActivity().application as ZomaTunesApplication
//        application.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (requireActivity() as MainActivity).showBottomViewBeta()
        binding = ArtistFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(verticalOffset == 0){
                binding.artistToolbar.visibility = View.INVISIBLE
//                binding.mainlayout.setBackgroundColor(resources.getColor(R.color.primaryDarkColor))
//                binding.mainlayout.setStatusBarBackgroundColor(resources.getColor(R.color.primaryDarkColor))
//                binding.artistCollapsingToolbar.setBackgroundColor(resources.getColor(R.color.transparent))
            }
            else if(verticalOffset == -appBarLayout.totalScrollRange){
                binding.artistToolbar.visibility = View.VISIBLE
//                binding.mainlayout.setStatusBarBackgroundColor(resources.getColor(R.color.background))
//                binding.mainlayout.background = resources.getDrawable(R.drawable.item_selected)
//                binding.artistCollapsingToolbar.setBackgroundColor(resources.getColor(R.color.background))
            }
        })

        var pref = PreferenceHelper.getInstance(requireContext())
        pref[AccountState.IS_REDIRECTION] = true

        var songInfo = RecyclerViewHelper("ARTIST" , mainActivityViewmodel.songrecyclerviewStateManeger , this , viewLifecycleOwner)


        artistId?.let {
             artistMetadataResult.observe(viewLifecycleOwner , Observer{
                 it?.let {
                     configureActionBar(binding.artistToolbar, it.artist.name)

                     artistData = it
                     follwerCount = it.artist.followersCount
                     binding.artistDonateBtn.isVisible = it.artist.donationEnabled ?: true
                     binding.lifecycleOwner = viewLifecycleOwner
                     binding.artistMetadata = artistData
                     binding.isArtistFavorite = isArtistInFavorite
                     binding.artistNameTextview.text = it.artist.name
                     var rank = it.rank + 1
                     if(rank <=20){
                         binding.rankTextview.text = rank.toString()
                         binding.rankIndicatorTextview.text =
                             if(rank == 1) getString(R.string.st)
                             else if(rank == 2) getString(R.string.nd)
                             else if(rank == 3) getString(R.string.rd)
                             else getString(R.string.th)
                         binding.textView122.isVisible = true
                     }

                     binding.donationLeaderboardComposeview.setContent {
                         ZomaTunesTheme(true) {
                             donationLeaderboardSection(artistId = artistId!!, artistName = artistData!!.artist.name , artistImage = artistData!!.artist.profileImagePath!![0])

                         }
                     }

                     binding.artistFollowersTextview.text = "$follwerCount ${getString(R.string.followers)}"
                     var imageUrl = it.artist.profileImagePath?.getOrNull(0)
                     Picasso.get().load(imageUrl).placeholder(R.drawable.music_placeholder).fit()
                         .centerCrop().into(binding.artistProfileImage)

                     binding.artistRadioCardView.setOnClickListener {view ->
                         (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = it.artist._id)
                     }

                     displayLatest(it)
                     it.topSongs?.let { topsongs -> displayTopSongs(topsongs) }
                     displaySingleSongs(it.artist.singleSongs , songInfo)
                     displayTopAlbums(it.artist.albums)
                     displayArtists(it.artist._id)

                     mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
                             var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                             songId?.let {
                                 mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                                     var a = it::class.qualifiedName
                                     Log.i("typemediainfo", "song")
                                     when (a) {
                                         "com.komkum.komkum.data.model.Song" -> {
                                             it.tittle?.let { it1 -> Log.i("typemedia", it1) }
                                             mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>
                                         }
                                     }
                                 }
                             }
                         })
                 }
             })

         }

        binding.artistDonateBtn.setOnClickListener {
            if(artistData?.artist?.user!= null && artistId != null){
                (requireActivity() as MainActivity).moveToDonation(Donation.ARTIST_DONATION , artistData!!.artist.user , artistData!!.artist.name , artistId!! , artistData!!.artist.profileImagePath!![0])
            }
        }

        binding.artistFollowBtn.setOnClickListener {
            binding.artistLoadingProgressbar.isVisible = true
            artistViewmodel.followArtists(listOf(artistId!!)).handleSingleDataObservation(viewLifecycleOwner){
                binding.artistLoadingProgressbar.isVisible = false
                isArtistInFavorite = it
                binding.isArtistFavorite = it
                if(it){
                    artistData!!.artist.followersCount+= 1
                    follwerCount += 1
                    binding.artistFollowersTextview.text = "$follwerCount Followers"
                    (requireActivity() as MainActivity).showSnacbar("Artist added to favorite")
                }

            }
        }

        binding.artistUnfollowBtn.setOnClickListener{
            binding.artistLoadingProgressbar.isVisible = true
            artistViewmodel.unfollowArtists(listOf(artistId!!)).handleSingleDataObservation(viewLifecycleOwner){
                binding.artistLoadingProgressbar.isVisible = false
                isArtistInFavorite = !it
                binding.isArtistFavorite = !it
                if(it) {
                    artistData!!.artist.followersCount -= 1
                    follwerCount -= 1
                    binding.artistFollowersTextview.text = "$follwerCount Followers"
                    (requireActivity() as MainActivity).showSnacbar("Artist removed from favorite")

                }
            }
        }

        binding.allSongsBtn.setOnClickListener {
            artistData?.let { (requireActivity() as MainActivity).movetoSongListFragment(null , it.artist.singleSongs) }
        }

        artistViewmodel.getError().observe(viewLifecycleOwner){}
        artistViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.artistLoadingProgressbar.isVisible = false
            error?.handleError(requireContext() , {artistViewmodel.removeOldErrors()}){
                binding.artistErrorTextview.isVisible = true
                binding.artistLoadingProgressbar.isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.artist_menu , menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            R.id.artist_info_menu_item ->{
                artistData?.let { it1 -> (requireActivity() as MainActivity).movetoArtistBio(it1) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        mainActivityViewmodel.isAudioPrepared = false
        super.onPause()
    }

    fun displayLatest(metadata : ArtistMetaData){
        var latestSong =
            metadata.artist.singleSongs.maxByOrNull { song -> song.dateCreated!! }
        var latestAlbum = metadata.artist.albums.maxByOrNull { album -> album.dateCreated!! }

        if(latestSong == null && latestAlbum == null){
            binding.latestReleaseContainer.isVisible = false
            binding.latestReleaseTextview.isVisible = false
            return
        }
        latestSong?.let {
            if(latestAlbum == null || it.dateCreated!! > latestAlbum.dateCreated){
                Picasso.get().load(latestSong.thumbnailPath!!.replace("localhost" , AdapterDiffUtil.URL))
                    .placeholder(R.drawable.music_placeholder).fit().centerCrop().into(binding.latestContentImageview)
                binding.latestNameTextview.text = latestSong.tittle
                val cal = Calendar.getInstance()
                cal.time = it.dateCreated!!
                binding.latestDateTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}  ${cal.get(Calendar.YEAR)}"
                binding.latestTypeTextview.text = "Single"

                binding.latestReleaseContainer.setOnClickListener{
                    mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(latestSong) , PlayerState.PlaylistState() , 0)
                }
                return
            }
        }

        latestAlbum?.let {
            if(latestSong == null ||  it.dateCreated!! > latestSong.dateCreated){
                Picasso.get().load(latestAlbum.albumCoverPath!!.replace("localhost" , AdapterDiffUtil.URL))
                    .placeholder(R.drawable.album_placeholder).fit().centerCrop().into(binding.latestContentImageview)
                binding.latestNameTextview.text = latestAlbum.name
                val cal = Calendar.getInstance()
                cal.time = it.dateCreated
                binding.latestDateTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}  ${cal.get(Calendar.YEAR)}"
                binding.latestTypeTextview.text = "Album"

                binding.latestReleaseContainer.setOnClickListener{ (requireActivity() as MainActivity).movefromArtisttoAlbum(latestAlbum!!._id) }
                return
            }
        }
    }

    fun displaySingleSongs(songs : List<Song<String,String>> , helper : RecyclerViewHelper<Song<String,String>>){
         singleSongs = songs.sortedByDescending { song -> song.monthlyStreamCount }

        var songAdapter = SongAdapter(helper)
        binding.singleSongRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.singleSongRecyclerview.adapter = songAdapter
        songAdapter.submitList(singleSongs.take(4))
        binding.allSongsBtn.setOnClickListener {
            (requireActivity() as MainActivity).movetoSongListFragment(null , songs)
        }
    }


    fun displayTopSongs(songs : List<Song<String,String>> ){

        if(songs.isEmpty()) binding.textView37.isVisible = false
        else{
            var topsongListener = object : IRecyclerViewInteractionListener<Song<String, String>>{
                override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
                   songClickHandler(data , position , option , songs)
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}

            }
            var topSongInfo = RecyclerViewHelper("ARTIST" , mainActivityViewmodel.songrecyclerviewStateManeger , topsongListener , viewLifecycleOwner)
            var songAdapter = SongAdapter(topSongInfo)
            binding.topSongRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.topSongRecyclerview.adapter = songAdapter
            songAdapter.submitList(songs)
        }


    }



    fun displayTopAlbums(albums : List<Album<String , String>>){
        var albumStateManager =  RecyclerviewStateManager<BaseModel>()
        var listener = object : IRecyclerViewInteractionListener<BaseModel> {
            override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movefromArtisttoAlbum(data.baseId!!)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var albumAdapter = MiniViewAdapter(albumStateManager, listener, viewLifecycleOwner, "ALBUM")
        binding.topAlbumRecyclerview.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL , false)
        binding.topAlbumRecyclerview.adapter = albumAdapter

        var baselist = albums.sortedByDescending { album -> album.favoriteCount }.map { album -> album.toBaseModel().apply { this.baseSubTittle = "${album.songs?.size.toString()} songs" } }.take(4)
        albumAdapter.submitList(baselist)

        binding.allAlbumsBtn.setOnClickListener {
            (requireActivity() as MainActivity).moveToAlbumListFragment("${getString(R.string.favorite_albums)} (${albums.size})" , AlbumListViewModel.OTHER , albumList = albums)
        }
    }

    fun displayArtists(artistId : String){
        var artistStataManager = RecyclerviewStateManager<Artist<String,String>>()
        var listener = object : IRecyclerViewInteractionListener<Artist<String, String>> {
            override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToArtist(data._id)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var info = RecyclerViewHelper("ARTIST" , artistStataManager , listener , viewLifecycleOwner)

            similarArtistResult.observe(viewLifecycleOwner){
            var adapter = ArtistAdapter(info)
            binding.similarArtistRecyclerview.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL , false)
            binding.similarArtistRecyclerview.adapter = adapter
            binding.artistTextview.visibility = View.VISIBLE
            var list = it.toMutableList().filter { artist -> artist._id != artistData!!.artist._id }
            adapter.submitList(list)
        }
    }

//    fun prepareAndPlaySongs(songs : List<Song<String,String>> ,  position: Int?){
//        if (!mainActivityViewmodel.isAudioPrepared) {
//            mainActivityViewmodel.playlistQueue.value = songs.toMutableList()
//            mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_MEDIA)
//        }
//        position?.let {
//            mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
//            mainActivityViewmodel.play(it.toLong())
//        }
//    }



    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        songClickHandler(data , position , option , singleSongs)
    }
    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    fun songClickHandler(data : Song<String , String> , position : Int , option: Int? , songLIst : List<Song<String , String>>){
        when(option){
            SongAdapter.NO_OPTION ->{
                mainActivityViewmodel.prepareAndPlaySongsBeta(songLIst , PlayerState.PlaylistState() , position , false , false , 1)
                mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                mainActivityViewmodel.playedFrom.value = "Now playing"
            }
//                    SongAdapter.PLAY_OPTION ->{
//                        mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
////                        playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
//                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
//
//                    }

            SongAdapter.ADD_TO_OPTION ->{
                (requireActivity() as MainActivity).moveToAddtoFragment(mutableListOf(data._id), LibraryService.CONTENT_TYPE_SONG , mutableListOf(data))
            }
            SongAdapter.DOWNLOAD_OPTION ->{
                var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
                if(isSubscriptionValid){
                    artistViewmodel.downloadSongs(listOf(data))
                    artistViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                    (requireActivity() as MainActivity).showSnacbar("Song added to download" , "View"){
                        findNavController().navigate(R.id.downloadListFragment)
                    }
                }
                else findNavController().navigate(R.id.subscriptionFragment2)

            }

            SongAdapter.SONG_RADIO_OPTION -> {
                (requireActivity() as MainActivity).movetoSingleRadioStation(songId = data._id)
            }

            SongAdapter.ARTIST_RADIO_OPTION ->{
                if(data.artists!!.size > 1){
                    MaterialDialog(requireContext()).show {
                        listItems(items = data.artists){dialog, index, text ->
                            (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![index])
                        }
                    }
                }
                else (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![0])
            }

            SongAdapter.GO_TO_ARTIST_OPTION ->{
                if(data.artists!!.size > 1){
                    data.artistsName?.showListDialog(requireContext() , "Choose Artist"){index, value ->
                        (requireActivity() as MainActivity).moveToArtist(data.artists!![index])
                    }
                }
                else data.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }

            }
            SongAdapter.GO_TO_ALBUM_OPTION -> {
                if(data.album != null){
                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.album!!, false , false)
                }
                else Toast.makeText(requireContext() , "This song is single" , Toast.LENGTH_SHORT).show()
            }

            SongAdapter.SONG_INFO_OPTION ->{
                mainActivityViewmodel.showSongOption(data)
            }
        }

    }


    @Composable
    fun donationLeaderboardSection(artistId: String , artistName : String , artistImage : String) {
        val donations by mainActivityViewmodel.getDonations(artistId).observeAsState()

        if(!donations.isNullOrEmpty()){
            var totalDonations = donations?.map { donation -> donation.amount!! }?.reduce { acc, i -> acc.plus(i) }
            leaderboardWithHeader(
                image = artistImage,
                title = artistName,
                subtitle = "${donations!!.size} donations",
                extra = "ETB $totalDonations",
                items = donations!!.toLeaderboard()
            )
        }
    }


}



