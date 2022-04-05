package com.komkum.komkum.ui.book

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentReviewListBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.store.ProductViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.adaper.ReviewAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewListFragment : Fragment() {

    lateinit var binding : FragmentReviewListBinding
    val bookViewmodel : BookViewModel by viewModels()
    val productViewodel : ProductViewModel by viewModels()


    lateinit var reviewAdapter : ReviewAdapter
    var reviewType : Int? = null
    var ratingFilter : String = "all"
    var sellerId : String? = null
    var id : String? = null   //  it can be productid, ebookid , audiobookid, sellerId
    var reviewList = MutableLiveData<MutableList<Review>?>()

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                id = it.getString("ID")
                reviewType = it.getInt("REVIEW_TYPE")
                ratingFilter = it.getString("RATING_FILTER") ?: "all"
            }
            id?.let {
                when(reviewType){
                    LOAD_PRODUCT_REVIEW -> {
                        reviewList.value = productViewodel.getProductReviews(id!! , 1)
                    }
                    LOAD_SELLER_REVIEWS -> {
                        reviewList.value = productViewodel.getStoreReviews(id!! , ratingFilter)
                    }
                }
            }

        }
    }

    companion object{
        const val LOAD_PRODUCT_REVIEW = 0
        const val LOAD_EBOOK_REVIEW = 1
        const val LOAD_AUDIOBOOK_REVIEW = 2
        const val LOAD_SELLER_REVIEWS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReviewListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var toolbarTitle = when(ratingFilter){
            "all" -> "All reviews"
            "5" -> "5 star reviews"
            "4" -> "4 star reviews"
            "3" -> "3 star reviews"
            "2" -> "2 star reviews"
            "1" -> "1 star reviews"
            else -> "Reviews"
        }
        configureActionBar(binding.toolbar , toolbarTitle)
        var currentUserId = PreferenceHelper.getInstance(requireContext()).get(AccountState.USER_ID , "")

        var currentUserReview : Review? = null


        reviewList.observe(viewLifecycleOwner){
            binding.ratingListProgressbar.isVisible = false
            it?.let {
                binding.noReviewTextview.isVisible == it.isNullOrEmpty()
                if(currentUserId.isNotBlank()) currentUserReview = it.find { rating -> rating.reviewer == currentUserId }

                reviewAdapter = ReviewAdapter()
                it.sortedByDescending { rating->rating.date }
                currentUserReview?.let { it1 -> it.add(0 , it1) }

                binding.reviewListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                binding.reviewListRecyclerview.adapter = reviewAdapter
                reviewAdapter.submitList(it)
            }

        }


        productViewodel.getErrors().observe(viewLifecycleOwner){}
        productViewodel.error.observe(viewLifecycleOwner){
            binding.ratingListProgressbar.isVisible = false
            it.handleError(requireContext()){
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
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
}