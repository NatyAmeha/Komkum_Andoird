package com.zomatunes.zomatunes.ui.artist

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.R

import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.ArtistFragmentBinding
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.ui.album.adapter.ArtistAdapter
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.MiniViewAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleError
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ArtistFragment : Fragment() {


    private val artistViewmodel : ArtistViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : ArtistFragmentBinding

     var artistData : ArtistMetaData? = null

    var artistMetadataResult = MutableLiveData<ArtistMetaData>()

    var artistId : String? = null
    var isArtistInFavorite = false


    init {
        lifecycleScope.launch {
            whenCreated{
                arguments?.getString("ARTISTID")?.let {
                    artistId = it
                    isArtistInFavorite = artistViewmodel.isArtistInUserFavorite(it) ?: false
                    artistMetadataResult.value =  artistViewmodel.getArtistMetadata(artistId!!).data
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().window.statusBarColor = resources.getColor(R.color.trnasparentColor)
        binding = ArtistFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(verticalOffset == 0) binding.artistToolbar.visibility = View.INVISIBLE
            else if(verticalOffset == -appBarLayout.totalScrollRange) binding.artistToolbar.visibility = View.VISIBLE
        })
        Log.i("itemitem2" , isArtistInFavorite.toString())


         artistId?.let {
             artistMetadataResult.observe(viewLifecycleOwner , Observer{
                 configureActionBar(binding.artistToolbar , it.artist.name)
                 artistData = it
                 binding.lifecycleOwner = viewLifecycleOwner
                 binding.artistMetadata = artistData
                 binding.isArtistFavorite = isArtistInFavorite
                 binding.artistNameTextview.text = it.artist.name
                 binding.artistFollowersTextview.text = "${it.artist.followersCount} Followers"
                 var imageUrl = it.artist.profileImagePath[0].replace("localhost" , AdapterDiffUtil.URL)
                 Picasso.get().load(imageUrl).placeholder(R.drawable.backimg).fit().centerInside().into(binding.artistProfileImage)

                 displayLatest(it)
                 displayTopSongs(it.artist.singleSongs)
                 displayTopAlbums(it.artist.albums)
                 displayArtists()

                 mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
                     var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                     songId?.let {
                         mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                             var a = it::class.qualifiedName
                             Log.i("typemediainfo" , "song")
                             when(a){
                                 "com.example.com.zomatunes.zomatunes.data.model.Song"  ->{
                                     Log.i("typemedia" , it.tittle)
                                     mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>}
                             }
                         }
                     }
                 })
             })
         }

        binding.artistInfoBtn.setOnClickListener {
            artistData?.let { it1 -> (requireActivity() as MainActivity).movetoArtistBio(it1) }
        }

        binding.artistFollowBtn.setOnClickListener {
            artistViewmodel.followArtists(listOf(artistId!!)).handleSingleDataObservation(viewLifecycleOwner){
                 isArtistInFavorite = it
                 binding.isArtistFavorite = it
                (requireActivity() as MainActivity).showSnacbar("Artist added to favorite")
            }
        }

        binding.artistUnfollowBtn.setOnClickListener{
            artistViewmodel.unfollowArtists(listOf(artistId!!)).handleSingleDataObservation(viewLifecycleOwner){
                isArtistInFavorite = !it
                binding.isArtistFavorite = !it
                (requireActivity() as MainActivity).showSnacbar("Artist removed from favorite")
            }
        }

        binding.allAlbumsBtn.setOnClickListener {
//            (requireActivity() as MainActivity).moveToAlbumListFragment("Albums" , data.baseId!!)
        }
        binding.allSongsBtn.setOnClickListener {
            artistData?.let { (requireActivity() as MainActivity).movetoSongListFragment(null , it.artist.singleSongs) }
        }

        artistViewmodel.getError().observe(viewLifecycleOwner){}
        artistViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.artistErrorTextview.isVisible = true
                binding.artistLoadingProgressbar.isVisible = false
            }
        }
    }

    override fun onPause() {
        mainActivityViewmodel.isAudioPrepared = false
        super.onPause()
    }

    fun displayLatest(metadata : ArtistMetaData){
        var latestSong = metadata.artist.singleSongs.maxBy { song -> song.dateCreated!! }
        var latestAlbum = metadata.artist.albums.maxBy { album -> album.dateCreated!! }

        latestSong?.let {
            if(latestAlbum == null || it.dateCreated!! > latestAlbum.dateCreated){
                Picasso.get().load(latestSong.thumbnailPath!!.replace("localhost" , AdapterDiffUtil.URL))
                    .placeholder(R.drawable.backimg).fit().centerInside().into(binding.latestContentImageview)
                binding.latestNameTextview.text = latestSong.tittle
                val cal = Calendar.getInstance()
                cal.time = it.dateCreated
                binding.latestDateTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}  ${cal.get(Calendar.YEAR)}"
                binding.latestTypeTextview.text = "Single"

                binding.latestReleaseContainer.setOnClickListener{ prepareAndPlaySongs(listOf(latestSong!!) , 0) }
                return
            }
        }

        latestAlbum?.let {
            if(latestSong == null ||  it.dateCreated!! > latestSong.dateCreated){
                Picasso.get().load(latestAlbum.albumCoverPath!!.replace("localhost" , AdapterDiffUtil.URL))
                    .placeholder(R.drawable.backimg).fit().centerInside().into(binding.latestContentImageview)
                binding.latestNameTextview.text = latestAlbum.name
                binding.latestDateTextview.text = latestAlbum.dateCreated!!.toLocaleString()
                binding.latestTypeTextview.text = "Album"

                binding.latestReleaseContainer.setOnClickListener{ (requireActivity() as MainActivity).movefromArtisttoAlbum(latestAlbum!!._id) }
                return
            }
        }

    }

    fun displayTopSongs(songs : List<Song<String,String>>){
        var topSongs = songs.sortedByDescending { song -> song.monthlyStreamCount }
        var listener = object : IRecyclerViewInteractionListener<Song<String, String>> {
            override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
                artistData?.let {
                    mainActivityViewmodel.prepareAndPlaySongsBeta(topSongs , PlayerState.PlaylistState() , position , false , false)
                    mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                }
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }
        var songInfo = RecyclerViewHelper("ARTIST" , mainActivityViewmodel.songrecyclerviewStateManeger , listener , viewLifecycleOwner)
        var songAdapter = SongAdapter(songInfo)
        binding.topSongRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.topSongRecyclerview.adapter = songAdapter

        songAdapter.submitList(topSongs.take(4))
    }

    fun displayTopAlbums(albums : List<Album<Song<String,String> , String>>){
        var albumStateManager =  RecyclerviewStateManager<BaseModel>()
        var listener = object : IRecyclerViewInteractionListener<BaseModel> {
            override fun onItemClick(
                data: BaseModel,
                position: Int,
                option: Int?
            ) {
                (requireActivity() as MainActivity).movefromArtisttoAlbum(data.baseId!!)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}

            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var albumAdapter = MiniViewAdapter<BaseModel>(albumStateManager, listener, viewLifecycleOwner, "ALBUM")
        binding.topAlbumRecyclerview.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL , false)
        binding.topAlbumRecyclerview.adapter = albumAdapter

        var baselist = albums
            .sortedByDescending { album -> album.favoriteCount }
            .map { album -> album.toBaseModel().apply { this.baseSubTittle = "${album.songs?.size.toString()} songs" }
        }
        albumAdapter.submitList(baselist)
    }

    fun displayArtists(){
       var artistStataManager = RecyclerviewStateManager<Artist<String,String>>()
        var listener = object : IRecyclerViewInteractionListener<Artist<String, String>> {
            override fun onItemClick(
                data: Artist<String, String>,
                position: Int,
                option: Int?
            ) {
                (requireActivity() as MainActivity).moveToArtist(data._id)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var info = RecyclerViewHelper("ARTIST" , artistStataManager , listener , viewLifecycleOwner)
        artistViewmodel.popularArtists.handleSingleDataObservation(viewLifecycleOwner){
            var adapter = ArtistAdapter(info)
            binding.similarArtistRecyclerview.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL , false)
            binding.similarArtistRecyclerview.adapter = adapter
            binding.artistTextview.visibility = View.VISIBLE
            var list = it.toMutableList().filter { artist -> artist._id != artistData!!.artist._id }
            adapter.submitList(list)
        }
    }

    fun prepareAndPlaySongs(songs : List<Song<String,String>> ,  position: Int?){
        if (!mainActivityViewmodel.isAudioPrepared) {
            mainActivityViewmodel.playlistQueue.value = songs
            mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_MEDIA)
        }
        position?.let {
            mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
            mainActivityViewmodel.play(it.toLong())
        }
    }

}

