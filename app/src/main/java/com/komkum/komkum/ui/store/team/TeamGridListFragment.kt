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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentTeamGridListBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.adaper.TeamAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamGridListFragment : Fragment() , IRecyclerViewInteractionListener<Team<Product>> {
    lateinit var binding : FragmentTeamGridListBinding
    val teamViewModel : TeamViewModel by viewModels()
    val mainactivityviewmodel : MainActivityViewmodel by activityViewModels()

    var loadType : Int? = null
    var teamsFromPrevPage : List<Team<Product>>? = null

    var address : Address? = null

    var fromReminder =  false

    companion object{
        const val LOAD_TRENDING_PACKAGES = 0
        const val  LOAD_EXPIRE_SOON = 1
        const val LOAD_USER_TEAMS = 2
        const val LOAD_TEAM_FROM_PREV_PAGE = 3
    }

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                loadType = it.getInt("LOAD_TYPE")
                fromReminder = it.getBoolean("FROM_REMINDER")
                teamsFromPrevPage = it.getParcelableArrayList("TEAM_LIST")
            }

            var pref = PreferenceHelper.getInstance(requireContext())
            var defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
            var defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)
            if(defaultLatitudeValue != 0f || defaultLongitudeValue != 0f){
                address = convertLocationToAddress(requireContext(), defaultLongitudeValue.toDouble(), defaultLatitudeValue.toDouble())
                when(loadType){
                    LOAD_TRENDING_PACKAGES -> teamViewModel.getTrendingPackages(address?.location?.coordinates?.get(0), address?.location?.coordinates?.get(1))
                    LOAD_EXPIRE_SOON -> teamViewModel.getProductsExpireSoon(address?.location?.coordinates?.get(0), address?.location?.coordinates?.get(1))
                    LOAD_USER_TEAMS -> teamViewModel.getUserTeams()
                }
            }
            else{
                // use current location
                if (requireActivity().isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    requireActivity().isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
                    getLocation(requireContext()) {
                        address = convertLocationToAddress(requireContext(), it.longitude, it.latitude)
                        when(loadType){
                            LOAD_TRENDING_PACKAGES -> teamViewModel.getTrendingPackages(address?.location?.coordinates?.get(0), address?.location?.coordinates?.get(1))
                            LOAD_EXPIRE_SOON -> teamViewModel.getProductsExpireSoon(address?.location?.coordinates?.get(0), address?.location?.coordinates?.get(1))
                            LOAD_USER_TEAMS -> teamViewModel.getUserTeams()
                        }
                    }
                }
            }
        }
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
       binding = FragmentTeamGridListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var teamInfo = RecyclerViewHelper(interactionListener = this , listItemType = TeamAdapter.TEAM_WITH_PRODUCT)
        var nearTeamInfo =  RecyclerViewHelper(interactionListener = this , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2 , listItemType = TeamAdapter.TEAM_WITH_PRODUCT)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.teamInfo = teamInfo
        binding.nearTeamInfo = nearTeamInfo
        binding.teamViewmodel = teamViewModel

        teamViewModel.nearAndTrendingTeamList.observe(viewLifecycleOwner){
            binding.errorTextview.isVisible = false
            if(loadType == LOAD_USER_TEAMS){
                configureActionBar(binding.toolbar , getString(R.string.your_teams))
                binding.yourActiveTeamRecyclerview.isVisible = !it.userTeams.isNullOrEmpty()
//                binding.yourActiveTeamTextview.isVisible = !it.userTeams.isNullOrEmpty()
                binding.yourCollectionsRecyclerview.isVisible = !it.userCollections.isNullOrEmpty()
                binding.yourCollectionsTextview.isVisible = !it.userCollections.isNullOrEmpty()

                if(it.userTeams.isNullOrEmpty() && it.userCollections.isNullOrEmpty()){
                    binding.errorTextview.text = getString(R.string.no_teams_found)
                    binding.errorTextview.isVisible = true
                }
            }
            else {
                configureActionBar(binding.toolbar , getString(R.string.browse_team))
                if(it.nearTeamList.isNullOrEmpty()){
                    binding.errorTextview.text = getString(R.string.no_teams_found)
                    binding.errorTextview.isVisible = true
                }
            }

            if(fromReminder){
                mainactivityviewmodel.purchasedFrom = FcmService.F_EVENT_PARAM_VALUE_PURCHASED_FROM_REMINDER
            }
        }

        teamViewModel.getError().observe(viewLifecycleOwner){}
        teamViewModel.error.observe(viewLifecycleOwner){
            binding.teamListProgressbar.isVisible = false
            it.handleError(requireContext()){
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
               if(teamViewModel.nearAndTrendingTeamList.value == null) binding.errorTextview.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    override fun onItemClick(data: Team<Product>, position: Int, option: Int?) {
        if(data.type == Team.MULTI_PRODUCT_TEAM && !data.active)
            (requireActivity() as MainActivity).movetoPackageTeamDetails(data._id!! , backstackId = R.id.teamGridListFragment)
        else if(data.type == Team.TRIVIA_TEAM) (requireActivity() as MainActivity).moveToGameTeamDetails(data.ad!!._id , backstackId = R.id.teamGridListFragment)
        else (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , backstackId = R.id.teamGridListFragment)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}