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
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Category
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.ProductPackage
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentBrowseProductBinding
import com.komkum.komkum.ui.store.team.TeamFragment
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.PackageAdapter
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseProductFragment : Fragment(), IRecyclerViewInteractionListener<Product> {

    lateinit var binding : FragmentBrowseProductBinding
    val productViewModel : ProductViewModel by viewModels()

    var selectedDepartment : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
           selectedDepartment = it.getString("DEPARTMENT")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBrowseProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , selectedDepartment ?: "Browse")
        var categoryListener = object : IRecyclerViewInteractionListener<Category> {
            override fun onItemClick(data: Category, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_PRODUCT_LIST_BY_CATEGORY , data.name)
            }

            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var packageListener = object : IRecyclerViewInteractionListener<ProductPackage<Product>> {
            override fun onItemClick(data: ProductPackage<Product>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , loadType = TeamFragment.LOAD_PACKAGE_TEAM , backstackId = R.id.storeHomepageFragment)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var categoryInfo = RecyclerViewHelper(interactionListener = categoryListener , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
        var productInfo = RecyclerViewHelper(interactionListener = this , listItemType = ProductAdapter.MAIN_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2)
        var horizonatalProductInfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.MINI_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        var packageInfo = RecyclerViewHelper(interactionListener = packageListener , listItemType = PackageAdapter.PACKAGE_MAIN_LIST_ITEM  , owner = viewLifecycleOwner)


        binding.lifecycleOwner = viewLifecycleOwner
        binding.productInfo = productInfo
        binding.miniDisplayInfo = horizonatalProductInfo
        binding.categoryInfo = categoryInfo
        binding.packageInfo = packageInfo
        binding.productViewmodel = productViewModel

        selectedDepartment?.let { productViewModel.getBrowseListbyDepResult(it) }

        productViewModel.getErrors().observe(viewLifecycleOwner){}
        productViewModel.error.observe(viewLifecycleOwner){
            binding.loadingProgressbar.isVisible = false
            it.handleError(requireContext()){

                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                binding.errorTextview.isVisible = true
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