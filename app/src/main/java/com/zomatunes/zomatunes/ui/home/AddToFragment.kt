package com.zomatunes.zomatunes.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.zomatunes.zomatunes.*

import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.AddToFragmentBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.library.LibraryViewModel
import com.zomatunes.zomatunes.ui.playlist.PlaylistFragment
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.adaper.DownloadStyleListItemAdapter
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.add_to_fragment.*

@AndroidEntryPoint
class AddToFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> , View.OnClickListener {


    val libraryViewmodel : LibraryViewModel by viewModels()


    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : AddToFragmentBinding

    var selectedData  = emptyList<BaseModel>()
    var selectedDataType = -1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            it.getParcelableArrayList<BaseModel>("DATA")?.let {
                selectedData = it.toList()
            }
            it.getInt("DATA_TYPE")?.let {
                selectedDataType = it
            }
        }

        binding = AddToFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        libraryViewmodel.getUserPlaylist()
        inflateView(selectedDataType)

        libraryViewmodel.playlistData.observe(viewLifecycleOwner , Observer { playlists ->
            var info = RecyclerViewHelper(interactionListener = this ,  type = "PLAYLIST")
            var adapter = DownloadStyleListItemAdapter(info)
            add_recyclerview.layoutManager = LinearLayoutManager(view.context)
            add_recyclerview.adapter = adapter
            var pl = playlists.filterNot { pl -> pl.type == "CHART" }.map { playlist -> BaseModel(baseId = playlist._id , baseTittle = playlist.name , baseSubTittle =  "${playlist.songs?.size} songs") }
            adapter.submitList(pl)
        })


    }

    fun inflateView(dataType : Int){
        var playlistItem = create_playlist_item
        var favoriteSongItem = favorite_song_item
        var favoriteAlbumItem = favorite_album_item
        var queueItem = favorite_queue_item

        when(dataType){
            LibraryService.CONTENT_TYPE_SONG -> favoriteAlbumItem.isVisible = false
            LibraryService.CONTENT_TYPE_ALBUM -> {
                favoriteSongItem.isVisible  = false
                queueItem.isVisible = false
            }
        }

        (playlistItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_add_24)
        (playlistItem.findViewById(R.id.library_item_textview) as TextView).text = "Create playlist"
        playlistItem.setOnClickListener(this)

        (favoriteSongItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_round_favorite_border_24)
        (favoriteSongItem.findViewById(R.id.library_item_textview) as TextView).text = "Favorite music"
        favoriteSongItem.setOnClickListener(this)

        (favoriteAlbumItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_outline_album_24)
        (favoriteAlbumItem.findViewById(R.id.library_item_textview) as TextView).text = "Favoreite Album"
        favoriteAlbumItem.setOnClickListener(this)

        (queueItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_outline_queue_music_24)
        (queueItem.findViewById(R.id.library_item_textview) as TextView).text = "Add to queue"
        queueItem.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.favorite_song_item ->{
                if(selectedDataType == LibraryService.CONTENT_TYPE_SONG){
                    var songIds = selectedData.map { baseModel -> baseModel.baseId!! }
                    mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds)
                    Toast.makeText(requireContext(), "songs added to favorite", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                else if(selectedDataType == LibraryService.CONTENT_TYPE_ALBUM){
                    var songIdsList = mutableListOf<String>()
                     selectedData.map { baseModel -> songIdsList.addAll(baseModel.baseListofInfo2!!) }
                    if(!songIdsList.isNullOrEmpty()){
                        mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIdsList)
                        Toast.makeText(v?.context, "songs added to favorite", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }else{
                        Toast.makeText(v?.context, "error occured", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }

            }
            R.id.favorite_album_item ->{
                if(selectedDataType == LibraryService.CONTENT_TYPE_ALBUM){
                      var albumId = selectedData.map { baseModel -> baseModel.baseId!! }
                    mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_ALBUM , albumId)
                    Toast.makeText(v?.context, "Albums added to favorite", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                else if (selectedDataType == LibraryService.CONTENT_TYPE_SONG){
                    var albumId = selectedData.map { baseModel -> baseModel.baseTittle}.first()
                    albumId?.let {
                        mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_ALBUM , listOf(albumId))
                        Toast.makeText(v?.context, "Album added to favorite", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }

                }
                else if(selectedDataType == LibraryService.CONTENT_TYPE_ARTIST){

                }
            }

            R.id.favorite_queue_item -> {
                if(selectedDataType == LibraryService.CONTENT_TYPE_SONG){
                    var songList = selectedData.map { bInfo ->
                        Song<String,String>(_id = bInfo.baseId!! , tittle = bInfo.baseSubTittle , mpdPath = bInfo.baseDescription , thumbnailPath = bInfo.baseImagePath)
                    }
                    mainActivityViewmodel.addToQueue(songList.toMutableList())
                    findNavController().popBackStack()
                }
            }
            R.id.create_playlist_item ->{
                var preference = PreferenceHelper.getInstance(requireContext())
                var creatorName = preference.get(AccountState.USERNAME , "")
                 MaterialDialog(requireContext()).show {
                    title(text = "Create Playist")
                    cornerRadius(14f)
                    input(hint = "Enter playlist name" , waitForPositiveButton = true,  allowEmpty = false){ materialDialog: MaterialDialog, charSequence: CharSequence ->
                        val playlistName = charSequence.toString()
                        if(!playlistName.isBlank()){
                            when(selectedDataType){
                                LibraryService.CONTENT_TYPE_SONG ->{
                                     selectedData.map { baseModel -> baseModel.baseId!! }
                                }
                                LibraryService.CONTENT_TYPE_ALBUM ->{
                                    var songIdsList = mutableListOf<String>()
                                    selectedData.map { baseModel -> songIdsList.addAll(baseModel.baseListofInfo2!!) }
                                    songIdsList
                                }
                                else -> emptyList()
                            }.also {
                                if(it.isNullOrEmpty()){
                                    Toast.makeText(requireContext() , "No song found or selected" , Toast.LENGTH_LONG).show()
                                    findNavController().navigateUp()
                                }
                                var playlist = Playlist(_id = null ,  name = playlistName , songs = it , creatorName = creatorName)
                                libraryViewmodel.createPlaylist(playlist).handleSingleDataObservation(viewLifecycleOwner){
                                    (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , it._id , null , false , true)
                                }
                            }
                        }
                    }
                    positiveButton(text = "Create")
                    negativeButton(text = "Cancel")
                }
            }
        }
    }




    override fun onItemClick(selectedPlaylist: BaseModel, position: Int, option: Int?) {
        handlePlaylistAddition(selectedPlaylist, selectedDataType)
    }

    override fun activiateMultiSelectionMode(){}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


    fun handlePlaylistAddition(selectedPlaylist: BaseModel , contentType : Int){
        when(contentType){
            LibraryService.CONTENT_TYPE_SONG ->{
                var selectedSongIds = selectedData.map { baseModel -> baseModel.baseId!! }
                mainActivityViewmodel.addToPlaylist(selectedPlaylist.baseId!! , selectedSongIds)
            }
            LibraryService.CONTENT_TYPE_ALBUM ->{
                var songIdsList = mutableListOf<String>()
                selectedData.map { baseModel -> songIdsList.addAll(baseModel.baseListofInfo2!!) }
                mainActivityViewmodel.addToPlaylist(selectedPlaylist.baseId!! , songIdsList)
            }
            else -> mainActivityViewmodel.addToPlaylist(null , null)

        }.handleSingleDataObservation(viewLifecycleOwner){
            if(it){
                Toast.makeText(context, "added to ${selectedPlaylist.baseTittle}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }else {
                Toast.makeText(context, "faild to add data", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}
