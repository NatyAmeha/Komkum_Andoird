package com.komkum.komkum.ui.store.seller

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.databinding.SellerFragmentBinding
import com.komkum.komkum.util.adaper.SellerViewpagerAdapter
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SellerFragment : Fragment() {
    lateinit var binding: SellerFragmentBinding
    var sellerId : String? = null
    val sellerViewmodel: SellerViewModel by viewModels()

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                sellerId = it.getString("SELLER_ID")
                sellerId?.let {
                    sellerViewmodel.getSellerDetails(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SellerFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.sellerViewpager.isUserInputEnabled = false
        sellerViewmodel.sellerInfo.observe(viewLifecycleOwner) {
            binding.sellerProgressbar.isVisible = false
            it?.let { sellerInfo ->
                binding.cToolbar.title = sellerInfo.name
                Picasso.get().load(sellerInfo.images?.first()).fit().centerCrop().into(binding.appBarImage)
                binding.appbar.isVisible = true
                var viewpager = binding.sellerViewpager
                viewpager.adapter = SellerViewpagerAdapter(requireActivity() , sellerId!! , it)
                TabLayoutMediator(binding.sellerTabLayout, viewpager) { tab, position ->
                    when (position) {
                        0 -> tab.text = "${getString(R.string.title_home)}"
                        1 -> tab.text = "${getString(R.string.products)}"
                    }
                }.attach()
            }
        }
    }


}