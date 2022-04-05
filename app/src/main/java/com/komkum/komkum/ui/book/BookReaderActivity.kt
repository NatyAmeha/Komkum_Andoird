package com.komkum.komkum.ui.book

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.observe
import com.komkum.komkum.R

import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.komkum.komkum.databinding.ActivityBookReaderBinding
import dagger.hilt.android.AndroidEntryPoint



import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL


@AndroidEntryPoint
class BookReaderActivity : AppCompatActivity() {

    var bookUrl : String? = null
    var bookId : String? = null
    var currentPosition : Int = 0
    var loadFromCache = false
    var handler = Handler()
    var isPdfOverlayVisible = false

    lateinit var binding : ActivityBookReaderBinding
    lateinit var bookReader : PDFView

    val bookViewmodel : BookViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityBookReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookReader = binding.ebookReader
        intent?.apply {
            bookUrl = getStringExtra("BOOK_URL")
            bookId = getStringExtra("BOOK_ID")
            loadFromCache = getBooleanExtra("LOAD_FROM_CACHE" , false)
            currentPosition = getIntExtra("CURRENT_POSITION" , 0)
        }



        Log.i("book url " , bookUrl.toString())
        bookUrl?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    var bookReaderBuilder = if(loadFromCache){
                        bookReader.fromFile(File(bookUrl) ).defaultPage(currentPosition)
                    }
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
                            Toast.makeText(this@BookReaderActivity, it.message, Toast.LENGTH_LONG).show()
                            super.onBackPressed()
                        }
                        .onPageChange { page, pageCount ->
                            binding.readPercentTextview.text = "$page"
                            binding.totalpageTextview.text = "page $page of $pageCount"
                            binding.readPageProgress.progress = page
                        }
                        .onPageError { page, t ->
                            Log.i("pdferrorpage", "$page ${t.message}")
                            Toast.makeText(this@BookReaderActivity , "$page ${t.message}" , Toast.LENGTH_LONG).show()
                        }
                        .onLoad {
                            binding.readPageProgress.max = it
//                            read_percent_textview.text = "${(currentPosition / it) * 100}%"
//                            totalpage_textview.text = "page 1 of $it"
                           binding.ebookLoadingBar.visibility = View.GONE
                            hide(100)
                        }
                        .load()

                    bookReader.pageFillsScreen()

                }catch (e : Throwable){
                    Log.i("pdfer" , "${e.toString()}")
                }

            }
        }

        binding.readPageProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
               binding.readPercentTextview.text = p0?.progress.toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0?.progress?.let {
                    bookReader.jumpTo(it)
//                    read_percent_textview.text = "${(it / bookReader.pageCount) * 100}%"
                }
            }

        })
    }

    override fun onBackPressed() {
        bookId?.let {
            bookViewmodel.updateCurrentReadingPage(it , bookReader.currentPage).observe(this){}
        }
        super.onBackPressed()

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
            window?.decorView?.systemUiVisibility = flags
            (this as? AppCompatActivity)?.supportActionBar?.hide()
            binding.readerInfoContainer.visibility = View.GONE
            isPdfOverlayVisible = false
        }
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable , delay)
    }

    fun show(delay : Long){
        var showRunnable = Runnable{
           binding.readerInfoContainer.visibility = View.VISIBLE
            (this as? AppCompatActivity)?.supportActionBar?.show()
        }
        binding.ebookReader?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isPdfOverlayVisible = true

        // Schedule a runnable to display UI elements after a delay
        handler.removeCallbacks(showRunnable)
        handler.postDelayed(showRunnable, delay)

    }
}