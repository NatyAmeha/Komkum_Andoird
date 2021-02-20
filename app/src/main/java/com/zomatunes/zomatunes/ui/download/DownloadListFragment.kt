package com.zomatunes.zomatunes.ui.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.zomatunes.zomatunes.*

import com.zomatunes.zomatunes.data.model.DbDownloadInfo
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.DownloadListFragmentBinding
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.ui.book.BookReaderActivity
import com.zomatunes.zomatunes.ui.book.BookViewModel
import com.zomatunes.zomatunes.ui.playlist.PlaylistFragment
import com.zomatunes.zomatunes.util.adaper.DownloadAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.toSong
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.google.android.material.tabs.TabLayoutMediator
import com.novoda.downloadmanager.DownloadBatchIdCreator
import com.novoda.downloadmanager.DownloadBatchStatus
import com.novoda.downloadmanager.DownloadFileIdCreator
import com.novoda.downloadmanager.StorageRootFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadListFragment : Fragment() , IRecyclerViewInteractionListener<DbDownloadInfo> {

    val viewModel: DownloadViewmodel by viewModels()
    val bookViewmodel : BookViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : DownloadListFragmentBinding
//    lateinit var  downloadAdapter : DownloadAdapter
    lateinit var downloadViewpagerAdapter : DownloadViewpagerAdapter
    var downloadList = emptyList<DbDownloadInfo>()

    var activeViewPagerPage = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            activeViewPagerPage = it.getInt("SELECTED_PAGE")
        }
        binding = DownloadListFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.downloadToolbar , "Downloads")
        val info = RecyclerViewHelper(interactionListener = this , downloadTracker = viewModel.downloadTracker , owner = viewLifecycleOwner)

        viewModel.downloadRepo.getDownloads().observe(viewLifecycleOwner , Observer { downloads ->
                downloads?.let {
                    downloadList = it
                    binding.dowloadLoadingProgressbar.visibility = View.GONE

                    var downloadsMap = it.sortedByDescending { download -> download._id }.groupBy { download -> download.category }
                    downloadViewpagerAdapter = DownloadViewpagerAdapter(info , downloadsMap , viewModel.appDb)
                    binding.downloadViewpager.adapter = downloadViewpagerAdapter
                    TabLayoutMediator(binding.downloadTabLayout , binding.downloadViewpager){tab, position ->
                        when(position){
                            0 -> tab.text = "Music"
                            1 -> tab.text = "AudioBook"
                            2 -> tab.text = "Book"
                        }
                    }.attach()
                    binding.downloadViewpager.currentItem = activeViewPagerPage
                }
        })

        binding.downloadViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { activeViewPagerPage = position }
        })

    }

