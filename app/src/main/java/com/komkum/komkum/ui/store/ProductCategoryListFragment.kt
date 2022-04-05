package com.komkum.komkum.ui.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Category
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentProductCategoryListBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductCategoryListFragment : Fragment() , IRecyclerViewInteractionListener<Category> {

    lateinit var binding : FragmentProductCategoryListBinding
    val productViewmodel : ProductViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()


    init {
        lifecycleScope.launchWhenCreated {
            productViewmodel.getProductBrowseCategories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProductCategoryListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar, getString(R.string.browse_categories))
        var categoryInfo = RecyclerViewHelper(interactionListener = this , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.categoryInfo = categoryInfo
        binding.productViewmodel = productViewmodel

        productViewmodel.getErrors().observe(viewLifecycleOwner){}
        productViewmodel.error.observe(viewLifecycleOwner){
            binding.categoryListProgressbar.isVisible = false
            it.handleError(requireContext() , signupSource = mainActivityViewmodel.signUpSource){
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

    override fun onItemClick(data: Category, position: Int, option: Int?) {
        (requireActivity() as MainActivity).moveToBrowseProductByDepartment(data.name!!)
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}