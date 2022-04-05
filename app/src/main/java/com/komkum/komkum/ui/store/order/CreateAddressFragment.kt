package com.komkum.komkum.ui.store.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.data.model.GeoSpacial
import com.komkum.komkum.data.model.Order
import com.komkum.komkum.databinding.FragmentCreateAddressBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateAddressFragment : Fragment() {

    lateinit var binding :FragmentCreateAddressBinding
    val orderViewmodel : OrderViewmodel by viewModels()

    var orderInfo : Order<String, Address>? = null
    var addressList : List<Address>? = null

    var userCurrentLocationAddress : Address? = null
    var userCurrntAddress : String? = null

    var selectedAddress : Address? = null
    var suggestedAddresses : List<Address> = emptyList()
    lateinit var suggesstionListAdapter : ArrayAdapter<String>

//    val NOT_MY_ADDRESS = getString(R.string.cant_find_my_location)
    var eligableCity  = mutableListOf<String>()
    var isExactaddress  = true

    var startSearchingSuggestion = false

    var maxDeliveryDay = 1

    var localAddressName = ""


    lateinit var placesClient : PlacesClient


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
        binding = FragmentCreateAddressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.create_address))
        var apiKey = "AIzaSyDaf0CADtFtCQJH-QeotjEgBACQKGFhob4"
        Places.initialize(requireContext() , apiKey)
        placesClient = Places.createClient(requireContext())

        //get default home address if already set
        var pref = PreferenceHelper.getInstance(requireContext())
        var defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
        var defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)
        if(defaultLatitudeValue != 0f && defaultLongitudeValue != 0f){
            binding.currentAddressSelectedCheckbox.text = getString(R.string.home_address_selected)
            var address = convertLocationToAddress(requireContext() , defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble())
            var savedAddressName = pref.get(PreferenceHelper.ADDRESS_NAME , address.address ?: "Not set")
            binding.addressTextview.setText(savedAddressName)
            selectedAddress = address
            userCurrentLocationAddress = address
        }
        else{
            // default home address not set. use current location
            requireActivity().requestAndAccessLocation {
                selectedAddress = it
                userCurrentLocationAddress = it
                binding.addressTextview.setText(it.address)
            }
        }





        orderViewmodel.getDeliveryDestination().observe(viewLifecycleOwner){
            it?.let {destinations ->
//                destinations.add(0 , getString(R.string.cant_find_my_location))
                eligableCity = destinations
                controlCitySelection(eligableCity)
            }
        }

        var userName = pref.get(AccountState.USERNAME , "")
        var phone = pref.get(AccountState.PHONE_NUMBER , "")
        var email = pref.get(AccountState.EMAIL , "")
        binding.userNameTextview.setText(userName)
        if(phone.isNotEmpty())
            binding.phonenumberTextview.setText(phone)

        binding.currentAddressSelectedCheckbox.setOnCheckedChangeListener { compoundButton, checked ->
            if(checked){
                startSearchingSuggestion = false
                binding.addressTextinputLayout.hint = "Current address"
                selectedAddress = userCurrentLocationAddress
                var address = convertLocationToAddress(requireContext() , defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble())
                var savedAddressName = pref.get(PreferenceHelper.ADDRESS_NAME , address.address ?: "Not set")
                binding.addressTextview.setText(savedAddressName)


                binding.addressTextinputLayout.isEnabled = false
                binding.addressTextinputLayout.isHelperTextEnabled = false
                binding.suggestedAddressListView.isVisible = false
                binding.currentAddressSelectedCheckbox.text = getString(R.string.current_address_selected)
            }
            else{
                selectedAddress = null
                startSearchingSuggestion = true
                binding.addressTextinputLayout.isEnabled = true
                binding.addressTextview.setText("")
                binding.addressTextinputLayout.hint = getString(R.string.type_your_address)
                binding.addressTextinputLayout.isHelperTextEnabled = true
                binding.addressTextinputLayout.helperText = getString(R.string.we_will_help_you_find_your_place)
                binding.currentAddressSelectedCheckbox.text =
                    if(defaultLatitudeValue != 0f && defaultLongitudeValue != 0f)
                        getString(R.string.use_your_home_address)
                    else getString(R.string.use_current_address)
            }
        }

        var placeSessionToken = AutocompleteSessionToken.newInstance()
        binding.addressTextview.doOnTextChanged { text, start, before, count ->
            if(count >=3 && startSearchingSuggestion){
                orderViewmodel.getPlaceSuggestion(text.toString() , placeSessionToken , placesClient).observe(viewLifecycleOwner){
                    if(it.isNotEmpty()){
                        suggestedAddresses = it
                        binding.addressTextinputLayout.isHelperTextEnabled = false
                        binding.suggestedAddressListView.isVisible = true
                        binding.suggestedAddressListView.isNestedScrollingEnabled = true
                        var addresses = it.map { it.address }
                        suggesstionListAdapter = ArrayAdapter(requireContext() , android.R.layout.simple_list_item_1 , addresses)
                        binding.suggestedAddressListView.adapter = suggesstionListAdapter
                        binding.mainContainer.smoothScrollTo(binding.currentAddressSelectedCheckbox)
                    }
                    else{
                        suggesstionListAdapter = ArrayAdapter(requireContext() , android.R.layout.simple_list_item_1 , listOf("place not found"))
                        binding.suggestedAddressListView.adapter = suggesstionListAdapter
                    }
                }
            }
        }

        binding.addressTextinputLayout.setEndIconOnClickListener {
            binding.addressTextview.setText("")
            binding.suggestedAddressListView.adapter = null
        }

