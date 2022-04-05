package com.komkum.komkum.ui.book

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.*
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.databinding.BookFragmentBinding
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.ChapterAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.*
import com.komkum.komkum.util.extensions.showShareMenu
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AudiobookFragment : Fragment() , IRecyclerViewInteractionListener<Chapter> {
    lateinit var binding : BookFragmentBinding
    val bookViewmodel: BookViewModel by viewModels()
    val downloadViewmodel: DownloadViewmodel by viewModels()

    val mainActivityViewmoel : MainActivityViewmodel by activityViewModels()

    val chapterStateManager : RecyclerviewStateManager<Chapter> by lazy {
        var stateManager = RecyclerviewStateManager<Chapter>()
        stateManager._downloadingItems.value = bookViewmodel.downloadTracker.getAllCurrentlyDownloadedItems()
        stateManager
    }

    var bookId : String? = null
    var audiobookResult : Audiobook<Chapter , Author<String , String>>? = null

    var selectedMediaId : String? = null

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                bookId = it.getString("BOOK_ID")
            }
            chapterStateManager.changeState(RecyclerviewState.DownloadState())
            bookId?.let { bookViewmodel.getAudiobook(it, true) }
        }
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BookFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.offlineBookContainer.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if(scrollY < 100){
                binding.toolbar.title = ""
            }
            else if(scrollY > 500){
                binding.toolbar.title = binding.bookTitleTextview.text
            }
        }

        bookId?.let {
            bookViewmodel.audiobookResult.observe(viewLifecycleOwner , Observer{
                audiobookResult = it
                binding.bookLoadingProgressbar.visibility = View.GONE
                binding.bookContainer.visibility = View.VISIBLE
                configureActionBar(binding.toolbar)


                var chapterCurrentlydownloaded = bookViewmodel.downloadTracker.getCurrentlyDownloadedItems(audiobookResult!!.chapters!!.map { chapter -> chapter._id })
                binding.continueListeningBtn.isEnabled = chapterCurrentlydownloaded.isEmpty()

                binding.bookTitleTextview.text = it.name
                binding.durationTextview.text = it.length
                binding.bookAuthorTextview.text = it.authorName?.joinToString(", ")
                binding.narratorNameTextview.text = "Narrated by ${it.narrator}"
                Picasso.get().load(it.coverImagePath).placeholder(R.drawable.audiobook_placeholder)
                    .fit().centerCrop().into(binding.bookCoverImageview)

                binding.downloadAmountTextview.text = "Audiobook"
                binding.ratingTextview.text = "${it.chapters?.size ?: 0} chapters"

                mainActivityViewmoel.nowPlaying.observe(viewLifecycleOwner , Observer{metadata ->
                    var chapterId : String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                    chapterId?.let { it ->
                        mainActivityViewmoel.getSelectedSongInfo(it)?.let {
                            var a = it::class.qualifiedName
                            when(a){
                                "com.komkum.komkum.data.model.Chapter" ->{
                                    chapterStateManager.selectedItem.value = it as Chapter
                                    mainActivityViewmoel.songrecyclerviewStateManeger.selectedItem.value = null
                                }
                            }
                        }
                    }
                })

                displayChapter(it)

                val cal = Calendar.getInstance()
                cal.time = it.releaseDate
                binding.listeningPercentTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}  ${cal.get(Calendar.YEAR)}"
            })

            binding.continueListeningBtn.setOnClickListener {
                audiobookResult?.let {
                    mainActivityViewmoel.prepareAndPlayAudioBook(it.chapters!! , false , PlayerState.AudiobookState() , it.lastListeningChapterIndex , true ,
                        it._id , it.chapters!![it.lastListeningChapterIndex].currentPosition)
                    chapterStateManager.selectedItem.value = it.chapters!![it.lastListeningChapterIndex]
                    Log.i("bookposition" , it.chapters!![it.lastListeningChapterIndex].currentPosition.toString())
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.removeItem(R.id.available_credit_menu_item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.book_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigate(R.id.downloadListFragment)
                true
            }
            R.id.share_menu_item ->{
                audiobookResult?.let {
                    requireActivity().showShareMenu("Share ${it.name}" ,"Check out ${it.name}on ZomaTunes https://play.google.com/store/apps/details?id=com.komkum.komkum" )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        binding.chapterRecyclerview.adapter = null
        super.onDestroyView()
    }


    override fun onItemClick(data: Chapter, position: Int, option: Int?) {
        CoroutineScope(Dispatchers.Main).launch{
            var chapterUri = Uri.parse(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))
            // check is chapter currently downloaded or paused before playing
            var uri =
            bookViewmodel.downloadTracker.getCurrentDownloadItem(data._id)?.let {
                Toast.makeText(context , "Not downloaded yet" , Toast.LENGTH_SHORT).show()
                return@launch
            }

            // check is chapter failed to be downloaded before playing
            var failedDownloads =  bookViewmodel.downloadTracker.getFailedDownloadBeta()

            var isFailed = failedDownloads.map { download -> download.request.uri }.contains(chapterUri)
            if(isFailed){
                Toast.makeText(context , "Download Failed " , Toast.LENGTH_SHORT).show()
                return@launch
            }



            audiobookResult?.let {audiobookResult ->
                mainActivityViewmoel.prepareAndPlayAudioBook(audiobookResult.chapters?.sortedBy { chapter -> chapter.chapterNumber }!!, false , PlayerState.AudiobookState() ,
                    position , true , audiobookResult._id , audiobookResult.chapters!![position].currentPosition)
                chapterStateManager.selectedItem.value = data
                mainActivityViewmoel.playedFrom.value = "${audiobookResult.name}\n Audiobook"

//                downloadViewmodel.getDownloads(Download.STATE_COMPLETED).observe(viewLifecycleOwner){
//                    var ids = it.map { download -> download.request.id }
//                    if(!ids.isNullOrEmpty()){
//                        audiobookResult.chapters?.filter { chapter -> ids.contains(chapter._id) }?.let { completedChapters ->
//                            }
//                    }else Toast.makeText(requireContext(), "No Chapter downloaded" , Toast.LENGTH_LONG).show()
//                }
                chapterStateManager.selectedItem.value = data
            }
        }
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    fun displayChapter(audiobook : Audiobook<Chapter , Author<String , String>>){
        var info = RecyclerViewHelper("CHAPTER" , chapterStateManager , this ,  viewLifecycleOwner , downloadTracker = bookViewmodel.downloadTracker , playbackState = mainActivityViewmoel.playbackState)
        var chaptersInfo = audiobook.chapters!!.map {
            it.authorNames = audiobook.authorName?.joinToString(", ")
            it.genre = it.authorNames
            it
        }.sortedBy { chapter -> chapter.chapterNumber }
        var chapterAdapter = ChapterAdapter(info , chaptersInfo , audiobook.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL))
        binding.chapterRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.chapterRecyclerview.adapter = chapterAdapter
    }

}