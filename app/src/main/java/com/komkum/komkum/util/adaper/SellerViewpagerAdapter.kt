package com.komkum.komkum.util.adaper

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.Seller
import com.komkum.komkum.ui.store.ProductListFragment
import com.komkum.komkum.ui.store.seller.SellerHomeFragment

class SellerViewpagerAdapter(var fm : FragmentActivity , var seller_Id : String , var sellerInfo : Seller<Product>) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> SellerHomeFragment().apply {
                arguments = bundleOf("SELLER_ID" to seller_Id , "SELLER_INFO"  to sellerInfo)
            }
            1 -> ProductListFragment().apply {
                arguments = bundleOf("LOAD_TYPE" to ProductListFragment.LOAD_FROM_PREV_FRAGMENT , "PRODUCTS" to sellerInfo.products)
            }
            else -> SellerHomeFragment().apply {
                arguments = bundleOf("SELLER_INFO"  to sellerInfo)
            }
        }
    }
}