package com.komkum.komkum.ui.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.ProductPackage
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentPackageListBinding
import com.komkum.komkum.ui.store.team.TeamFragment
import com.komkum.komkum.ui.store.team.TeamViewModel
import com.komkum.komkum.util.adaper.PackageAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PackageListFragment : Fragment() , IRecyclerViewInteractionListener<ProductPackage<Product>> {
    val teamViewmodel : TeamViewModel by viewModels()
    val mainactivityviewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : FragmentPackageListBinding

    var fromReminder =  false

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                fromReminder = it.getBoolean("FROM_REMINDER")
            }
            teamViewmodel.getAvailablePackages()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPackageListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       configureActionBar(binding.toolbar , getString(R.string.packages))
        var packageInfo = RecyclerViewHelper(interactionListener = this , listItemType = PackageAdapter.PACKAGE_GRID_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2  , owner = viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.teamViewmodel = teamViewmodel
        binding.packageInfo = packageInfo

        var packageTaglist = MutableLiveData<MutableList<String>>()
        packageTaglist.observe(viewLifecycleOwner){tags ->
            if(!tags.isNullOrEmpty()){
                tags.distinct().forEach {tag ->
                    var chip = Chip(requireContext())
                    chip.text = tag
                    chip.isCheckable = true
                    chip.isCheckedIconVisible = true
                    binding.packageTagsChipGroup.addView(chip)
                }
                binding.packageTagsChipGroup.setOnCheckedChangeListener { group, checkedId ->
                    binding.pakageLoadingProgressbar.isVisible = true
                    var isChecked = group.children.toList().firstOrNull() { (it as Chip).isChecked } != null
                    if(isChecked){
                        group.children.toList().firstOrNull() { (it as Chip).isChecked }?.let {
                            var tag = (it as Chip).text.toString()
                            var newList = teamViewmodel.originalPackageList.filter { it.tags?.contains(tag) == true }
                            teamViewmodel.packageLists.value = newList
                        }
                    }
                    else {
                        teamViewmodel.packageLists.value = teamViewmodel.originalPackageList
                    }
                }

                packageTaglist.removeObservers(viewLifecycleOwner)

                if(fromReminder){
                    mainactivityviewmodel.purchasedFrom = FcmService.F_EVENT_PARAM_VALUE_PURCHASED_FROM_REMINDER
                }
            }
        }

        teamViewmodel.packageLists.observe(viewLifecycleOwner){
            binding.pakageLoadingProgressbar.isVisible = false
            var tags = mutableSetOf<String>()
            teamViewmodel.originalPackageList.forEach {
                it.tags?.let { it1 -> tags.addAll(it1) }
            }
            packageTaglist.value = tags.distinct().toMutableList()

        }

        teamViewmodel.getError().observe(viewLifecycleOwner){}
        teamViewmodel.error.observe(viewLifecycleOwner){
            binding.pakageLoadingProgressbar.isVisible = false
            Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
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

    override fun onItemClick(data: ProductPackage<Product>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , loadType = TeamFragment.LOAD_PACKAGE_TEAM , backstackId = R.id.packageListFragment)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}