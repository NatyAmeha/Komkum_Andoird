package com.zomatunes.zomatunes.ui.book

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.FragmentBookHomeBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.error_page.view.*

@AndroidEntryPoint
class BookHomeFragment : Fragment() , IRecyclerViewInteractionListener<Book<String>> {

    lateinit var binding : FragmentBookHomeBinding
    val bookViewModel : BookViewModel by viewModels()

    var bookHomeResult = MutableLiveData<BookHomeModel>()


    init {
        lifecycleScope.launchWhenCreated {
            bookViewModel.getBookHomeResult()
//            bookHomeResult.value = bookViewModel.getBookHomeResultBeta().data
            getFacebookFriendsPurchasedBooks()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentBookHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner

        bookViewModel.getError()



        var categoryListener = object : IRecyclerViewInteractionListener<MusicBrowse> {
            override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
                var bundle = bundleOf("BROWSE" to data)
                findNavController().navigate(R.id.bookBrowseFragment , bundle)
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

        var bookInfo = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = this , owner = viewLifecycleOwner,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        var categoryInfo = RecyclerViewHelper<MusicBrowse>("BROWSE" , interactionListener = categoryListener , owner = viewLifecycleOwner)
        var bookCollectionInfo = RecyclerViewHelper("BOOK" , interactionListener = bookCollectionListner , owner = viewLifecycleOwner,
        layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)

        binding.bookInfo = bookInfo
        binding.bookViewmodel = bookViewModel

        binding.categoryInfo = categoryInfo
        binding.bookCollectionInfo = bookCollectionInfo

        bookViewModel.bookHomeResult.observe(viewLifecycleOwner){}
        bookViewModel.error.observe(viewLifecycleOwner , Observer {
            binding.bookLoadingProgressbar.visibility = View.GONE
            binding.errorView.visibility = View.VISIBLE
            binding.errorView.error_textview.text = "Something went srong \n Please try againg or go to downloads"
            binding.errorView.goto_download_btn.setOnClickListener { findNavController().navigate(R.id.downloadListFragment) }
            binding.errorView.tryagain_btn.setOnClickListener { findNavController().navigate(R.id.bookHomeFragment) }
        })
    }

    fun getFacebookFriendsPurchasedBooks(){
        var pref = PreferenceHelper.getInstance(requireContext())
        var canaccessFbFriendsInfo = pref.get(AccountState.ACCESS_FACEBOOK_FRIENDS , false)
        if(canaccessFbFriendsInfo){
            var userId = pref.get(AccountState.USER_ID , "")
            if(userId.isNotBlank()){
                bookViewModel.getFacebookfriendsBook(userId)
            }

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