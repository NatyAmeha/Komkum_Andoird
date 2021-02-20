package com.zomatunes.zomatunes.ui.search

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.MainActivityViewmodel
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.FragmentMusicSearchBinding
import com.zomatunes.zomatunes.ui.album.AlbumListViewModel
import com.zomatunes.zomatunes.ui.album.adapter.ArtistAdapter
import com.zomatunes.zomatunes.ui.playlist.PlaylistFragment
import com.zomatunes.zomatunes.util.adaper.BrowseCategoryAdapter
import com.zomatunes.zomatunes.util.adaper.MiniViewAdapter
import com.zomatunes.zomatunes.util.adaper.PlaylistAdapter
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicSearchFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {

    lateinit var binding : FragmentMusicSearchBinding

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()


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
        (binding.msuicGenreView.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_album_24)
        (binding.msuicGenreView.findViewById(R.id.library_item_textview) as TextView).text = "Genre and Moods"
        binding.msuicGenreView.setOnClickListener {
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
                        "com.example.com.zomatunes.zomatunes.data.model.Song"  -> {
                            Log.i("typemedia" , it.tittle)
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
                        mainActivityViewmodel.prepareAndPlaySongsBeta(it, PlayerState.PlaylistState() , position , false , false , 1)
                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                    }
                }
                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

            var songInfo = RecyclerViewHelper("SONG" , mainActivityViewmodel.songrecyclerviewStateManeger , songIteractionListner  , viewLifecycleOwner)
            var playlistInfo = RecyclerViewHelper("PLAYLIST" , interactionListener = playlistInteractionLIstener ,
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
                (requireActivity() as MainActivity).movetoSongListFragment(null , it)}
        }

        binding.viewAllAlbumBtn.setOnClickListener {
            searchResult?.albums?.let {
                (requireActivity() as MainActivity).moveToAlbumListFragment("Albums (${it.size})" , AlbumListViewModel.OTHER , albumList = it)
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