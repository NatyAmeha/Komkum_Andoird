package com.komkum.komkum.ui.search

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentMusicSearchBinding
import com.komkum.komkum.ui.album.AlbumListViewModel
import com.komkum.komkum.ui.album.adapter.ArtistAdapter
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.ui.playlist.PlaylistViewModel
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.adaper.BrowseCategoryAdapter
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.adaper.MiniViewAdapter
import com.komkum.komkum.util.adaper.PlaylistAdapter
import com.komkum.komkum.util.extensions.showListDialog
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicSearchFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {

    lateinit var binding : FragmentMusicSearchBinding

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()
    val playlistViewmodel : PlaylistViewModel by viewModels()


    var searchResult : Search? = null
    var browseList : List<MusicBrowse> = emptyList()
    var showBrowseItem = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            searchResult = it.getParcelable<Search>("SEARCH_RESULT")
            browseList = it.getParcelableArrayList("BROWSE_LIST") ?: emptyList()
        }

       binding = FragmentMusicSearchBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.msuicGenreView.libraryItemImageView.setImageResource(R.drawable.ic_dashboard_black_24dp)
        binding.msuicGenreView.libraryItemTextview.text = getString(R.string.genre_moods)
        binding.msuicGenreView.root.setOnClickListener {
            browseList?.let{
                var browseInfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner)
                var browseList = it.groupBy { browse -> browse.contentType }

                binding.searchContainer.visibility = View.GONE
                binding.musicBrowseRecyclerview.visibility = View.VISIBLE
                showBrowseItem = true

                browseList[MusicBrowse.CONTENTY_TYPE_MUSIC]?.let {browseList ->
                    var list = browseList.groupBy { browse -> browse.category }
                    var categoryBrowseAdapter = BrowseCategoryAdapter(browseInfo , list)
                    binding.musicBrowseRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.musicBrowseRecyclerview.adapter = categoryBrowseAdapter
                }
            }
        }

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
            var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let {
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    var a = it::class.qualifiedName
                    Log.i("typemediainfo" , "song")
                    when(a){
                        "com.example.com.komkum.komkum.data.model.Song"  -> {
                            it.tittle?.let { it1 -> Log.i("typemedia" , it1) }
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>}
                    }
                }
            }
        })

        searchResult?.let {
            var playlistInteractionLIstener = object : IRecyclerViewInteractionListener<Playlist<String>> {
                override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
                    (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_PLAYLIST , data._id, null , false)
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

            var albumListener = object : IRecyclerViewInteractionListener<BaseModel> {
                override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.baseId!! , false , false)
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}

            }

            var artistListener = object : IRecyclerViewInteractionListener<Artist<String, String>> {
                override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
                    (requireActivity() as MainActivity).moveToArtist(data._id)
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}

                override fun onMoved(prevPosition: Int, newPosition: Int) {}

            }

            var songIteractionListner = object : IRecyclerViewInteractionListener<Song<String , String>> {
                override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
                    searchResult?.songs?.let {


                        when(option){
                            SongAdapter.NO_OPTION ->{
                                mainActivityViewmodel.prepareAndPlaySongsBeta(it, PlayerState.PlaylistState() , position , false , false , 1)
                                mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                            }
//                            SongAdapter.PLAY_OPTION ->{
//                                mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
////                        playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
//                                mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
//                            }

                            SongAdapter.ADD_TO_OPTION ->{
                                (requireActivity() as MainActivity).moveToAddtoFragment(listOf(data._id), LibraryService.CONTENT_TYPE_SONG , mutableListOf(data))
                            }
                            SongAdapter.DOWNLOAD_OPTION ->{
                                if(mainActivityViewmodel.userRepo.userManager.hasValidSubscription()){
                                    playlistViewmodel.downloadPlaylistSongs(listOf(data))
                                    playlistViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
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
                                if(data.album != null)
                                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.album!!, false , false)

                                else Toast.makeText(requireContext() , "Song dosn't have album" , Toast.LENGTH_SHORT).show()
                            }
                            SongAdapter.SONG_INFO_OPTION ->{
                                mainActivityViewmodel.showSongOption(data)
                            }
                            else -> {}
                        }
                    }
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

            var songInfo = RecyclerViewHelper("SONG" , mainActivityViewmodel.songrecyclerviewStateManeger , songIteractionListner  , viewLifecycleOwner)
            var playlistInfo = RecyclerViewHelper("BROWSE_PLAYLIST" , interactionListener = playlistInteractionLIstener ,
                owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
            binding.playlistInfo = playlistInfo
            binding.songInfo = songInfo
            binding.searchResult = it



            var albumAdapter = MiniViewAdapter(type = "ALBUM" , interaction = albumListener  )
            binding.albumSearchRecyclerview.layoutManager = GridLayoutManager(view.context , 3)
            binding.albumSearchRecyclerview.adapter = albumAdapter
            it.albums?.let {
                var albumList = if(it.size > 6) it.subList(0,6); else it
                var baseInfo = albumList.map { album -> album.toBaseModel() }
                albumAdapter.submitList(baseInfo)
            }


            var artistInfo = RecyclerViewHelper(interactionListener = artistListener , owner = viewLifecycleOwner)
            var artistAdapter = ArtistAdapter(artistInfo)
            binding.artistSearchRecyclerview.layoutManager = GridLayoutManager(requireContext() , 3)
            binding.artistSearchRecyclerview.adapter = artistAdapter
            it.artists?.let {
                var artistList = if(it.size > 6) it.subList(0,6); else it
                artistAdapter.submitList(artistList)
            }
        }

        binding.viewAllSongBtn.setOnClickListener {
            searchResult?.songs?.let {
                (requireActivity() as MainActivity).movetoSongListFragment(null , it , "Popular songs")}
        }

        binding.viewAllAlbumBtn.setOnClickListener {
            searchResult?.albums?.let {
                (requireActivity() as MainActivity).moveToAlbumListFragment("${getString(R.string.albums)} (${it.size})" , AlbumListViewModel.OTHER , albumList = it)
            }
        }

        binding.viewAllArtistBtn.setOnClickListener {
            searchResult?.artists?.let {
                (requireActivity() as MainActivity).moveToArtistList("Artists (${it.size})" , Artist.OTHER , artistList = it)
            }
        }

        binding.viewAllPlaylistsBtn.setOnClickListener {
            searchResult?.playlists?.let {
                var bundle = bundleOf("DATA_TYPE" to Playlist.FROM_PERVIOUS_FRAGMENT , "PLAYLISTS" to it)
                findNavController().navigate(R.id.playlistListFragment , bundle)
            }
        }

//        (requireActivity() as MainActivity).onBackPressedDispatcher.addCallback(viewLifecycleOwner){
//            if(showBrowseItem){
//                binding.musicBrowseRecyclerview.visibility = View.GONE
//                binding.searchContainer.visibility = View.VISIBLE
//            }
//            else findNavController().navigateUp()
//
//        }
    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var bundle = bundleOf("BROWSE" to data)
        when(data.contentType){
            MusicBrowse.CONTENTY_TYPE_MUSIC ->  findNavController().navigate(R.id.browseMusicFragment , bundle)
            MusicBrowse.CONTENT_TYPE_BOOK -> findNavController().navigate(R.id.bookBrowseFragment , bundle)
        }
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}