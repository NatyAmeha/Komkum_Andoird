package com.komkum.komkum.ui.book

import android.os.Bundle
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
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentBookHomeBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.home.BaseBottomTabFragment
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookHomeFragment : BaseBottomTabFragment() , IRecyclerViewInteractionListener<Book<String>> {

    lateinit var binding : FragmentBookHomeBinding
    val bookViewModel : BookViewModel by viewModels()

    var bookHomeResult = MutableLiveData<BookHomeModel>()


    init {
        lifecycleScope.launchWhenCreated {
//            bookViewModel.getBookHomepageResult()
////            bookHomeResult.value = bookViewModel.getBookHomeResultBeta().data
//            getFacebookFriendsPurchasedBooks()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentBookHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.lifecycleOwner = viewLifecycleOwner

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


//        bookViewModel.getError().observe(viewLifecycleOwner){}
//        bookViewModel.error.observe(viewLifecycleOwner , Observer{ error ->
//            binding.bookLoadingProgressbar.visibility = View.GONE
//            error.handleError(requireContext()){
//                binding.errorView.root.visibility = View.VISIBLE
//                binding.errorView.errorTextview.text = "Something went wrong. \nPlease try again or go to  your downloads "
//                binding.errorView.gotoDownloadBtn.setOnClickListener {
//                    findNavController().navigate(R.id.downloadListFragment)
//                }
//                binding.errorView.tryagainBtn.setOnClickListener {
//                    findNavController().navigate(R.id.bookHomeFragment)
//                }
//            }
//        })
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