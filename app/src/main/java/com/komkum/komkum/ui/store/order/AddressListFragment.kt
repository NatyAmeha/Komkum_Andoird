package com.komkum.komkum.ui.store.order

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
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.data.model.Order
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentAddressListBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressListFragment : Fragment() , IRecyclerViewInteractionListener<Address> {

    lateinit var binding : FragmentAddressListBinding
    val orderViewmodel : OrderViewmodel by viewModels()

    var orderInfo : Order<String, Address>? = null
    var addressList : List<Address>? = null
    var maxDeliveryDay = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            orderInfo = it.getParcelable("ORDER")
            addressList = it.getParcelableArrayList("ADDRESS_LIST")
            maxDeliveryDay = it.getInt("MAX_DELIVERY_DAY")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddressListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.shipping_address))
        var addressInfo = RecyclerViewHelper(interactionListener = this)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.addressInfo = addressInfo
        binding.addressList = addressList?.sortedBy { address -> address.default == true }

//        orderViewmodel.getUserShippingAddress()

        binding.addNewAddressBtn.setOnClickListener {
            orderInfo?.let { it1 -> (requireActivity() as MainActivity).moveToCreateAddress(it1, addressList ?: emptyList() , maxDeliveryDay = maxDeliveryDay) }
        }


        orderViewmodel.getError().observe(viewLifecycleOwner){}
        orderViewmodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                orderViewmodel.removeOldError()
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                if(addressList.isNullOrEmpty()) binding.errorTextview.isVisible = true
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

    override fun onItemClick(data: Address, position: Int, option: Int?) {
        orderInfo?.let {
            it.discountCodes = mutableListOf()
            (requireActivity() as MainActivity).movetoOrderFragment(it, data) }
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}