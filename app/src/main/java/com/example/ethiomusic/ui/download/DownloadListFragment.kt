package com.example.ethiomusic.ui.download

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.ethiomusic.*

import com.example.ethiomusic.data.model.DbDownloadInfo
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.DownloadListFragmentBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.ui.playlist.PlaylistFragment
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.adaper.DownloadAdapter
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.toSong
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.PlayerState
import javax.inject.Inject

class DownloadListFragment : Fragment() , IRecyclerViewInteractionListener<DbDownloadInfo> {
    @Inject
    lateinit var downloadFacotory : DownloadViewmodelFactory
    val viewModel: DownloadViewmodel by viewModels { GenericSavedStateViewmmodelFactory(downloadFacotory , this) }

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : DownloadListFragmentBinding
    lateinit var  downloadAdapter : DownloadAdapter
    var downloadList = emptyList<DbDownloadInfo>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      binding = DownloadListFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.downloadToolbar , "Downloads")
        val info = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner)

        downloadAdapter = DownloadAdapter(info , viewModel.downloadTracker , viewModel.appDb)
        binding.downloadRecyclerview.layoutManager = LinearLayoutManager(view.context)
        binding.downloadRecyclerview.adapter = downloadAdapter

        viewModel.getDownloads().observe(viewLifecycleOwner , Observer { downloads ->
            downloads?.let {
                downloadList = downloads
                binding.dowloadLoadingProgressbar.visibility = View.GONE
                binding.textView37.isVisible = it.isEmpty()
                downloadAdapter.submitList(downloads)
            }
        })

        binding.downloadFilterImageview.setOnClickListener {
            initDownloadFilterMenu()
        }

    }

    fun initDownloadFilterMenu(){
        var prefHelper = PreferenceHelper.getInstance(requireContext())
        var popupMenu = androidx.appcompat.widget.PopupMenu(requireContext() , binding.downloadFilterImageview)
        popupMenu.apply {
            inflate(R.menu.download_filter_menu)
            gravity = Gravity.START
            var selectedMenuItemId = prefHelper.getInt("DOWNLOAD_FILTER_KEY_ID" , R.id.show_all_download_menu_item)
            menu.findItem(selectedMenuItemId)?.let { it.isChecked = true  }
            setOnMenuItemClickListener {
                prefHelper["DOWNLOAD_FILTER_KEY_ID"] = it.itemId
                when(it.itemId){
                    R.id.show_all_download_menu_item ->{
                        filterDownloadResult(-1)
                        true
                    }
                    R.id.show_song_download_menu_item ->{
                        filterDownloadResult(DownloadViewmodel.DOWNLOAD_TYPE_SONG)
                        true
                    }
                    R.id.show_album_download_menu_item ->{
                        filterDownloadResult(DownloadViewmodel.DOWNLOAD_TYPE_ALBUM)
                        true
                    }
                    R.id.show_playlist_download_menu_item ->{
                        filterDownloadResult(DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST)
                        true
                    }
                    else -> {true}
                }
            }
            show()
        }

    }

    fun filterDownloadResult(type : Int){
        downloadList?.let {
            if(type == -1) downloadAdapter.submitList(downloadList)
           else{
                var filterdList = it.filter { download -> download.type == type}
                downloadAdapter.submitList(filterdList)
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.download_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.resume_all_download_menu_item ->{
                viewModel.resumeAllDownloads(downloadAdapter.currentList)
                downloadAdapter.submitList(viewModel.getDownloads().value)
                true
            }
            R.id.delete_alldownload_menu_item ->{
                MaterialDialog(requireActivity()).show {
                    title(text = "Delete")
                    message(text = "Are you Sure to delete all Downloads from your device?")
                    cornerRadius(literalDp = 14f)
                    positiveButton(text = "Delete"){
                        viewModel.deleteAllDownloads(downloadAdapter.currentList)
                        downloadAdapter.submitList(emptyList())
                    }
                    negativeButton(text = "Cancel") {
                        it.dismiss()
                    }
                }
                true
            }
            else -> { super.onOptionsItemSelected(item) }
        }
    }



    override fun onItemClick(data: DbDownloadInfo, action: Int, option: Int?) {
        if(action == DownloadAdapter.DOWNLOAD_ACTION_DELETE){
            when(data.type){
                DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> { viewModel.removeDownloadedAlbum(data.contentId) }
                DownloadViewmodel.DOWNLOAD_TYPE_SONG ->{viewModel.removeDownloadedSong(data.contentId)}
                DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST -> viewModel.removeDownloadedPlaylist(data.contentId)
            }
        }
        else if(action == DownloadAdapter.DOWNLOAD_ACTION_PAUSE){
            when(data.type){
                DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> {
                    viewModel.pauseAlbumDownload(data.contentId , data)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_SONG ->{
                    viewModel.pauseSongDownload(data.contentId , data)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
            }
        }
        else if(action == DownloadAdapter.DOWNLOAD_ACTION_RESUME){
            when(data.type){
                DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> {
                    viewModel.resumeAlbumDownload(data.contentId , data)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_SONG ->{
                    viewModel.resumeSongDownload(data.contentId , data)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
            }
        }
        else{
            when(data.type){
                DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> {
                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.contentId , true , true)
                }
                DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST ->{
                    (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , data.contentId , null , true)
                }
                DownloadViewmodel.DOWNLOAD_TYPE_SONG ->{
                    viewModel.getSong(data.contentId).observe(viewLifecycleOwner , Observer{
                        prepareAndPlaySongs(it.toSong() , action)
                    })
                }
            }
        }
    }

    override fun activiateMultiSelectionMode() {

    }


    fun prepareAndPlaySongs(songs : List<Song<String,String>>, position: Int){
        viewModel.downloadTracker.getCompletedDownloads().observe(viewLifecycleOwner , Observer{
            if(!it.isNullOrEmpty()){
                var allDownloadsUri = it.map { download -> download.request.uri }
                var completedSongs = songs.filter { song -> allDownloadsUri.contains(Uri.parse(song.songFilePath!!.replace("localhost", "192.168.43.166"))) }

                if(!completedSongs.isNullOrEmpty()){
                    mainActivityViewmodel.playlistQueue.value = completedSongs
                    mainActivityViewmodel.preparePlayer(ServiceConnector.DOWNLOADED_MEDIA_TYPE)
                    mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
                    mainActivityViewmodel.play()
                }
                else Toast.makeText(requireContext() , "Song not downloaded yet" , Toast.LENGTH_SHORT).show()
            }
            else  Toast.makeText(requireContext() , "Song not downloaded yet" , Toast.LENGTH_SHORT).show()
        })

    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }
}