//        binding.addressTextview.setOnEditorActionListener { textView, actionId, keyEvent ->
//            if(actionId == EditorInfo.IME_ACTION_DONE){
//
//            }
//        }

        binding.suggestedAddressListView.setOnItemClickListener { adapterView, view, position, l ->
            var selectedAddrFromSuggestion = suggestedAddresses.getOrNull(position)
            selectedAddrFromSuggestion?.let {
                orderViewmodel.getPlaceDetails(it._id!! , placesClient).observe(viewLifecycleOwner){
                    selectedAddress = it
                    startSearchingSuggestion = false
                    selectedAddress?.nearLocation = suggestedAddresses.getOrNull(position)?.address
                    selectedAddress?.address = suggestedAddresses.getOrNull(position)?.address
                    binding.addressTextview.setText(suggestedAddresses.getOrNull(position)?.address)
                    binding.suggestedAddressListView.isVisible = false

//                    pref[PreferenceHelper.DEFAULT_LONGITUDE_VALUE] = it.location?.coordinates?.getOrNull(0)?.toFloat()
//                    pref[PreferenceHelper.DEFAULT_LATITUDE_VALUE] = it.location?.coordinates?.getOrNull(1)?.toFloat()
//                    pref[PreferenceHelper.IS_CURRENT_LOCATION_SELECTED] = false
                }
            }
        }


        binding.createAddressBtn.setOnClickListener {
            var selectedItem = binding.cityActv.text.toString()
//            if(selectedItem == getString(R.string.cant_find_my_location)){
//                binding.createAddressBtn.isEnabled = false
//                showDialogforNearLocationSelection()
//            }
            if(eligableCity.contains(selectedItem)){
                if(checkfields()) createNewAddress(email)
                else Toast.makeText(requireContext() , getString(R.string.fill_form) , Toast.LENGTH_LONG).show()
            }
            else{
                binding.createAddressBtn.isEnabled = false
                Toast.makeText(requireContext() , getString(R.string.unsupported_delivery_destination_message) , Toast.LENGTH_LONG).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            (requireActivity() as MainActivity).showDialog(getString(R.string.discard) , null , getString(R.string.yes) , showNegative = true){
                findNavController().navigateUp()
            }
        }


        orderViewmodel.getError().observe(viewLifecycleOwner){}
        orderViewmodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                orderViewmodel.removeOldError()
                binding.createNewAddressProgressbar.isVisible = false
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       if(grantResults.isNotEmpty()){
           getLocation(requireContext()){
               var teamAddress = convertLocationToAddress(requireContext() , it.longitude , it.latitude)
               binding.addressTextview.setText(teamAddress.address.toString())
           }
       }
    }

    fun createNewAddress(emailAddress : String){
        binding.createNewAddressProgressbar.isVisible = true
        var isDefault = binding.makeDefaultCheckbox.isChecked
        var username = binding.userNameTextview.text.toString()
        var phoneNumber = binding.phonenumberTextview.text.toString()
        var countryData = "Ethiopia"
        var city = binding.cityActv.text.toString()
        var addressText = binding.addressTextview.text.toString()

        selectedAddress?.let { addr ->
            var addressInfo = addr.apply {
                name = username
                address = addressText
                email = emailAddress
                phone = phoneNumber
                default = isDefault
                country = countryData
            }

            if(isExactaddress) addressInfo.city = city
            else addressInfo.nearLocation = city

            orderViewmodel.addNewShippingAddress(addressInfo).observe(viewLifecycleOwner){
                binding.createNewAddressProgressbar.isVisible = false
                orderInfo?.let { it1 -> (requireActivity() as MainActivity).movetoOrderFragment(it1 , it , maxDeliveryDay = maxDeliveryDay) }


//                if(isExactaddress) orderInfo?.let { it1 -> (requireActivity() as MainActivity).movetoOrderFragment(it1 , it) }
//                else{
//                    (requireActivity()).showDialog("Successful" , "Your order with this address will be delivered to our store located in $city and you can pick it up from our store.\n" +
//                            "We will contact you when the order reached in our store" ,
//                        getString(R.string.ok) , showNegative = false){
//                        orderInfo?.let { it1 -> (requireActivity() as MainActivity).movetoOrderFragment(it1 , it) }
//                    }
//                }

            }
        }
    }


    fun controlCitySelection(destinations : MutableList<String>){
        binding.createNewAddressProgressbar.isVisible = false
        var actvAdapter = ArrayAdapter(requireContext() , android.R.layout.simple_list_item_1 , destinations)
        binding.cityActv.setAdapter(actvAdapter)
        binding.cityActv.setText(actvAdapter.getItem(0).toString())

        binding.cityActv.setOnItemClickListener { adapterView, view, i, l ->
            if(!eligableCity.isNullOrEmpty()){
                var selectedItem = actvAdapter.getItem(i)
                var result = destinations.contains(selectedItem)
                if(result) binding.createAddressBtn.isEnabled = true
                else Toast.makeText(requireContext() , getString(R.string.not_deliver_to_address) , Toast.LENGTH_LONG).show()

//                if(selectedItem== getString(R.string.cant_find_my_location)){
//                    binding.createAddressBtn.isEnabled = false
//                    showDialogforNearLocationSelection()
//                }
//                else{
//                    var result = destinations.contains(selectedItem)
//                    if(result) binding.createAddressBtn.isEnabled = true
//                    else Toast.makeText(requireContext() , getString(R.string.not_deliver_to_address) , Toast.LENGTH_LONG).show()
//                }
            }
        }
    }



    fun showDialogforNearLocationSelection(){
        requireActivity().showCustomDialog(getString(R.string.sorry) ,
            getString(R.string.pick_from_near_location_message),
            getString(R.string.select_near_city),
            getString(R.string.yes),
            getString(R.string.no_thanks),
            negetiveAction = {
                binding.createAddressBtn.isVisible = false
                findNavController().navigateUp()
            }
        ){
            isExactaddress = false
            eligableCity.remove(getString(R.string.cant_find_my_location))
            binding.cityActv.setText("")
            controlCitySelection(eligableCity)
        }
    }

    fun checkfields() : Boolean{
        return (!binding.userNameTextview.text.isNullOrBlank() && !binding.phonenumberTextview.text.isNullOrBlank()
                && !binding.cityActv.text.isNullOrBlank() && !binding.addressTextview.text.isNullOrBlank())
    }

    fun NestedScrollView.smoothScrollTo(view: View) {
        var distance = view.top
        var viewParent = view.parent
        //traverses 10 times
        for (i in 0..9) {
            if ((viewParent as View) === this) break
            distance += (viewParent as View).top
            viewParent = viewParent.getParent()
        }
        smoothScrollTo(0, distance)
    }


}