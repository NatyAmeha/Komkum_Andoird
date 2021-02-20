package com.zomatunes.zomatunes.ui.book

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.MainActivityViewmodel
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Author
import com.zomatunes.zomatunes.data.model.Audiobook
import com.zomatunes.zomatunes.data.model.Chapter
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.BookFragmentBinding
import com.zomatunes.zomatunes.ui.download.DownloadViewmodel
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.ChapterAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import com.google.android.exoplayer2.offline.Download
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        bookId?.let {
            bookViewmodel.audiobookResult.observe(viewLifecycleOwner , Observer{
                audiobookResult = it
                binding.bookLoadingProgressbar.visibility = View.GONE
                binding.bookContainer.visibility = View.VISIBLE
                configureActionBar(binding.toolbar , it.name)
                displayChapter(it)

                var chapterCurrentlydownloaded = bookViewmodel.downloadTracker.getCurrentlyDownloadedItems(audiobookResult!!.chapters!!.map { chapter -> chapter._id })
                binding.continueListeningBtn.isEnabled = chapterCurrentlydownloaded.isEmpty()

                binding.bookTitleTextview.text = it.name
                binding.bookAuthorTextview.text = it.authorName?.joinToString(", ")
                binding.narratorNameTextview.text = "Narrated by ${it.narrator}"
                Picasso.get().load(it.coverImagePath?.replace("localhost" , AdapterDiffUtil.URL)).fit().centerCrop().into(binding.bookCoverImageview)

                binding.downloadAmountTextview.text = "Audiobook"
                var ratingValue = it.rating?.map { rating -> rating.rate }
                if(!ratingValue.isNullOrEmpty()){
                    var totalValue =  ratingValue.reduce { acc, rating -> acc.plus(rating) }
                    var averageRatingValue = totalValue.toFloat().div(it.rating!!.size)
                    binding.ratingTextview.text = "$averageRatingValue Rating"
                }
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


    override fun onItemClick(data: Chapter, position: Int, option: Int?) {
        CoroutineScope(Dispatchers.Main).launch{
            var chapterUri = Uri.parse(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))
            // check is chapter currently downloaded or paused before playing
            bookViewmodel.downloadTracker.getCurrentDownloadItem(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))?.let {
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
                downloadViewmodel.getDownloads(Download.STATE_COMPLETED).observe(viewLifecycleOwner){
                    var ids = it.map { download -> download.request.id }
                    if(!ids.isNullOrEmpty()){
                        audiobookResult.chapters?.filter { chapter -> ids.contains(chapter._id) }?.let { completedChapters ->
                            mainActivityViewmoel.prepareAndPlayAudioBook(completedChapters, false , PlayerState.AudiobookState() ,
                                position , true , audiobookResult._id , completedChapters[audiobookResult.lastListeningChapterIndex].currentPosition)
                        }
                    }else Toast.makeText(requireContext(), "No Chapter downloaded" , Toast.LENGTH_LONG).show()
                }
                chapterStateManager.selectedItem.value = data
            }
        }
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    fun displayChapter(audiobook : Audiobook<Chapter , Author<String , String>>){
        var info = RecyclerViewHelper<Chapter>("CHAPTER" , chapterStateManager , this ,  viewLifecycleOwner , downloadTracker = bookViewmodel.downloadTracker , playbackState = mainActivityViewmoel.playbackState)
        var chaptersInfo = audiobook.chapters!!.map {
            it.authorNames = audiobook.authorName?.joinToString(", ")
            it.genre = it.authorNames
            it
        }
        var chapterAdapter = ChapterAdapter(info , chaptersInfo , audiobook.coverImagePath!!.replace("localhost" , AdapterDiffUtil.URL))
        binding.chapterRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.chapterRecyclerview.adapter = chapterAdapter
    }

}