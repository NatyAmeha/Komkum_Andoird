package com.komkum.komkum.ui.store.team

import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.data.model.TeamMember
import com.komkum.komkum.databinding.FragmentTeamBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.skydoves.balloon.showAlignBottom
import dagger.hilt.android.AndroidEntryPoint
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt


@AndroidEntryPoint
class TeamFragment : Fragment(), IRecyclerViewInteractionListener<Product> {
    lateinit var binding : FragmentTeamBinding
    lateinit var timer : Timer

    val teamViewmodel : TeamViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var teamId : String? = null
    var loadType : Int? = null
    var inviterId : String? = null

    var teamInfo : Team<Product>? = null
    var totalCommission = 0

    var backstackId : Int? = null

    var packageId : String? = null


    var showAsMainError = true
    var isUseraMember = false

    var discountedPercent = 15
    var discountedAmount = 0

    var canAccessLocation = true
    var fromReminder =  false

    var teamExpired = false    // boolean flag to control other dialog from showing if team is expired
    var dialog : MaterialDialog? = null

    var forceCancelDefaultLocationDialog = true

    var defaultLatitudeValue = 0f
    var defaultLongitudeValue = 0f

    var centerLatitude = 9.0101502
    var centerLongtiude = 38.7591836


    companion object{
        const val LOAD_BY_TEAM_ID = 0
        const val LOAD_PACKAGE_TEAM = 1

    }

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                teamId = it.getString("TEAM_ID")
                inviterId = it.getString("INVITER_ID")
                backstackId = it.getInt("BACKSTACK_ID")
                loadType = it.getInt("LOAD_TYPE")

