package com.komkum.komkum.ui.store.team

import android.Manifest
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
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.TeamListFragmentBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.adaper.TeamAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RadioSpan
import dagger.hilt.android.AndroidEntryPoint
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class TeamListFragment : Fragment() , IRecyclerViewInteractionListener<Team<Product>> {

    lateinit var binding : TeamListFragmentBinding
    val teamViewModel: TeamViewModel by viewModels()

    var loadType : Int? = -1
    var productMetadata : ProductMetadata<Product,String>? = null
    var productId : String? = null
    var canCreateDefaultTeam = false

    var productTeamSize : Int? = null
    var teamDuration : Int? = null
    var productImage : String? = null

    var timer : Timer? = null

    companion object{
        const val LOAD_ACTIVE_TEAMS_FOR_PRODUCT  = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            productMetadata = it.getParcelable("PRODUCT_INFO")
            loadType = it.getInt("LOAD_TYPE")
        }
//        requireActivity().requestPermission(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION),999)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        binding = TeamListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.teams_in_your_area))
        productId = productMetadata?.product?._id
        productTeamSize = productMetadata!!.teamSize!!
        teamDuration = productMetadata!!.teamDuration!!
        productImage = productMetadata?.product?.gallery?.firstOrNull {it.type == "image"}?.path

        var teamInfo = RecyclerViewHelper(interactionListener = this , listItemType = TeamAdapter.MINI_TEAM_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_VERTICAL)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.teamViewmodel = teamViewModel
        binding.teamInfo = teamInfo


        when(loadType){
            LOAD_ACTIVE_TEAMS_FOR_PRODUCT ->{
                var pref = PreferenceHelper.getInstance(requireContext())
                var defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
                var defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)
                if(defaultLatitudeValue != 0f || defaultLongitudeValue != 0f){
                    productId?.let { teamViewModel.getActiveTeams(it, defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble()) }
                }
                else{
                    // use current location to get team details
                    requireActivity().requestAndAccessLocationBeta(onFailure = {
                        if(it == false){
                            (requireActivity() as MainActivity).showDialog(getString(R.string.error) , getString(R.string.location_permission_info) ,
                                getString(R.string.grant_permission) , showNegative = true , owner = viewLifecycleOwner){
                                requireActivity().gotoPermissionSetting()
                            }
                        }
                        else {
                            (requireActivity() as MainActivity).showDialog("Error",
                                getString(R.string.location_permission_info),
                                getString(R.string.give_permission),
                                showNegative = true,
                                negetiveAction = {
                                    binding.teamListLoadingProgressbar.isVisible = false
                                    binding.errorTextview.text =
                                        getString(R.string.no_team_in_your_area)
                                    binding.errorTextview.isVisible = true

                                }) {
                                requireActivity().gotoPermissionSetting()
                            }
                        }
                    }) {location -> productId?.let { teamViewModel.getActiveTeams(it , location.longitude , location.latitude)} }

                }
            }
        }

        teamViewModel.teamListWithProductInfo.observe(viewLifecycleOwner){
            binding.errorTextview.isVisible = false
            if(it.isEmpty()){
                createAndHandleDefaultTEam()
//                binding.errorTextview.text = getString(R.string.no_team_in_your_area)
//                binding.errorTextview.isVisible = true
            }
            else if(it.find { it.type  == Team.DEFAULT_TEAM} == null){
                createAndHandleDefaultTEam()
                binding.otherTeamsTextview.isVisible = true

            }
            else{
                binding.defaultTeam.root.isVisible = false
            }
        }

//        binding.createTeamBtn.setOnClickListener {
//            productId?.let {
//                (requireActivity() as MainActivity).moveToCreateTEam(listOf(it) ,productTeamSize ?: 15 , teamDuration )
//            }
//        }

        teamViewModel.getError().observe(viewLifecycleOwner){}
        teamViewModel.error.observe(viewLifecycleOwner){
            binding.teamListLoadingProgressbar.isVisible = false
            binding.findingTeamIndicatorTextview.isVisible = false
            it.handleError(requireContext()){
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
               if(teamViewModel.teamListWithProductInfo.value.isNullOrEmpty()) binding.errorTextview.isVisible = true
            }
        }

    }



    fun createAndHandleDefaultTEam() {
        binding.teamListLoadingProgressbar.isVisible = true
        binding.defaultTeam.teamTitleTextview.text = "Default team"
        binding.defaultTeam.teamProductImageview.loadImage(listOf(productImage ?: R.drawable.product_placeholder.toString()))
        binding.defaultTeam.productNumberAvatarview.displayAvatar(null, "1"  , 11f)
        var joinedMember = 1
        var teamMemberTExt = "$joinedMember joined span ${(productTeamSize ?: 6).minus(joinedMember)} remaining"
        var spanner = Spanner(teamMemberTExt).span("span", Spans.custom {
                RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin))
            })
        binding.defaultTeam.teamSubtitleTextview.text = spanner
        binding.defaultTeam.root.isVisible = true

        requireActivity().handlePermissionStatus(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
            getString(R.string.team_location_permission) , 999){
            binding.teamListLoadingProgressbar.isVisible = false

            var currentDate = Calendar.getInstance()
            currentDate.time = Date()
            currentDate.add(Calendar.DATE , teamDuration ?: 7)

            var location = getLocation(requireContext()){
                var latlong =  GeoSpacial(type = "Point" , coordinates = listOf(it.longitude , it.latitude))

                var teamCreatedBySystem = Team(name = "${productMetadata?.product?.title ?: "Default"} team" ,
                    creatorName = "Admin",   location = latlong, type = Team.DEFAULT_TEAM ,
                    products = listOf(productId!!) , active = true , teamSize = productTeamSize ?: 15 , endDate = currentDate.time)


                teamCreatedBySystem.endDate?.let {date ->
                    var calendar = Calendar.getInstance()
                    calendar.time = date
                    var timerTask =  fixedRateTimer("timer" , false , period = 1000){
                        this@TeamListFragment.activity?.runOnUiThread {
                            binding.defaultTeam.teamSubtitle1Textview.text = "Expired in ${calendar.time.getRemainingDay(requireContext() , includeSec = true) ?: "0d 0m 0sec"}"
                        }
                    }
                    timer = timerTask
                }

                binding.defaultTeam.root.setOnClickListener {
                    binding.teamListLoadingProgressbar.isVisible = true
                    if(productId != null){
                        teamViewModel.createTeam(teamCreatedBySystem).observe(viewLifecycleOwner){
                            binding.teamListLoadingProgressbar.isVisible = false
                            it?.let{
                                (requireActivity() as MainActivity).movetoTeamDetails(it , backstackId = R.id.productFragment)
                            }
                        }
                    }
                    else binding.teamListLoadingProgressbar.isVisible = false
                }


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
        when(requestCode){
            999 -> {
                requireActivity().handlePermissionStatus(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
                    getString(R.string.location_permission_info) , 999,
                    onFailure = {requireActivity().gotoPermissionSetting()}){
                    getLocation(requireContext()){ location ->
                        var address = convertLocationToAddress(requireContext() , location.longitude , location.latitude)
                        productId?.let { teamViewModel.getActiveTeams(it , location.longitude , location.latitude)}
                    }
                }
            }
        }
    }

    override fun onPause() {
        timer?.cancel()

        super.onPause()
    }


    override fun onItemClick(data: Team<Product>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , backstackId = R.id.teamListFragment)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}