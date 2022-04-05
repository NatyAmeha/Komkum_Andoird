package com.komkum.komkum.ui.store.team

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.facebook.login.LoginManager
import com.skydoves.balloon.showAlignTop
import com.komkum.komkum.MainActivity
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.databinding.FragmentCreateTeamBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class CreateTeamFragment : Fragment() {
    lateinit var binding : FragmentCreateTeamBinding
    val teamViewmodel : TeamViewModel by viewModels()
    var teamSize : Int? = 10
    var teamDuration : Int = 2
    var productId : List<String>? = null
    var teamType : Int? = Team.SINGLE_PRODUCT_TEAM

    var showAsMainError = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getStringArrayList("PRODUCT_ID")
            teamSize = it.getInt("TEAM_SIZE")
            teamDuration = it.getInt("TEAM_DURATION")
            teamType = it.getInt("TEAM_TYPE")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentCreateTeamBinding.inflate(inflater)
        return  binding.root
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pref = PreferenceHelper.getInstance(requireContext())
        if( teamType == Team.MULTI_PRODUCT_TEAM){
            configureActionBar(binding.toolbar , getString(R.string.create_package))
            binding.textInputLayout9.hint = getString(R.string.package_name)
            binding.createTeamButton.text = getString(R.string.create_package)
            binding.createTeamButton.showAlignTop(requireActivity()
                .getBalloon(getString(R.string.package_feature_description) ,
                    "CREATE_PACKAGE" , 0.5f, viewLifecycleOwner))
        }
        else{
            configureActionBar(binding.toolbar , getString(R.string.create_team))
            binding.textInputLayout9.hint = getString(R.string.team_name)
            binding.createTeamButton.text = getString(R.string.create_team)
        }


        binding.teamSizeTextview.text = "$teamSize ${getString(R.string.team_size_description)}"
        binding.teamNameTextview.doOnTextChanged { text, start, before, count ->
            binding.createTeamButton.isEnabled = !text.isNullOrBlank()
        }

        requireActivity().requestPermission(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) , 999)
        requireActivity().handlePermissionStatus(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
            getString(R.string.team_location_permission) , 999){
            handleTeamCreation()
        }

        teamViewmodel.getError().observe(viewLifecycleOwner){}
        teamViewmodel.error.observe(viewLifecycleOwner){
            binding.teamCreateProgressbar.isVisible = false
            it?.handleError(requireContext() , {teamViewmodel.removeOldErrors()}){
                (requireActivity() as MainActivity).showErrorSnacbar("${getString(R.string.error_message)}.\n${it}" , "Dismiss Error"){}
            }
        }

        if(!teamViewmodel.isAuthenticated()){
            pref[AccountState.IS_REDIRECTION] = true
            LoginManager.getInstance().logOut()
            var intent = Intent(context , OnboardingActivity::class.java )
            return  requireContext().startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            999 -> {
                requireActivity().handlePermissionStatus(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
                    getString(R.string.team_location_permission) , 999){
                    handleTeamCreation()
                }
            }
        }
    }

    fun handleTeamCreation(){
        showAsMainError = false
        var pref = PreferenceHelper.getInstance(requireContext())
        getLocation(requireContext()){
            var teamAddress = convertLocationToAddress(requireContext() , it.longitude , it.latitude)
            binding.teamCityEdittext.setText(teamAddress.address.toString())

            var currentDate = Calendar.getInstance()
            currentDate.time = Date()
            currentDate.add(Calendar.DATE , teamDuration)
            var teamEndDate = if( teamType == Team.MULTI_PRODUCT_TEAM) null else currentDate.time


            binding.createTeamButton.setOnClickListener {
                var isTeamActive = teamType != Team.MULTI_PRODUCT_TEAM
                var userId = pref.get(AccountState.USER_ID , "")
                var username = pref.get(AccountState.USERNAME , "")

                binding.teamCreateProgressbar.isVisible = true
                if(userId.isNotBlank()){
                    productId?.let { prIds ->
                        var teamName = binding.teamNameTextview.text.toString()
                        var teamCity = binding.teamCityEdittext.text.toString()
                        var teamDescription = binding.teamDescrTextview.text.toString()
                        var teamInfo = Team(name = teamName , desc = teamDescription , creator = userId ,
                            creatorName = username, city = teamCity ,  location = teamAddress.location, type = teamType ,
                            products = prIds , active = isTeamActive , teamSize = teamSize , endDate = teamEndDate)

                        if(prIds.isNotEmpty()){
                            teamViewmodel.createTeam(teamInfo).observe(viewLifecycleOwner){
                                it?.let{
                                    binding.teamCreateProgressbar.isVisible = false
                                    if(teamType == Team.MULTI_PRODUCT_TEAM) (requireActivity() as MainActivity).movetoPackageTeamDetails(it , backstackId = R.id.productFragment)
                                    else (requireActivity() as MainActivity).movetoTeamDetails(it , backstackId = R.id.productFragment)
                                }
                            }
                        }
                    }
                }
                else{
                    pref[AccountState.IS_REDIRECTION] = true
                    LoginManager.getInstance().logOut()
                    var intent = Intent(context , OnboardingActivity::class.java)
                    return@setOnClickListener requireContext().startActivity(intent)
                }
            }

        }
    }

}