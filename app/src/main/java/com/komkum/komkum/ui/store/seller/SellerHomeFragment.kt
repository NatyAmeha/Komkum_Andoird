package com.komkum.komkum.ui.store.seller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Seller
import com.komkum.komkum.databinding.FragmentSellerHomeBinding
import com.komkum.komkum.ui.book.ReviewListFragment
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.extensions.convertLocationToAddress
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import kotlin.math.roundToInt

class SellerHomeFragment : Fragment() , IRecyclerViewInteractionListener<Product> {

    lateinit var binding : FragmentSellerHomeBinding

    var sellerId : String? = null
    var sellerData : Seller<Product>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sellerId = it.getString("SELLER_ID")
            sellerData = it.getParcelable("SELLER_INFO")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSellerHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sellerData?.let {
            var miniProductDisplayinfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.MINI_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
            binding.miniDisplayInfo = miniProductDisplayinfo
            var bestSellingProducts = it.products?.sortedByDescending { it.totalSell }?.take(8)
            binding.products = bestSellingProducts
            binding.sellerNameTextview.text = it.name
            binding.sellerDescTextview.text = it.desc
            binding.sellerPhoneTextview.text = it.address?.phone
            binding.sellerEmailTextview.text = it.address?.email
            binding.sellerCityTextview.text = it.address?.city

            it.address?.let { addr ->
                binding.sellerAddressTextview.text =  if(addr.address != null) addr.address
                else {
                    var longtiude = addr.location?.coordinates?.get(0)
                    var latitiude = addr.location?.coordinates?.get(1)
                    if(longtiude != null && latitiude != null){
                        var address =  convertLocationToAddress(requireContext() , longtiude , latitiude)
                        address.address
                    }
                    else ""
                }
            }


            var totalRating = it.reviewInfo?.totalRating ?: 0f
            binding.totalRatingTextview.text = "${if(totalRating > 5.0) 5.0 else String.format("%.1f", totalRating)}"
            binding.totalRatingBar.rating = totalRating ?: 0f
            binding.totalReviewTextview.text = "${it.reviewInfo?.totalReview ?: 0} reviews"

            it.reviewInfo?.totalReview?.apply {
                binding.fiveStartRatingBar.max = this
                binding.fourStarRatingBar.max = this
                binding.thhreeStartRatingBar.max = this
                binding.twoStarRatingBar.max = this
                binding.oneStarRatingBar.max = this
            }

            binding.fiveStartRatingBar.progress = it.reviewInfo?.fiveStar ?: 0
            binding.fourStarRatingBar.progress =  it.reviewInfo?.fourStar ?: 0
            binding.thhreeStartRatingBar.progress =  it.reviewInfo?.threeStar ?: 0
            binding.twoStarRatingBar.progress =  it.reviewInfo?.twoStar ?: 0
            binding.oneStarRatingBar.progress =  it.reviewInfo?.oneStar ?: 0

            binding.seeAllReviewTextview.setOnClickListener {view ->
                (requireActivity() as MainActivity).moveToReviewList(sellerId!! , ReviewListFragment.LOAD_SELLER_REVIEWS)
            }
            binding.fiveStartRatingBar.setOnClickListener {view ->
                (requireActivity() as MainActivity).moveToReviewList(sellerId!! , ReviewListFragment.LOAD_SELLER_REVIEWS , "5")
            }
            binding.fourStarRatingBar.setOnClickListener {view ->
                (requireActivity() as MainActivity).moveToReviewList(sellerId!! , ReviewListFragment.LOAD_SELLER_REVIEWS , "4")
            }
            binding.thhreeStartRatingBar.setOnClickListener {view ->
                (requireActivity() as MainActivity).moveToReviewList(sellerId!! , ReviewListFragment.LOAD_SELLER_REVIEWS , "3")
            }
            binding.twoStarRatingBar.setOnClickListener {view ->
                (requireActivity() as MainActivity).moveToReviewList(sellerId!! , ReviewListFragment.LOAD_SELLER_REVIEWS , "2")
            }
            binding.oneStarRatingBar.setOnClickListener {view ->
                (requireActivity() as MainActivity).moveToReviewList(sellerId!! , ReviewListFragment.LOAD_SELLER_REVIEWS , "1")
            }
        }

        binding.sellerDescTextview.setOnClickListener {
            if(binding.sellerDescTextview.maxLines == 5) binding.sellerDescTextview.maxLines = 1000
            else binding.sellerDescTextview.maxLines = 5
        }


    }

    override fun onItemClick(data: Product, position: Int, option: Int?) {
        var preference = PreferenceHelper.getInstance(requireContext())
        preference[PreferenceHelper.PURCHASED_FROM] = null
        (requireActivity() as MainActivity).movetoProductDetails(data._id!!)

    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}