                var pref = PreferenceHelper.getInstance(requireContext())
                defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
                defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)

                if(inviterId != null){
                    teamId?.let { teamViewmodel.getTeamDetails(it) }
                }
                else if(defaultLatitudeValue != 0f || defaultLongitudeValue != 0f){
                    when(loadType){
                        LOAD_PACKAGE_TEAM -> teamId?.let {id ->
                            packageId = id
                            teamViewmodel.getTeamForPackage(id , defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble())

//                        else{
//                            requireActivity().requestAndAccessLocationBeta(){
//                                teamViewmodel.getTeamForPackage(id , it.longitude , it.latitude)
//                            }
//                        }
                        }
                        else  -> teamId?.let { teamViewmodel.getTeamDetails(it) }
                    }
                }


                fromReminder = it.getBoolean("FROM_REMINDER")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
       binding = FragmentTeamBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        var pref = PreferenceHelper.getInstance(requireContext())
        defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
        defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)
        var canReloadTeamInfo = pref.get(PreferenceHelper.RELOAD_TEAM_PAGE_AFTER_DEFAULT_ADDRESS_CHANGED , false)
        if(canReloadTeamInfo){
            pref[PreferenceHelper.RELOAD_TEAM_PAGE_AFTER_DEFAULT_ADDRESS_CHANGED] = false
            if(loadType == LOAD_PACKAGE_TEAM && defaultLatitudeValue != 0f && defaultLongitudeValue != 0f){
                packageId = teamId
                packageId?.let {
                    binding.teamLoadingProgressbar.isVisible = true
                    teamViewmodel.getTeamForPackage(it, defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble()) }
            }
            else  teamId?.let { teamViewmodel.getTeamDetails(it) }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        pref[AccountState.IS_REDIRECTION] = true
        defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
        defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)


        configureActionBar(binding.toolbar , getString(R.string.team_details))
        var teamMemberListner = object : IRecyclerViewInteractionListener<TeamMember> {
            override fun onItemClick(data: TeamMember, position: Int, option: Int?) {
                teamInfo?.let {
                    var invitedMembersWhoPlacedOrders = it.members?.filter { data.reward?.contains(it.user) == true && it.ordered == true }
                    var info = listOf(
                        "brought ${data.reward?.size ?: 0} people to this team",
                        "${invitedMembersWhoPlacedOrders?.size ?: 0} people bought the product"
                    )
                    MaterialDialog(requireContext()).show {
                        title(text = data.username ?: "")
                        listItems(items = info)
                        positiveButton(text = "OK")
                    }
                }
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }
        var productListInfo = RecyclerViewHelper(interactionListener = this , listItemType = ProductAdapter.VERTICAL_PRODUCT_LIST_ITEM )
        var teamMemberInfo = RecyclerViewHelper(type = "", interactionListener = teamMemberListner)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.productListInfo = productListInfo
        binding.teamMemberInfo = teamMemberInfo

        if(fromReminder){
            mainActivityViewmodel.purchasedFrom = FcmService.F_EVENT_PARAM_VALUE_PURCHASED_FROM_REMINDER
        }


        // show dialog to set home address
        var homeAddressOptions = listOf(getString(R.string.use_current_address_for_default_location), getString( R.string.find_my_location_for_default_location))

        var Mdialog = MaterialDialog(requireContext())


        // show language dialog is user redirected using deeplink
        var languageDialog = requireActivity().showLanguageSelelectDialog(false , 0){
            if(inviterId != null){
                binding.teamLoadingProgressbar.isVisible = true
                teamId?.let { teamViewmodel.getTeamDetails(it) }
            }
            else{
                // check if location service is enabled
                var isLocationTurnedOn = requireActivity().isLocationServiceTurnedOn()
                if(!isLocationTurnedOn){
                    (requireActivity() as MainActivity).showDialog(getString(R.string.error) ,
                        getString(R.string.app_need_location_feature) ,
                        getString(R.string.turn_on_location),
                        autoDismiss = false){
                        var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                        this.displayAddressSelectionDialog(Mdialog)
                    }
                }
                else{
                   this.displayAddressSelectionDialog(Mdialog)
                }
            }
        }

        if((defaultLatitudeValue == 0f || defaultLongitudeValue == 0f) && inviterId == null && languageDialog == null){
            var isLocationTurnedOn = requireActivity().isLocationServiceTurnedOn()
            if(!isLocationTurnedOn){
                (requireActivity() as MainActivity).showDialog(getString(R.string.error) ,
                    getString(R.string.app_need_location_feature) ,
                    getString(R.string.turn_on_location),
                    autoDismiss = false){
                    var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                    this.displayAddressSelectionDialog(Mdialog)
                }
            }
            else{
                this.displayAddressSelectionDialog(Mdialog)
            }
        }


        teamViewmodel.teamDetails.observe(viewLifecycleOwner){
            binding.teamLoadingProgressbar.isVisible = false
            binding.container.smoothScrollTo(0 , 0)
            binding.errorTextview.isVisible = false
            teamId = it._id
            teamInfo = it


            defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
            defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)

            binding.additionalQty = it.additionalQty


            var totalProductPrice = teamInfo?.products?.sumOf { product -> (product.dscPrice ?: 0.0).times(product.minQty) }

            binding.teamViewmodel = teamViewmodel
            binding.totalProductPrice = totalProductPrice?.roundToInt() ?: 0


            binding.tJoinBtn.isVisible = true


            // control team distance and disable team functionalities if distance is larger than 10KM
//                requireActivity().requestAndAccessLocationBeta(onFailure = {
//                    canAccessLocation = false
//                    dialog = (requireActivity() as MainActivity).showDialog(getString(R.string.error) , getString(R.string.location_permission_info) ,
//                        getString(R.string.grant_permission) , showNegative = true , owner = viewLifecycleOwner){
//                        requireActivity().gotoPermissionSetting()
//                    }
//                }) {
//
//                    if(it != null){
//                        var teamLongtiude = teamInfo?.location?.coordinates?.get(0)
//                        var teamLatitude = teamInfo?.location?.coordinates?.get(1)
//                        if(teamLatitude != null && teamLongtiude != null){
//                            var distanceInKm = getDistanceBetweenLocation(it , teamLatitude , teamLongtiude).div(1000)
//                            // team radius is 10 km
//                            if((loadType == LOAD_PACKAGE_TEAM &&  distanceInKm > 10) || distanceInKm > 50){
//                                binding.tJoinBtn.isVisible = false
////                                binding.createAnotherTeamBtn.isVisible = true
//                                binding.createAnotherTeamBtn.isVisible = false
//                                binding.claimCommissionCardview.isVisible = false
//                                dialog = requireActivity().showDialog(getString(R.string.sorry) , getString(R.string.team_location_restriction_message) , getString(R.string.ok)  , owner = viewLifecycleOwner){
////                                    teamInfo?.products?.map { it._id!! }?.let {
////                                        (requireActivity() as MainActivity).moveToCreateTEam(it , teamInfo?.teamSize ?: 15 , teamInfo?.duration , teamInfo?.type)
////                                    }
////                                    teamId?.let { tId ->
////                                        (requireActivity() as MainActivity).movetoTeamDetails(tId , inviterId , backstackId, loadType)
////                                    }
//                                }
//                            }
//                        }
//                        else Toast.makeText(requireContext() ,"Unable to get the distance", Toast.LENGTH_LONG).show()
//                    }
//                }
//
            var isUserJoined = teamInfo!!.members!!.find{member -> member.user == userId && member.joined == true}
            isUseraMember = isUserJoined != null

            it.location?.let {loc ->
                var address = convertLocationToAddress(requireContext() , loc.coordinates!![0] , loc.coordinates!![1])
                if(address.address == null || address.address?.contains("Addis Ababa" , true) == false ||
                    address.address?.contains(" አዲስ " , true) == false){
                    // geofence using distance from the center
                    var centerLocation = Location("Reference Location").apply {
                        latitude = centerLatitude
                        longitude = centerLongtiude
                    }
                    var distanceInKm = getDistanceBetweenLocation(centerLocation ,  defaultLatitudeValue.toDouble() , defaultLongitudeValue.toDouble()).div(1000)
                    if(distanceInKm > 40){
                        binding.tJoinBtn.isVisible = false
                        binding.continueToBuyBtn.isVisible = false
                        dialog = requireActivity().showDialog(getString(R.string.sorry) , getString(R.string.team_location_restriction_message) , getString(R.string.ok)
                            , owner = viewLifecycleOwner , autoDismiss = false){
                            findNavController().navigateUp()
                        }
                    }
                    else{
                        displayUI(it , pref)
                    }
                }

                else {
                    displayUI(it , pref)
                }


                binding.tAddressTextview.text =  address.address
            }
        }




        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if(::timer.isInitialized) timer.cancel()

            findNavController().navigateUp()
