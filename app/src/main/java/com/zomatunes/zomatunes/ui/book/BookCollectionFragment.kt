package com.zomatunes.zomatunes.ui.book

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Book
import com.zomatunes.zomatunes.data.model.BookCollection
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.FragmentBookCollectionBinding
import com.zomatunes.zomatunes.util.adaper.BookAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.appbar.AppBarLayout

class BookCollectionFragment : Fragment() , IRecyclerViewInteractionListener<Book<String>> {

    lateinit var binding : FragmentBookCollectionBinding

    val bookViewmodel : BookViewModel by viewModels()

    var bookCollection : BookCollection? = null
    var backgroundColor : Int = R.color.primaryColor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookCollection = it.getParcelable("BOOK_COLLECTION")
            backgroundColor = it.getInt("BACKGROUND_COLOR")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
       binding = FragmentBookCollectionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(verticalOffset == 0) binding.toolbar.visibility = View.INVISIBLE
            else if(verticalOffset == -appBarLayout.totalScrollRange) binding.toolbar.visibility = View.VISIBLE
        })
       bookCollection?.let {
           initView(it)
       }
    }

    fun initView(bookCollection: BookCollection){
        configureActionBar(binding.toolbar , bookCollection.name)
        binding.bookCollectionHeaderCustomview.hideText()
//        binding.toolbar.title = bookCollection.name
        binding.bookCollectionTitleTextview.text = bookCollection.name
        binding.bookCollectionsDescriptionTextview.text = bookCollection.description
        binding.bookCollectionHeaderCustomview.inflateData(bookCollection , backgroundColor)
        binding.curatorTextview.text = bookCollection.curator

        var info = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = BookAdapter.VERTICAL_BOOK_LIST_ITEM)
        var bookAdapter = BookAdapter(info , bookCollection.books!!)
        binding.bookListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.bookListRecyclerview.adapter = bookAdapter
    }

    override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
        var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
        findNavController().navigate(R.id.EBookSellFragment , bundle)
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}