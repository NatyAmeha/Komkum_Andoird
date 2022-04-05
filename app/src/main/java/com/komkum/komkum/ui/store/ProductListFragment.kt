package com.komkum.komkum.ui.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentProductListBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductListFragment : Fragment() , IRecyclerViewInteractionListener<Product> {

    lateinit var binding : FragmentProductListBinding
    val productViewmodel : ProductViewModel  by viewModels()

    var loadType : Int? = -1
    var category : String? = null
    var products : List<Product>? = null  // have a value if previous fragment send list of products
    var query : String? = null  // contains a value when find products by tags

    var toolbarTitle : String? = null

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                loadType = it.getInt("LOAD_TYPE")
                category = it.getString("CATEGORY")
                products = it.getParcelableArrayList("PRODUCTS")
                query = it.getString("TAG")
            }

            when(loadType){
                LOAD_PRODUCT_LIST_BY_CATEGORY -> category?.let {
                    toolbarTitle = it
                    productViewmodel.getBrowsebyCategoryResult(it)
                }
                LOAD_TRENDING_PRODUCTS -> {
                    toolbarTitle = getString(R.string.trending_products)
                    productViewmodel.getTrendingProducts(50)
                }
                LOAD_BESTSELLING_PRODUCTS ->{
                    toolbarTitle = getString(R.string.bestselling_products)
                    productViewmodel.getBestsellingProducts(30)
                }
                LOAD_WISHLIST -> {
                    toolbarTitle = getString(R.string.your_list)
                    productViewmodel.getWishListProducts()
                }
                LOAD_NEW_ARRIVAL ->{
                    toolbarTitle = getString(R.string.new_arrival)
                    productViewmodel.getNewArrivals()
                }
                LOAD_BY_TAG -> {
                    query?.let {
                        toolbarTitle = it
                        productViewmodel.searchProducts(it)
                    }
                }
                LOAD_FROM_PREV_FRAGMENT -> {
                    productViewmodel.productList.value = products
                }
            }
        }
    }

    companion object{
        const val LOAD_PRODUCT_LIST_BY_CATEGORY = 0
        const val LOAD_TRENDING_PRODUCTS = 1
        const val LOAD_BESTSELLING_PRODUCTS = 2
        const val LOAD_WISHLIST = 3
        const val LOAD_BY_TAG = 4
        const val LOAD_FROM_PREV_FRAGMENT = 5
        const val LOAD_NEW_ARRIVAL = 6
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentProductListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(loadType == LOAD_FROM_PREV_FRAGMENT) binding.appbar.isVisible = false
        else configureActionBar(binding.toolbar , toolbarTitle)

//        var gridListInteractionListener = object : IRecyclerViewInteractionListener<Product> {
//            override fun onItemClick(data: Product, position: Int, option: Int?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun activiateMultiSelectionMode() {}
//
//            override fun onSwiped(position: Int) {}
//
//            override fun onMoved(prevPosition: Int, newPosition: Int) {}
//        }

        var verticalProductList = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.VERTICAL_PRODUCT_LIST_ITEM)
        var gridProductList = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.MAIN_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.productInfo = verticalProductList
        binding.productViewmodel = productViewmodel



        productViewmodel.productList.observe(viewLifecycleOwner){
            binding.errorTextview.isVisible = false
            it?.let {
                if(it.isEmpty()){
                    binding.errorTextview.text = getString(R.string.no_product_found)
                    binding.errorTextview.isVisible = true
                }
                var productadapter : ProductAdapter
                when(loadType){
                    LOAD_FROM_PREV_FRAGMENT, LOAD_BESTSELLING_PRODUCTS,
                    LOAD_TRENDING_PRODUCTS, LOAD_NEW_ARRIVAL , LOAD_BY_TAG, LOAD_PRODUCT_LIST_BY_CATEGORY -> {
                        binding.productListRecyclerview.layoutManager = GridLayoutManager(context , 2)
                        productadapter = ProductAdapter(gridProductList , it)
                    }
                    else -> {
                        binding.productListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                        productadapter = ProductAdapter(verticalProductList , it)
                    }
                }
                binding.productListRecyclerview.adapter = productadapter

            }
        }

        productViewmodel.getErrors().observe(viewLifecycleOwner){}
        productViewmodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                binding.productListLoadingProgressbar.isVisible = false
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                if(productViewmodel.productList.value == null) binding.errorTextview.isVisible = true
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

    override fun onItemClick(data: Product, position: Int, option: Int?) {
        var preference = PreferenceHelper.getInstance(requireContext())
        preference[PreferenceHelper.PURCHASED_FROM] = null
        (requireActivity() as MainActivity).movetoProductDetails(data._id!!)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}