//    fun initDownloadFilterMenu(){
//        var prefHelper = PreferenceHelper.getInstance(requireContext())
//        var popupMenu = androidx.appcompat.widget.PopupMenu(requireContext() , binding.downloadFilterImageview)
//        popupMenu.apply {
//            inflate(R.menu.download_filter_menu)
//            gravity = Gravity.START
//            var selectedMenuItemId = prefHelper.getInt("DOWNLOAD_FILTER_KEY_ID" , R.id.show_all_download_menu_item)
//            menu.findItem(selectedMenuItemId)?.let { it.isChecked = true  }
//            setOnMenuItemClickListener {
//                prefHelper["DOWNLOAD_FILTER_KEY_ID"] = it.itemId
//                when(it.itemId){
//                    R.id.show_all_download_menu_item ->{
//                        filterDownloadResult(-1)
//                        true
//                    }
//                    R.id.show_song_download_menu_item ->{
//                        filterDownloadResult(DownloadViewmodel.DOWNLOAD_TYPE_SONG)
//                        true
//                    }
//                    R.id.show_album_download_menu_item ->{
//                        filterDownloadResult(DownloadViewmodel.DOWNLOAD_TYPE_ALBUM)
//                        true
//                    }
//                    R.id.show_playlist_download_menu_item ->{
//                        filterDownloadResult(DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST)
//                        true
//                    }
//                    else -> {true}
//                }
//            }
//            show()
//        }
//
//    }

    fun filterDownloadResult(type : Int){
//        downloadList?.let {
//            if(type == -1) downloadAdapter.submitList(downloadList)
//           else{
//                var filterdList = it.filter { download -> download.type == type}
//                downloadAdapter.submitList(filterdList)
//            }
//        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.download_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.resume_all_download_menu_item ->{
                viewModel.resumeAllDownloads(downloadList)
                viewModel.downloadRepo.getDownloads()
                true
            }
            R.id.delete_alldownload_menu_item ->{
                MaterialDialog(requireActivity()).show {
                    title(text = "Delete")
                    message(text = "Are you Sure to delete all Downloads from your device?")
                    cornerRadius(literalDp = 14f)
                    positiveButton(text = "Delete"){ viewModel.deleteAllDownloads(downloadList) }
                    negativeButton(text = "Cancel") { it.dismiss() }
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
                DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST ->{ viewModel.removeDownloadedPlaylist(data.contentId) }
                DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK ->{
                    MaterialDialog(requireContext()).show {
                        title(text = "Delete Audiobook")
                        message(text = "Are you sure to delete this audiobook? You need to buy again to access this audiobook")
                        positiveButton(text = "Yes") {materialDialog -> viewModel.removeDownloadedAudioBook(data.contentId) }
                        negativeButton(text = "Cancel"){materialDialog -> materialDialog.dismiss() }
                    }
                }
                DownloadViewmodel.DOWNLOAD_TYPE_EBOOK ->{
                    viewModel.removeDownloadedEbook(data.contentId)
                }
            }
        }
        else if(action == DownloadAdapter.DOWNLOAD_ACTION_PAUSE){
            when(data.type){
                DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> {
                    viewModel.pauseAlbumDownload(data.contentId)
                    viewModel.downloadTracker.getDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST ->{
                    viewModel.pausePlaylistDownload(data.contentId)
                    viewModel.downloadTracker.getDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK ->{
                    viewModel.pauseAudioBookDownload(data.contentId)
                    viewModel.downloadTracker.getDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_EBOOK ->{
                    viewModel.pauseEbookDownload(data.contentId)
                    viewModel.downloadTracker.getAllNovodaDownload()

                }
            }
        }
        else if(action == DownloadAdapter.DOWNLOAD_ACTION_RESUME){
            when(data.type){
                DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> {
                    viewModel.resumeAlbumDownload(data.contentId)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST ->{
                    viewModel.resumePlaylistDownload(data.contentId)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK ->{
                    viewModel.resumeAudiobookDownload(data.contentId)
                    viewModel.downloadTracker.getOngoingDownloads()
                }
                DownloadViewmodel.DOWNLOAD_TYPE_EBOOK ->{
                    viewModel.resumeEbookDownload(data.contentId)
                    viewModel.downloadTracker.getAllNovodaDownload().observe(viewLifecycleOwner){}
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
                DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK ->{
                    var bundle = bundleOf("BOOK_ID" to data.contentId )
                    findNavController().navigate(R.id.audiobookFragment , bundle)
                }
                DownloadViewmodel.DOWNLOAD_TYPE_EBOOK ->{
                    viewModel.downloadTracker.getNovodaDownload(data.contentId).observe(viewLifecycleOwner , Observer{
                        if(it.status() == DownloadBatchStatus.Status.DOWNLOADED){
                            viewModel.downloadTracker.getNovodaDownloadedFile(data.contentId).observe(viewLifecycleOwner , Observer{file ->
                                viewModel.getDownloadedEbook(data.contentId).observe(viewLifecycleOwner){
                                    it?.let {
                                        var bundle = bundleOf("BOOK_URL" to file?.localFilePath()?.path() , "BOOK_ID" to data.contentId ,
                                            "CURRENT_POSITION" to it.lastReadingPage, "LOAD_FROM_CACHE" to true)
                                        var intent = Intent(requireContext() , BookReaderActivity::class.java)
                                        intent.putExtras(bundle)
                                        requireContext().startActivity(intent)
                                    }
//                                    Toast.makeText(requireContext() , "book not saved to db properly" , Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        else {
                            Toast.makeText(requireContext() , "Not downloaded yet" , Toast.LENGTH_LONG).show()
                        }
                    })

                }
            }
        }
    }



    override fun activiateMultiSelectionMode() {}


    fun prepareAndPlaySongs(songs : List<Song<String,String>>, position: Int){
        viewModel.downloadTracker.getCompletedDownloads().observe(viewLifecycleOwner , Observer{
            if(!it.isNullOrEmpty()){
                var allDownloadsUri = it.map { download -> download.request.uri }
                var completedSongs = songs.filter { song -> allDownloadsUri.contains(Uri.parse(song.songFilePath!!.replace("localhost", "192.168.43.166"))) }

                if(!completedSongs.isNullOrEmpty()){
                    mainActivityViewmodel.playlistQueue.value = completedSongs
                    mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_DOWNLOAD)
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
