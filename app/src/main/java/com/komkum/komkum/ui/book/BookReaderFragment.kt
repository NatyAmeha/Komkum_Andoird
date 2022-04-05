package com.komkum.komkum.ui.book

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.R
import com.komkum.komkum.util.extensions.toControllerActivity
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.komkum.komkum.MainActivity
import com.komkum.komkum.databinding.FragmentBookReaderBinding

import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.net.URL

class BookReaderFragment : Fragment() {

    lateinit var binding : FragmentBookReaderBinding
    var bookUrl : String? = null
    var loadFromCache = false
    var handler = Handler()
    var isPdfOverlayVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            bookUrl = it.getString("BOOK_URL")
            loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
        }
        return inflater.inflate(R.layout.fragment_book_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var bookReader = binding.ebookReader

        (requireActivity() as MainActivity).hideBottomView()

        bookUrl?.let {
            CoroutineScope(Dispatchers.IO).launch {
               try {
                   var bookReaderBuilder = if(loadFromCache) bookReader.fromFile(File(bookUrl) )
                   else{
                       var bookUrl = URL(it)
                       var inputStream =   bookUrl.openConnection().getInputStream()
                       bookReader.fromStream(inputStream)
                   }

                   bookReaderBuilder.enableSwipe(true)
                       .swipeHorizontal(true)
                       .pageSnap(true)
                       .autoSpacing(true)
                       .pageFitPolicy(FitPolicy.HEIGHT)
                       .password("passcode")
                       .fitEachPage(true)
                       .pageFling(true)
                       .onTap {
                           if (isPdfOverlayVisible) hide(100)
                           else show(100)
                           true
                       }
                       .onError {
                           binding.ebookLoadingBar.visibility = View.GONE
                           Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                           findNavController().navigateUp()
                       }
                       .onPageChange { page, pageCount ->
                           var percent = (page.div(pageCount)).times(100)
                           binding.readPercentTextview.text = "${percent}%"
                           binding.totalpageTextview.text = "page $page of $pageCount"
                           binding.readPageProgress.progress = page
                       }
                       .onPageError { page, t ->
                           Log.i("pdferrorpage", "$page ${t.message}")
                       }
                       .onLoad {
                           binding.readPageProgress.max = it
                           binding.readPercentTextview.text = "0%"
                           binding.totalpageTextview.text = "page 1 of $it"
                           binding.ebookLoadingBar.visibility = View.GONE
                           Toast.makeText(requireContext(), "$it saved ", Toast.LENGTH_SHORT).show()
                       }
                       .load()

                       bookReader.pageFillsScreen()

               }catch (e : Throwable){
                   Log.i("pdfer" , "${e.toString()}")
               }

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            Toast.makeText(requireContext() , "${bookReader.currentPage} saved " , Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        hide(300)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun hide(delay : Long){
        var hideRunnable = java.lang.Runnable {
            val flags =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            activity?.window?.decorView?.systemUiVisibility = flags
            (activity as? AppCompatActivity)?.supportActionBar?.hide()
           binding.readPageProgress.visibility = View.GONE
            binding.readPercentTextview.visibility = View.GONE
            binding.totalpageTextview.visibility = View.GONE
            isPdfOverlayVisible = false
        }
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable , delay)
    }

    fun show(delay : Long){
        var showRunnable = Runnable{
            binding.readPageProgress.visibility = View.VISIBLE
            binding.readPercentTextview.visibility = View.VISIBLE
            binding.totalpageTextview.visibility = View.VISIBLE
            (activity as? AppCompatActivity)?.supportActionBar?.show()
        }
        binding.ebookReader?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isPdfOverlayVisible = true

        // Schedule a runnable to display UI elements after a delay
        handler.removeCallbacks(showRunnable)
        handler.postDelayed(showRunnable, delay)

    }


}