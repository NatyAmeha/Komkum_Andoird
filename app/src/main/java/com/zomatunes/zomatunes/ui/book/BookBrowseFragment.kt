package com.zomatunes.zomatunes.ui.book

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Author
import com.zomatunes.zomatunes.data.model.Book
import com.zomatunes.zomatunes.data.model.MusicBrowse
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.FragmentBookBrowseBinding
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookBrowseFragment : Fragment() , IRecyclerViewInteractionListener<Book<String>> {

    lateinit var binding : FragmentBookBrowseBinding

    val bookViewmodel : BookViewModel by viewModels()

    var browseInfo : MusicBrowse? = null

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
        var authorInfo = RecyclerViewHelper(interactionListener = authorInteractionListener , owner = viewLifecycleOwner ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)

        var bookInfo = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = this , owner = viewLifecycleOwner,
             layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        binding.bookInfo = bookInfo
        binding.authorInfo = authorInfo
        binding.bookViewmodel = bookViewmodel
        browseInfo?.let {
            bookViewmodel.getBooksHomebyGenre(it.name!!)
            configureActionBar(binding.toolbar)
            Picasso.get().load(it.imagePath?.replace("localhost" , AdapterDiffUtil.URL)).placeholder(R.drawable.backimage)
                .fit().centerCrop().into(binding.appBarImage)
            binding.bookCollapsingToolbar.title = it.name
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