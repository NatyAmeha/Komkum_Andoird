package com.example.ethiomusic.ui.artist

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ethiomusic.*

import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.databinding.ArtistFragmentBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.ui.album.adapter.ArtistAdapter
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.util.adaper.MiniViewAdapter
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtistFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ArtistViewmodelFactory
    private val artistViewmodel : ArtistViewModel by viewModels { GenericSavedStateViewmmodelFactory(viewModelFactory , this) }
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : ArtistFragmentBinding

     var artistData : ArtistMetaData? = null

    var artistId : String? = null
    var isArtistInFavorite = false


    init {
        lifecycleScope.launch {
            whenCreated{
                arguments?.getString("ARTISTID")?.let {
                    artistId = it
                    isArtistInFavorite = artistViewmodel.isArtistInUserFavorite(it)
                    binding.lifecycleOwner = viewLifecycleOwner
                    binding.isArtistFavorite = isArtistInFavorite
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().window.statusBarColor = resources.getColor(R.color.trnasparentColor)
        binding = ArtistFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.artistToolbar)
         artistId?.let {
             artistViewmodel.getArtistMetadata(it).handleSingleDataObservation(viewLifecycleOwner){
                 artistData = it
                 binding.artistMetadata = artistData
                 binding.artistCollapsingToolbar.title  = it.artist.name
                 var imageUrl = it.artist.profileImagePath[0].replace("localhost" , "192.168.43.166")
                 Picasso.get().load(imageUrl).placeholder(R.drawable.backimg).fit().centerInside().into(binding.artistProfileImage)

                 displayLatest(it)
                 displayTopSongs(it.artist.singleSongs)
                 displayTopAlbums(it.artist.albums)
                 displayArtists()
             }
         }

        binding.artistInfoBtn.setOnClickListener {
            artistData?.let { it1 -> (requireActivity() as MainActivity).movetoArtistBio(it1) }
        }

        binding.artistFollowBtn.setOnClickListener {
            artistViewmodel.followArtists(listOf(artistId!!)).handleSingleDataObservation(viewLifecycleOwner){
                 binding.isArtistFavorite = it
                (requireActivity() as MainActivity).showSnacbar("Artist added to favorite")
            }
        }

        binding.artistUnfollowBtn.setOnClickListener{
            artistViewmodel.unfollowArtists(listOf(artistId!!)).handleSingleDataObservation(viewLifecycleOwner){
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
                Picasso.get().load(latestSong.thumbnailPath!!.replace("localhost" , "192.168.43.166"))
                    .placeholder(R.drawable.backimg).fit().centerInside().into(binding.latestContentImageview)
                binding.latestNameTextview.text = latestSong.tittle
                binding.latestDateTextview.text = latestSong.dateCreated!!.toLocaleString()
                binding.latestTypeTextview.text = "Single"

                binding.latestReleaseContainer.setOnClickListener{ prepareAndPlaySongs(listOf(latestSong!!) , 0) }
                return
            }
        }

        latestAlbum?.let {
            if(latestSong == null ||  it.dateCreated!! > latestSong.dateCreated){
                Picasso.get().load(latestAlbum.albumCoverPath!!.replace("localhost" , "192.168.43.166"))
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
       var songStateManager =  RecyclerviewStateManager<Song<String,String>>()
        var listener = object : IRecyclerViewInteractionListener<Song<String, String>> {
            override fun onItemClick(
                data: Song<String, String>,
                position: Int,
                option: Int?
            ) {
                artistData?.let {
                    prepareAndPlaySongs(topSongs , position)
                }
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}

            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }
        var songInfo = RecyclerViewHelper("ARTIST" , songStateManager , listener , viewLifecycleOwner)
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
            mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_MEDIA_TYPE)
        }
        position?.let {
            mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
            mainActivityViewmodel.play(it.toLong())
        }
    }

}

