package com.komkum.komkum.ui.book

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Book
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentBookListBinding
import com.komkum.komkum.util.adaper.BookAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener

class BookListFragment : Fragment() , IRecyclerViewInteractionListener<Book<String>> {

    lateinit var binding : FragmentBookListBinding
    var bookList : List<Book<String>>? = null
    var showFilter = false
    var title : String? = null
    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                bookList = it.getParcelableArrayList("BOOK_LIST")!!
                title = it.getString("TITLE")
                showFilter = it.getBoolean("SHOW_FILTER")
                requireActivity().invalidateOptionsMenu()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , title)
        bookList?.let {
            var info = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = BookAdapter.VERTICAL_BOOK_LIST_ITEM)
            var bookAdapter = BookAdapter(info , it)
            binding.bookListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.bookListRecyclerview.adapter = bookAdapter
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(!showFilter) menu.removeItem(R.id.book_filter_menu_item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.book_list_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> findNavController().navigateUp()
            R.id.book_filter_menu_item -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
        var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
        if(data.format == Book.AudiobookFormat) findNavController().navigate(R.id.audiobookSellFragment , bundle)
        else findNavController().navigate(R.id.EBookSellFragment , bundle)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}