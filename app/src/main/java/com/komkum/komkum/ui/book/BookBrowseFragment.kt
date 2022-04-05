package com.komkum.komkum.ui.book

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.databinding.FragmentBookBrowseBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookBrowseFragment : Fragment() , IRecyclerViewInteractionListener<Book<String>> {

    lateinit var binding : FragmentBookBrowseBinding

    val bookViewmodel : BookViewModel by viewModels()

    var browseInfo : MusicBrowse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            browseInfo = it.getParcelable("BROWSE")
        }
        binding = FragmentBookBrowseBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        var authorInteractionListener = object : IRecyclerViewInteractionListener<Author<String,String>>{
            override fun onItemClick(data: Author<String,String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoAuthor(data._id!!)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var bookCollectionListner = object : IRecyclerViewInteractionListener<BookCollection> {
            override fun onItemClick(data: BookCollection, position: Int, option: Int?) {
                var backgroudColorList = listOf<Int>(R.color.primaryColor , R.color.secondaryColor , R.color.sg1 , R.color.sg2 , R.color.secondaryTextColor)
                var bundle = bundleOf("BOOK_COLLECTION" to data , "BACKGROUND_COLOR" to (backgroudColorList[position % backgroudColorList.size]))
                findNavController().navigate(R.id.bookCollectionFragment , bundle)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var bookCollectionInfo = RecyclerViewHelper("BOOK" , interactionListener = bookCollectionListner , owner = viewLifecycleOwner,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)

        var authorInfo = RecyclerViewHelper(interactionListener = authorInteractionListener , owner = viewLifecycleOwner ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)

        var bookInfo = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = this , owner = viewLifecycleOwner,
             layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        binding.bookInfo = bookInfo
        binding.authorInfo = authorInfo
        binding.bookCollectionInfo = bookCollectionInfo
        binding.bookViewmodel = bookViewmodel
        browseInfo?.let {
            bookViewmodel.getBooksHomebyGenre(it.name!!)
            configureActionBar(binding.toolbar)
            Picasso.get().load(it.imagePath).placeholder(R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.appBarImage)
            binding.bookCollapsingToolbar.title = it.name
        }

        bookViewmodel.bookHomeResult.observe(viewLifecycleOwner){
            if (it == null || (it.bestSellingAudiobooks.isNullOrEmpty() && it.bestSellingBooks.isNullOrEmpty())) {
                binding.bookHomeLoadingProgressbar.isVisible = false
                binding.bookBrowseErrorTextview.isVisible = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
        var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
        if(data.format == Book.EbookFormat) findNavController().navigate(R.id.EBookSellFragment , bundle)
        else findNavController().navigate(R.id.audiobookSellFragment , bundle)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}