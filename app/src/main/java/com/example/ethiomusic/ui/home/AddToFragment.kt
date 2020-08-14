package com.example.ethiomusic.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivityViewmodel

import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.AddToFragmentBinding
import com.example.ethiomusic.ui.library.LibraryViewModel
import com.example.ethiomusic.ui.library.LibraryViewmodelFacotry
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.adaper.DownloadStyleListItemAdapter
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import kotlinx.android.synthetic.main.add_to_fragment.*
import javax.inject.Inject

class AddToFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> , View.OnClickListener {

    @Inject
    lateinit var libraryFactory : LibraryViewmodelFacotry
    val libraryViewmodel : LibraryViewModel by viewModels{ GenericSavedStateViewmmodelFactory(libraryFactory , this) }

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : AddToFragmentBinding

    var selectedData  = emptyList<BaseModel>()
    var selectedDataType = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

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
        libraryViewmodel.playlistData.observe(viewLifecycleOwner , Observer { playlists ->
            var info = RecyclerViewHelper(interactionListener = this ,  type = "PLAYLIST")
            var adapter = DownloadStyleListItemAdapter(info)
            add_recyclerview.layoutManager = LinearLayoutManager(view.context)
            add_recyclerview.adapter = adapter
            var pl = playlists.map { playlist -> BaseModel(baseId = playlist._id , baseTittle = playlist.name , baseSubTittle =  "${playlist.songs?.size} songs") }
            adapter.submitList(pl)
        })

        binding.addToFavSongImageview.setOnClickListener(this)
        binding.addToFavAlbumImageview.setOnClickListener(this)
        binding.addToFavArtistImageview.setOnClickListener(this)
        binding.addToQueueImageview.setOnClickListener(this)
    }

    override fun onItemClick(
        selectedPlaylist: BaseModel,
        position: Int,
        option: Int?
    ) {
       handlePlaylistAddition(selectedPlaylist, selectedDataType)
    }

    override fun activiateMultiSelectionMode(){}




    override fun onClick(v: View?) {
        when(v?.id){
            R.id.add_to_fav_song_imageview ->{
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
            R.id.add_to_fav_album_imageview ->{
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

            R.id.add_to_queue_imageview -> {
                if(selectedDataType == LibraryService.CONTENT_TYPE_SONG){
                    var songList = selectedData.map { bInfo ->
                        Song<String,String>(_id = bInfo.baseId!! , tittle = bInfo.baseSubTittle , mpdPath = bInfo.baseDescription , thumbnailPath = bInfo.baseImagePath)
                    }
                    mainActivityViewmodel.addToQueue(songList.toMutableList())
                    findNavController().popBackStack()
                }
            }
        }
    }


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

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }
}
