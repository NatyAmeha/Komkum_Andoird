package com.zomatunes.zomatunes.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.FragmentBookSearchBinding
import com.zomatunes.zomatunes.util.adaper.BrowseCategoryAdapter
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSearchFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {
    lateinit var binding : FragmentBookSearchBinding

    val searchViewmodel : SearchViewModel by viewModels()

    var searchResult : Search? = null
    var browseList : List<MusicBrowse> = emptyList()
    var showBrowseItem = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            searchResult = it.getParcelable<Search>("SEARCH_RESULT")
            browseList = it.getParcelableArrayList("BROWSE_LIST") ?: emptyList()
        }
        binding = FragmentBookSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (binding.bookInterestView.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_album_24)
        (binding.bookInterestView.findViewById(R.id.library_item_textview) as TextView).text = "Interest"
        binding.bookInterestView.setOnClickListener {
            browseList.let{
                var browseInfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner)
                var browseList = it.groupBy { browse -> browse.contentType }

                binding.searchContainer.visibility = View.GONE
                binding.bookInteresetRecyclerview.visibility = View.VISIBLE
                showBrowseItem = !showBrowseItem

                browseList[MusicBrowse.CONTENT_TYPE_BOOK]?.let { browseList ->
                    var list = browseList.groupBy { browse -> browse.category }
                    var categoryBrowseAdapter = BrowseCategoryAdapter(browseInfo , list)
                    binding.bookInteresetRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.bookInteresetRecyclerview.adapter = categoryBrowseAdapter
                }
            }

        }

       searchResult?.let {
           var bookInteractionListener = object : IRecyclerViewInteractionListener<Book<String>> {
               override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
                   var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
                   if(data.format == Book.EbookFormat) findNavController().navigate(R.id.EBookSellFragment , bundle)
                   else findNavController().navigate(R.id.audiobookSellFragment , bundle)
               }
               override fun activiateMultiSelectionMode() {}
               override fun onSwiped(position: Int) {}
               override fun onMoved(prevPosition: Int, newPosition: Int) {}
           }

           var authorInteractionListener = object : IRecyclerViewInteractionListener<Author<String, String>>{
               override fun onItemClick(data: Author<String,String>, position: Int, option: Int?) {
                   (requireActivity() as MainActivity).movetoAuthor(data._id!!)
               }
               override fun activiateMultiSelectionMode() {}
               override fun onSwiped(position: Int) {}
               override fun onMoved(prevPosition: Int, newPosition: Int) {}
           }

           var bookInfo = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = bookInteractionListener , owner = viewLifecycleOwner,
               layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
           var authorInfo = RecyclerViewHelper<Author<String , String>>(interactionListener = authorInteractionListener , owner = viewLifecycleOwner ,
               layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
           binding.bookInfo = bookInfo
           binding.authorInfo = authorInfo
           binding.searchResult = it
       }

//        (requireActivity() as MainActivity).onBackPressedDispatcher.addCallback(viewLifecycleOwner){
//            if(showBrowseItem){
//                binding.bookInteresetRecyclerview.visibility = View.GONE
//                binding.searchContainer.visibility = View.VISIBLE
//            }
//            else handleOnBackPressed()
//        }

        binding.viewMoreAudioBookBtn.setOnClickListener {
             searchResult?.audiobooks?.let {
                 (requireActivity() as MainActivity).showBookList(it ,"Audiobooks (${it.size})" , false)
             }
        }

        binding.viewAllEbookBtn.setOnClickListener {
            searchResult?.books?.let {
                (requireActivity() as MainActivity).showBookList(it ,"Ebooks (${it.size})" , false)
            }
        }
    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var bundle = bundleOf("BROWSE" to data)
        when(data.contentType){
            MusicBrowse.CONTENTY_TYPE_MUSIC ->  findNavController().navigate(R.id.browseMusicFragment , bundle)
            MusicBrowse.CONTENT_TYPE_BOOK -> findNavController().navigate(R.id.bookBrowseFragment , bundle)
        }
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}