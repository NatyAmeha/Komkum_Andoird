package com.zomatunes.zomatunes.ui.book

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.FragmentReviewListBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.adaper.ReviewAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewListFragment : Fragment() {

    lateinit var binding : FragmentReviewListBinding
    lateinit var reviewAdapter : ReviewAdapter
    val bookViewmodel : BookViewModel by viewModels()

    var audiobook : Book<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            audiobook = it.getParcelable("BOOK")
        }
        binding = FragmentReviewListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        audiobook?.let {
            binding.noReviewTextview.isVisible == it.rating.isNullOrEmpty()
            reviewAdapter = ReviewAdapter()
            binding.reviewListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.reviewListRecyclerview.adapter = reviewAdapter
            reviewAdapter.submitList(it.rating?.sortedByDescending { rating->rating.date })
        }

        binding.createReviewBtn.setOnClickListener {
            var preference = PreferenceHelper.getInstance(requireContext())

            binding.createRatingProgressbar.visibility = View.VISIBLE
            audiobook?.let {
                var comment = binding.writeReviewEditText.text.toString()
                var ratingValue = binding.bookRatingBar.rating
                var userId = preference.getString(AccountState.USER_ID , "")
                var username = preference.getString(AccountState.USERNAME , "")
                var ratingInfo = Rating(null , comment , userId , username , ratingValue , audiobook!!._id)
                bookViewmodel.rateBook(ratingInfo , it.format!!).observe(viewLifecycleOwner , Observer{
                    if(it){
                        binding.createRatingProgressbar.visibility = View.GONE
                        binding.writeReviewEditText.setText("")
                        var reviewList = reviewAdapter.currentList.toMutableList()
                        reviewList.add(0 , ratingInfo)
                        reviewAdapter.submitList(reviewList.distinctBy { rating -> rating.userId })
                    }
                })
            }

        }
    }

}