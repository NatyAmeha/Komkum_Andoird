package com.komkum.komkum.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import com.mancj.materialsearchbar.MaterialSearchBar
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.ProductPackage
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentProductSearchBinding
import com.komkum.komkum.ui.store.ProductViewModel
import com.komkum.komkum.ui.store.team.TeamFragment
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.PackageAdapter
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductSearchFragment : Fragment() , MaterialSearchBar.OnSearchActionListener ,
    IRecyclerViewInteractionListener<Product> {
    lateinit var binding : FragmentProductSearchBinding
    val productViewmodel : ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = resources.getColor(R.color.light_background)
        }
        binding = FragmentProductSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.productSearchview.openSearch()
        var packageListener = object : IRecyclerViewInteractionListener<ProductPackage<Product>> {
            override fun onItemClick(data: ProductPackage<Product>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , loadType = TeamFragment.LOAD_PACKAGE_TEAM , backstackId = R.id.storeHomepageFragment)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var productInfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.VERTICAL_PRODUCT_LIST_ITEM)
        var packageInfo = RecyclerViewHelper(interactionListener = packageListener , listItemType = PackageAdapter.PACKAGE_MAIN_LIST_ITEM  , owner = viewLifecycleOwner)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.productInfo = productInfo
        binding.packageInfo = packageInfo
        binding.productViewmodel = productViewmodel
        binding.productSearchview.setOnSearchActionListener(this)

        productViewmodel.productList.observe(viewLifecycleOwner){
            binding.productSearchProgressbar.isVisible = false
            binding.searchErrorTextview.isVisible = false
            if(it.isNullOrEmpty()){
                binding.searchErrorTextview.text = getString(R.string.no_product_found)
                binding.searchErrorTextview.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = resources.getColor(R.color.light_secondaryDarkColor)
        }
        super.onDestroyView()
    }

    override fun onSearchStateChanged(enabled: Boolean) {}

    override fun onSearchConfirmed(text: CharSequence?) {
        binding.searchErrorTextview.isVisible = false
        text?.let {
            binding.productSearchProgressbar.isVisible = true
            productViewmodel.searchProducts(text.toString())
        }
    }

    override fun onButtonClicked(buttonCode: Int) {
        Toast.makeText(requireContext() , buttonCode , Toast.LENGTH_SHORT).show()
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