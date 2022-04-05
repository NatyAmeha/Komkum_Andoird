package com.komkum.komkum.ui.store.team

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.databinding.FragmentFindMyAddressBinding
import com.komkum.komkum.ui.store.order.OrderViewmodel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FindMyAddressFragment : Fragment() {

    lateinit var binding : FragmentFindMyAddressBinding

    val orderViewmodel : OrderViewmodel by viewModels()

    var suggestedAddresses : List<Address> = emptyList()
    var selectedAddrFromSuggestion : Address? = null
    lateinit var suggesstionListAdapter : ArrayAdapter<String>
    var startSearchingSuggestion = true

    var newLatitude : Float? = null
    var newLongitude : Float? = null

    lateinit var placesClient : PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFindMyAddressBinding.inflate(inflater)
        return binding.root
        // Inflate the layout for this fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.find_my_location_for_default_location))
        var apiKey = "AIzaSyDaf0CADtFtCQJH-QeotjEgBACQKGFhob4"
        Places.initialize(requireContext() , apiKey)
        placesClient = Places.createClient(requireContext())

        binding.addressTextinputLayout.setEndIconOnClickListener {
            binding.addressTextview.setText("")
            binding.suggestedAddressListView.adapter = null
            startSearchingSuggestion = true
        }



        var placeSessionToken = AutocompleteSessionToken.newInstance()


        binding.addressTextview.doOnTextChanged { text, start, before, count ->
            if(count >=3 && startSearchingSuggestion){
                try{
                    binding.progressBar9.isVisible = true
                    orderViewmodel.getPlaceSuggestion(text.toString() , placeSessionToken , placesClient).observe(viewLifecycleOwner){
                        binding.progressBar9.isVisible = false
                        if(it.isNotEmpty()){
                            suggestedAddresses = it
                            binding.addressTextinputLayout.isHelperTextEnabled = false
                            var addresses = it.map { it.address }
                            suggesstionListAdapter = ArrayAdapter(requireContext() , android.R.layout.simple_list_item_1 , addresses)
                            binding.suggestedAddressListView.adapter = suggesstionListAdapter
                        }
                        else{
                            suggesstionListAdapter = ArrayAdapter(requireContext() , android.R.layout.simple_list_item_1 , listOf("place not found"))
                            binding.suggestedAddressListView.adapter = suggesstionListAdapter
                        }
                    }
                }catch (ex : Throwable){
                    binding.progressBar9.isVisible = false
                    Toast.makeText(requireContext() , getString(R.string.error_message) , Toast.LENGTH_LONG).show()
                }

            }
        }

        binding.suggestedAddressListView.setOnItemClickListener { adapterView, view, position, l ->
            selectedAddrFromSuggestion = suggestedAddresses.getOrNull(position)
            selectedAddrFromSuggestion?.let {
                orderViewmodel.getPlaceDetails(it._id!! , placesClient).observe(viewLifecycleOwner){
                    newLongitude = it.location?.coordinates?.getOrNull(0)?.toFloat()
                    newLatitude = it.location?.coordinates?.getOrNull(1)?.toFloat()
                    startSearchingSuggestion = false
                    binding.addressTextview.setText(suggestedAddresses.getOrNull(position)?.address)
                    binding.findMyLocationBtn.isEnabled = true
                    binding.suggestedAddressListView.adapter = null
                }
            }
        }

        binding.findMyLocationBtn.setOnClickListener {
            if(newLatitude != null && newLongitude != null){
                var pref = PreferenceHelper.getInstance(requireContext())
                pref[PreferenceHelper.DEFAULT_LONGITUDE_VALUE] = newLongitude
                pref[PreferenceHelper.DEFAULT_LATITUDE_VALUE] = newLatitude
                pref[PreferenceHelper.ADDRESS_NAME] = selectedAddrFromSuggestion?.address
                pref[PreferenceHelper.IS_CURRENT_LOCATION_SELECTED] = false
                pref[PreferenceHelper.RELOAD_TEAM_PAGE_AFTER_DEFAULT_ADDRESS_CHANGED] = true

                findNavController().navigateUp()
            }
            else {
                binding.addressTextview.setText("")
                binding.suggestedAddressListView.adapter = null
                Toast.makeText(requireContext() , "Error occurred please try again" , Toast.LENGTH_LONG).show()
            }
        }
    }
}