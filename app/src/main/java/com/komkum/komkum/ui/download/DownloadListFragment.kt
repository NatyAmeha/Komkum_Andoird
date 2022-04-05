package com.komkum.komkum.ui.download

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.komkum.komkum.*

import com.komkum.komkum.data.model.DbDownloadInfo
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.DownloadListFragmentBinding
import com.komkum.komkum.media.ServiceConnector
import com.komkum.komkum.ui.book.BookReaderActivity
import com.komkum.komkum.ui.book.BookViewModel
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.toSong
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.google.android.material.tabs.TabLayoutMediator
import com.novoda.downloadmanager.DownloadBatchIdCreator
import com.novoda.downloadmanager.DownloadBatchStatus
import com.novoda.downloadmanager.DownloadFileIdCreator
import com.novoda.downloadmanager.StorageRootFactory
import com.komkum.komkum.R
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.extensions.toEpisode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DownloadListFragment : Fragment() , IRecyclerViewInteractionListener<DbDownloadInfo> , TimerTracker {

    val viewModel: DownloadViewmodel by viewModels()
    val bookViewmodel : BookViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var mDownloadList : LiveData<List<DbDownloadInfo>>
    var timers = mutableListOf<Timer>()

    lateinit var binding : DownloadListFragmentBinding
//    lateinit var  downloadAdapter : DownloadAdapter
    lateinit var downloadViewpagerAdapter : DownloadViewpagerAdapter
    var downloadList = emptyList<DbDownloadInfo>()

    var activeViewPagerPage = 0
    var emptyDownload = false

    init {
        lifecycleScope.launchWhenCreated {
            mDownloadList = viewModel.downloadRepo.getDownloads()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
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

        mDownloadList.observe(viewLifecycleOwner , Observer { downloads ->
                downloads?.let {
                    downloadList = it
                    emptyDownload = it.isEmpty()
                    requireActivity().invalidateOptionsMenu()
                    binding.dowloadLoadingProgressbar.visibility = View.GONE

                    var downloadsMap = it.sortedByDescending { download -> download._id }.groupBy { download -> download.category }
                    downloadViewpagerAdapter = DownloadViewpagerAdapter(info , downloadsMap , viewModel.appDb , this)
                    binding.downloadViewpager.adapter = downloadViewpagerAdapter
                    TabLayoutMediator(binding.downloadTabLayout , binding.downloadViewpager){tab, position ->
                        when(position){
                            0 -> tab.text = getString(R.string.music)
                            1 -> tab.text = getString(R.string.audiobooks)
                            2 -> tab.text = getString(R.string.books)
                            3 -> tab.text = getString(R.string.podcast_episodes)
                        }
                    }.attach()
                    binding.downloadViewpager.currentItem = activeViewPagerPage
                }
        })

        binding.downloadViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { activeViewPagerPage = position }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            Log.i("downloadtrac" , timers.size.toString())
            timers.forEach { timer -> timer.cancel() }
            binding.downloadViewpager.adapter = null
            info.interactionListener = null
            findNavController().navigateUp()
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        if(emptyDownload){
            menu.removeItem(R.id.resume_all_download_menu_item)
            menu.removeItem(R.id.delete_alldownload_menu_item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.download_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

            R.id.resume_all_download_menu_item ->{
                viewModel.resumeAllDownloads(downloadList)
                viewModel.downloadRepo.getDownloads()
                true
            }
            R.id.delete_alldownload_menu_item ->{
                MaterialDialog(requireActivity()).show {
                    title(text = getString(R.string.delete))
                    message(text = getString(R.string.delete_download_warning_message))
                    cornerRadius(literalDp = 14f)
                    positiveButton(text = getString(R.string.delete)){ viewModel.deleteAllDownloads(downloadList) }
                    negativeButton(text = getString(R.string.cancel)) { it.dismiss() }
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
                    viewModel.removeDownloadedAudioBook(data.contentId)
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
                DownloadViewmodel.DOWNLOAD_TYPE_EPISODE ->{
                    viewModel.pausePodcastEpisodeDownload(data.contentId)
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

                DownloadViewmodel.DOWNLOAD_TYPE_EPISODE ->{
                    viewModel.resumePodcastEpisodeDownload(data.contentId)
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

                DownloadViewmodel.DOWNLOAD_TYPE_EPISODE ->{
                    viewModel.downloadTracker.getCurrentDownloadItem(data.contentId)?.let {
                        Toast.makeText(context , getString(R.string.download_not_completed) , Toast.LENGTH_SHORT).show()
                        return
                    }
                    viewModel.downloadTracker.getFailedDownloads().value?.let { downloads ->
                        var isFailed = downloads.map { download -> download.request.id }.contains(data.contentId)
                        if(isFailed){
                            Toast.makeText(context , getString(R.string.download_failed) , Toast.LENGTH_SHORT).show()
                            return
                        }
                    }

                    viewModel.getEpisodeFromDatabase(data.contentId).observe(viewLifecycleOwner){
                        it?.let {
                            var episode = it.toEpisode()
                            mainActivityViewmodel.episodeStateManager.selectedItem.value = episode
                            (requireActivity() as MainActivity).moveToEpisode(episode , true)
                        }
                    }
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
                            Toast.makeText(requireContext() , getString(R.string.download_not_completed) , Toast.LENGTH_LONG).show()
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
                var completedSongs = songs.filter { song -> allDownloadsUri.contains(Uri.parse(song.songFilePath!!)) }

                if(!completedSongs.isNullOrEmpty()){
                    mainActivityViewmodel.playlistQueue.value = completedSongs.toMutableList()
                    mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_DOWNLOAD)
                    mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
                    mainActivityViewmodel.play()
                }
                else Toast.makeText(requireContext() , getString(R.string.download_not_completed) , Toast.LENGTH_SHORT).show()
            }
            else  Toast.makeText(requireContext() , getString(R.string.download_not_completed) , Toast.LENGTH_SHORT).show()
        })

    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }

    override fun onAdd(timer: Timer) {
        timers.add(timer)
    }
}