//            if(backstackId!= null && backstackId == 0) findNavController().navigate(R.id.storeHomepageFragment)
//            else if(backstackId != null) findNavController().popBackStack(backstackId!! , false)
//            else{
//                if(findNavController().isFragmentInBackStack(R.id.storeHomepageFragment)){
//                    findNavController().popBackStack(R.id.storeHomepageFragment , false)
//                }
//                else findNavController().navigate(R.id.storeHomepageFragment)
//            }
        }

        teamViewmodel.getError().observe(viewLifecycleOwner){}
        teamViewmodel.error.observe(viewLifecycleOwner){
            binding.teamLoadingProgressbar.isVisible = false
            it?.handleError(requireContext() , {teamViewmodel.removeOldErrors()} ,
                signupSource = mainActivityViewmodel.signUpSource){
                if(showAsMainError && teamViewmodel.teamDetails.value == null)
                    binding.errorTextview.isVisible = true
                else (requireActivity() as MainActivity)
                    .showErrorSnacbar(getString(R.string.error_message) , "Dismiss"){}
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onPause() {
        dialog?.dismiss()
        super.onPause()
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
        when(requestCode){
            999 -> {
                if(grantResults.isNotEmpty()){
                    getLocation(requireContext()){
                        if(teamInfo != null){
                            var teamLat = teamInfo?.location?.coordinates?.get(0)
                            var teamLong = teamInfo?.location?.coordinates?.get(1)
                            if(teamLat != null && teamLong != null){
                                var distance = getDistanceBetweenLocation(it , teamLat , teamLong)
                            }
                            else Toast.makeText(requireContext() ,"Unable to get the distance", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }


    override fun onDestroyView()  {
        if(::timer.isInitialized) timer.cancel()
        super.onDestroyView()
    }

    fun displayAddressSelectionDialog(dialog : MaterialDialog){
        var pref = PreferenceHelper.getInstance(requireContext())
        var homeAddressOptions = listOf(getString(R.string.use_current_address_for_default_location), getString( R.string.find_my_location_for_default_location))

        dialog
            .title(text = getString(R.string.add_home_address))
            .message(text = getString(R.string.default_location_selection_description))
            .listItemsSingleChoice(items = homeAddressOptions){ dialog, index, text ->
                when(index){
                    0 -> {
                        forceCancelDefaultLocationDialog = false
                        binding.teamLoadingProgressbar.isVisible = true
                        requireActivity().requestAndAccessLocationBeta(onFailure = {
                            canAccessLocation = false
                            if(it == false){
                                (requireActivity() as MainActivity).showDialog(getString(R.string.error) , getString(R.string.location_permission_info) ,
                                    getString(R.string.grant_permission) , showNegative = true , owner = viewLifecycleOwner){
                                    requireActivity().gotoPermissionSetting()
                                }
                            }else{
                                (requireActivity() as MainActivity).showDialog(
                                    getString(R.string.error),
                                    getString(R.string.location_permission_info),
                                    getString(R.string.grant_permission),
                                    showNegative = true,
                                    owner = viewLifecycleOwner
                                ) {
                                    requireActivity().gotoPermissionSetting()
                                }

                            }
                        }){ location ->
                            pref[PreferenceHelper.DEFAULT_LONGITUDE_VALUE] = location.longitude.toFloat()
                            pref[PreferenceHelper.DEFAULT_LATITUDE_VALUE] = location.latitude.toFloat()
                            pref[PreferenceHelper.IS_CURRENT_LOCATION_SELECTED] = true

                            if(loadType == LOAD_PACKAGE_TEAM){
                                packageId = teamId
                                packageId?.let {
                                    teamViewmodel.getTeamForPackage(it, location.longitude , location.latitude) }
                            }
                            else  teamId?.let { teamViewmodel.getTeamDetails(it) }

                        }
                    }
                    1 -> {
                        forceCancelDefaultLocationDialog = false
                        findNavController().navigate(R.id.findMyAddressFragment)
                    }

                }
            }
            .cancelOnTouchOutside(false)
            .onDismiss {
                it.dismiss()
                if(forceCancelDefaultLocationDialog) findNavController().navigateUp()
            }
            .show()
    }

    fun displayUI(teamInfo : Team<Product> , pref : SharedPreferences){
        configureActionBar(binding.toolbar , teamInfo.name)
        var userId = pref.get(AccountState.USER_ID , "")
        var validMembers = teamInfo.members!!.filter { member -> member.joined == true }
        binding.validMembers = validMembers.take(15)

        if(!teamInfo.products.isNullOrEmpty()){
            var stdProductPrices = teamInfo.products!!
                .sumOf { product -> product.stdPrice!!.times(product.minQty.plus(teamInfo.additionalQty ?: 0)) }

            var discountPRices = teamInfo.products!!
                .sumOf { product -> product.dscPrice!!.times(product.minQty.plus(teamInfo.additionalQty ?: 0)) }
            discountedPercent = ((1 - (discountPRices / stdProductPrices))*100).roundToInt()
            discountedAmount = stdProductPrices.roundToInt() - discountPRices.roundToInt()

            var descSpannable = Spanner()
                .append("${getString(R.string.by_using_this_team)} ${getString(R.string.you_can_save)} ")
                .append("${getString(R.string.birr)} $discountedAmount" , Spans.bold())
                .append(" ${getString(R.string.from_your_order)}. ")
                .append(getString(R.string.with_out_this_team))
                .append(" ${getString(R.string.birr)} ${stdProductPrices.roundToInt()} " , Spans.bold())
                .append("${getString(R.string.link_phrase)} ${getString(R.string.you_only_pay)} ")
                .append(" ${getString(R.string.birr)} ${discountPRices.roundToInt()} " , Spans.bold())
                .append("${getString(R.string.only)}. ${getString(R.string.wait_for_team_formation)}")

            binding.tDescriptionTextview.text = descSpannable
            binding.totalProductPriceTextview.text = "${getString(R.string.birr)} ${discountPRices.roundToInt()}"
            binding.teamDiscountTextview.text = "${discountedPercent}% (${getString(R.string.birr)} ${discountedAmount}) ${getString(R.string.discount)}"

            var totalProductPrice = teamInfo.products?.sumOf { product -> (product.dscPrice ?: 0.0).times(product.minQty) }
            var commission = (totalProductPrice?.times(5))?.div(100)?.roundToInt().toString()
            binding.commissionHeaderTextview.text = "${getString(R.string.birr)} $commission ${getString(R.string.commission_per_user_invite)}"
        }

//        binding.teamMember3Imageview.text = "${validMembers.size}"
        var p = if(teamInfo.products!!.size > 1) "${teamInfo.products!![0].title} and other ${teamInfo.products!!.size} ${getString(R.string.products)}"
        else "${teamInfo.products!![0].title}"
        binding.tRequiredSizeTextview.text = "${teamInfo.teamSize} ${getString(R.string.members)}"
        binding.tJoinedNumberTextview.text = "${validMembers.size} ${getString(R.string.members)}"
        binding.productCountTextview.text = "${teamInfo.products?.size ?: 0} ${getString(R.string.products)}"



        //nudge user to invite other peoples
        var isUserJoined = teamInfo.members!!.find{member -> member.user == userId && member.joined == true}
        if((teamInfo.teamSize ?: 10 > teamInfo.members?.size ?: 0) && canAccessLocation && isUserJoined != null){
            var showShareDialog = pref.get(PreferenceHelper.SHOW_SHARE_DIALOG , true)
            if(showShareDialog && defaultLatitudeValue != 0f || defaultLongitudeValue != 0f){
                dialog  = requireActivity().showDialog(getString(R.string.you_joined_team) , getString(R.string.share_with_friends) ,
                    positiveButtonText = getString(R.string.share) , autoDismiss = false , owner = viewLifecycleOwner , checkBoxPrompt = true){
                    binding.teamLoadingProgressbar.isVisible = true
                    share(teamInfo , userId)
                }
            }
        }


//        teamInfo.location?.let {
//            var address = convertLocationToAddress(requireContext() , it.coordinates!![0] , it.coordinates!![1])
//            binding.tAddressTextview.text = teamInfo.city ?: address.address
//        }


        var currentDate = Calendar.getInstance().time


        if(isUserJoined != null){
            binding.tJoinBtn.isVisible = false
            binding.tShareBtn.isVisible = true
            binding.continueToBuyBtn.isVisible = true
        }
        else binding.tCoinNumberTextview.visibility = View.INVISIBLE

        if(teamInfo.teamSize ?: 10 <= validMembers.size){
            var membersWhoOrdered = teamInfo.members?.filter { it.ordered == true }
            if(membersWhoOrdered?.size ?: 0 > 0){
                binding.currentOrderCountTextview.text = "${membersWhoOrdered?.size} ${getString(R.string.members_already_ordered)}"
                binding.currentOrderCountTextview.isVisible = true
            }
        }

        if(teamInfo.endDate != null){
            if(currentDate > teamInfo.endDate){
                binding.tShareBtn.isVisible = false
                binding.tJoinBtn.isVisible = false
                binding.tCoinNumberTextview.isVisible = false
                binding.continueToBuyBtn.isVisible = false
                binding.tRemainingDayTextview.text = getString(R.string.team_expire_title)
                binding.claimCommissionCardview.isVisible = false
                binding.shareCardView.isVisible = false
                teamExpired = true
//                dialog = requireActivity().showDialog(getString(R.string.team_expire_title) , "${getString(R.string.team_expire_message)}" , "${getString(R.string.yes)}" ,
//                    negetiveAction = {findNavController().navigateUp()} , owner = viewLifecycleOwner ){
//                    teamInfo.products?.first()?._id?.let {
//                        (requireActivity() as MainActivity).movetoProductDetails(it)
//                    }
//                }
            }
            else{
                if(isUserJoined != null)
                    binding.tRemainingDayTextview.showAlignBottom(requireActivity().getBalloon(getString(R.string.start_ordering_before_expire) , arrowPos = 0.5f , prefName = "TEAM_EXPIRE_COUNT_DOWN" , showCount = 1 , lifeyCycleOwner = viewLifecycleOwner))
                else
                    binding.tRemainingDayTextview.showAlignBottom(requireActivity().getBalloon(getString(R.string.join_and_order_before_expire) , arrowPos = 0.5f, prefName = "TEAM_EXPIRE_COUNT_DOWN" , showCount = 1 , lifeyCycleOwner = viewLifecycleOwner ))


                var calendar = Calendar.getInstance()
                calendar.time = teamInfo.endDate
                timer =  fixedRateTimer("team_expire_timer" , false , period = 1000){
                    activity?.runOnUiThread {
                        binding.tRemainingDayTextview.text = calendar.time.getRemainingDay(requireContext() , includeSec = true)
                    }
                }
            }
        }
        else{
            binding.textView182.isVisible = false
            binding.tRemainingDayTextview.isVisible = false
        }

        binding.tCoinNumberTextview.isVisible = true
        binding.textView225.isVisible = true
        var userCommission = getUserCommission(teamInfo)
        binding.tCoinNumberTextview.text = "${getString(R.string.birr)} $userCommission"

        binding.commissionDescTextview.text = "${getString(R.string.congrats)}, ${getString(R.string.team_commission_desc_1)} ${getString(R.string.birr)} $userCommission ${getString(R.string.team_commission_desc_2)}"

        val cal = Calendar.getInstance()
        cal.time = teamInfo.startDate!!

        binding.tJoinBtn.setOnClickListener {
            showAsMainError = false
//            Toast.makeText(requireContext() , inviterId , Toast.LENGTH_SHORT).show()
            binding.teamLoadingProgressbar.isVisible = true
            teamViewmodel.joinTeam(teamInfo._id!! , inviterId).observe(viewLifecycleOwner){
                binding.teamLoadingProgressbar.isVisible = false
                if(it == true){
                    var teamType = if(loadType == LOAD_PACKAGE_TEAM) FcmService.F_EPV_PACKAGE
                    else FcmService.F_EPV_TEAM
                    mainActivityViewmodel.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP){
                        param(FcmService.F_EP_TEAM_TYPE , teamType)
                    }

                    if(teamInfo.type == Team.TRIVIA_TEAM){
                        (requireActivity() as MainActivity).moveToGameTeamDetails(teamInfo.ad!!._id!! , backstackId = R.id.teamFragment)
                    }
                    else{
                        teamId?.let { teamViewmodel.getTeamDetails(it) }
//                    (requireActivity() as MainActivity).movetoTeamDetails(teamInfo._id!! ,backstackId = backstackId)
                    }
                }
            }
        }

        binding.createAnotherTeamBtn.setOnClickListener {
            teamInfo.products?.map { it._id!! }?.let {
                (requireActivity() as MainActivity).moveToCreateTEam(it , teamInfo.teamSize ?: 15 , teamInfo.duration , teamInfo.type)
            }
        }


        binding.tShareBtn.setOnClickListener {
            if(userId.isNotBlank()){
                binding.teamLoadingProgressbar.isVisible = true
                share(teamInfo , userId)
            }
            else {
                 dialog = (requireActivity() as MainActivity)
                    .showDialog(getString(R.string.info) , getString(R.string.join_team_first) , isBottomSheet = true , owner = viewLifecycleOwner){}

            }
        }



        binding.claimCommissionBtn.setOnClickListener {
            binding.teamLoadingProgressbar.isVisible = true
            teamViewmodel.cliamTeamCommission(teamInfo._id!!).observe(viewLifecycleOwner){
                binding.teamLoadingProgressbar.isVisible = false
                if(it != null && it > 0){
                    (requireActivity() as MainActivity).showSnacbar("$it ${getString(R.string.wallet_transfer_message)}" , "view"){}
                    (requireActivity() as MainActivity).movetoTeamDetails(teamInfo._id!! , backstackId = backstackId)
                }
            }
        }

        binding.continueToBuyBtn.setOnClickListener {
            if(validMembers.size>= teamInfo.teamSize!!){
                var productIds = teamInfo.products?.map { pro -> pro._id }?.joinToString(",")
                productIds?.let { it1 ->
                    var purchaseType = if(loadType == LOAD_PACKAGE_TEAM) Product.PACKAGE_PURCHASE
                        else if(teamInfo.products!!.size > 1) Product.MULTI_PRODUCT_TEAM_PURCHASE
                    else Product.SINGLE_PRODUCT_TEAM_PURHASE
                    (requireActivity() as MainActivity).moveToProductCustomization(it1 , purchaseType , teamInfo._id , teamInfo.additionalQty)
                }
            }
            else  dialog = requireActivity().showDialog("Info" ,
                "${getString(R.string.team_member_invitation_message)} ${teamInfo.teamSize?.minus(validMembers.size)} ${getString(R.string.members)}.\n ${getString(R.string.commission_info)}",
            owner = viewLifecycleOwner , positiveButtonText = getString(R.string.share)){
                share(teamInfo, userId)
            }
        }


        binding.tDescriptionTextview.setOnClickListener {
            if(binding.tDescriptionTextview.maxLines == 3) binding.tDescriptionTextview.maxLines = 1000
            else binding.tDescriptionTextview.maxLines = 3
        }
    }


    private fun share(teamInfo: Team<Product>, userId: String) {
        binding.teamLoadingProgressbar.isVisible = true
            var invitationLink = "https://komkum.com/team/${teamInfo._id}/invitation?uid=$userId"
        var title = if(teamInfo.products!!.size > 1) "${teamInfo.products!![0].title} and other ${teamInfo.products!!.size -1} products"
        else teamInfo.products!![0].title

        var totalProductPrice = teamInfo.products?.sumOf { product -> (product.dscPrice ?: 0.0).times(product.minQty) }
        var commission = (totalProductPrice?.times(5))?.div(100)?.roundToInt().toString()

        var body = "በዚህ ቡድን ውስጥ ያሉ ምርቶችን በማይታመን ቅናሽ ይግዙ ፣ እንዲሁም እነዚህን ምርቶች እንዲገዙ ሌሎችን በመጋበዝ በእያንዳንዱ ግብዣ 5% ($commission ብር) ኮሚሽን ያግኙ።"

        this.createdynamicLink(invitationLink , "Komkum" ,
            "Get ${discountedPercent}% (${discountedAmount} birr) discount when you buy using this team.",
        image = teamInfo.products!!.firstOrNull()?.gallery?.firstOrNull{ it.type == "image" }?.path ?: "",
        onFailure = {
            binding.teamLoadingProgressbar.isVisible = false
            var linktoShare = "\n\nclick here $invitationLink"
            var linkDesc = "\nBuy $title with unbelievable discount and get 5% commission by inviting others to buy those products.\n"
            var data = body.plus(linkDesc).plus(linktoShare)
            requireActivity().showShareMenu("Buy with team" , data)
        }){dynamicLink ->
            binding.teamLoadingProgressbar.isVisible = false
            var linktoShare = "\n\nclick here $dynamicLink"
            var data = body.plus(linktoShare)
            requireActivity().showShareMenu("Buy with team" , data)
        }

        mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.TEAM_LINK_SHARE , null)
    }

    fun getUserCommission(teamInfo: Team<Product>) : Int{
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        var totalProductPrice = teamInfo.products?.sumOf { product -> (product.dscPrice ?: 0.0).times(product.minQty) }

        var commission = (totalProductPrice?.times(5))?.div(100)?.roundToInt()

        var user = teamInfo.members?.find { member -> member.user == userId }

        // find members invited by this user. they must place order
        var invitedUserWhoPlaceOrder = teamInfo.members?.filter { member -> user?.reward?.contains(member.user!!) == true && member.ordered == true }

        //if user != null and size of invited members who placed orders greater than 0, then user has a commission
        return if(user != null && !invitedUserWhoPlaceOrder.isNullOrEmpty()){
            totalCommission = commission?.times(invitedUserWhoPlaceOrder.size) ?: 0
            if(totalCommission > 0) binding.claimCommissionCardview.isVisible = true
            totalCommission
        } else 0
    }



    override fun onItemClick(data: Product, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoProductDetails(data._id!! , true)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}