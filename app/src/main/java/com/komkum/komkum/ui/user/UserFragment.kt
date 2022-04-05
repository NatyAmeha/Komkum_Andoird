package com.komkum.komkum.ui.user

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.UserFragmentBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.delete
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.komkum.komkum.MainActivity
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.isVisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFragment : Fragment() {

    lateinit var facebookcallbackManager : CallbackManager
    lateinit var binding : UserFragmentBinding
    lateinit var preference : SharedPreferences

    val userViewmodel: UserViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var user : UserWithSubscription? = null

    init {
        lifecycleScope.launchWhenCreated {
            userViewmodel.getUserData()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = UserFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar)
        preference = PreferenceHelper.getInstance(requireContext())
        var isSubscriptionVaid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()


        showUserInfo(requireContext())
        userViewmodel.userData.observe(viewLifecycleOwner){
            binding.progressBar7.isVisible = false
            user = it
            configureActionBar(binding.toolbar , it.username)
            var preference = PreferenceHelper.getInstance(requireContext())
            var fbResult =  preference.get(AccountState.ACCESS_FACEBOOK_FRIENDS , false)
            var isFbAuthenticated = AccessToken.getCurrentAccessToken()?.token !=null &&
                    AccessToken.getCurrentAccessToken()?.isExpired == false && !it.facebookId.isNullOrEmpty() && fbResult
            if(isFbAuthenticated){
//                binding.connectToFbBtn.text = "Connected to Facebook"
//                binding.connectToFbBtn.isEnabled = false
//                var preference = PreferenceHelper.getInstance(requireContext())
//                preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
            }
            else {
//                binding.connectToFbBtn.text = "Connect with friends"
//                binding.connectToFbBtn.isEnabled = true
            }


//            binding.connectToFbBtn.setOnClickListener {
//                var permissions = listOf("user_friends")
//
//                var loginManager = LoginManager.getInstance()
//                facebookcallbackManager = CallbackManager.Factory.create()
//                loginManager.registerCallback(facebookcallbackManager , object : FacebookCallback<LoginResult>{
//                    override fun onSuccess(result: LoginResult) {
//                        result?.let {
//                            preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
//                            requestUserFriendFromFacebookAndSaveToDb(it.accessToken)
//                        }
//                    }
//
//                    override fun onCancel() {
//                        Toast.makeText(requireContext() , "Authentication Cancelled" , Toast.LENGTH_LONG).show()
//                    }
//
//                    override fun onError(error: FacebookException) {
//                        Toast.makeText(requireContext() , "Authentication Error" , Toast.LENGTH_LONG).show()
//                    }
//                })
//
//                loginManager.logIn(this, permissions)
//            }

        }

//        appsetting_layout.setOnClickListener {
//            sendIntent(SettingsActivity::class.java)
//        }


        binding.userEditProfile.libraryItemImageView.setImageResource(R.drawable.ic_person_black_24dp)
        binding.userEditProfile.libraryItemTextview.text = getString(R.string.edit_profile)
        binding.userEditProfile.listItemSubtitleTextview.visibility = View.INVISIBLE

        binding.userEditProfile.root.setOnClickListener {
            var bundle = bundleOf("USER" to user)
            findNavController().navigate(R.id.profileFragment , bundle)
        }

        binding.musicCostomize.libraryItemImageView.setImageResource(R.drawable.ic_baseline_album_24)
        binding.musicCostomize.libraryItemTextview.text = getString(R.string.improve_recommendation)
        binding.musicCostomize.root.setOnClickListener {
            findNavController().navigate(R.id.recommendationFragment)
        }

        binding.logoutTextview.libraryItemImageView.setImageResource(R.drawable.ic_baseline_logout_24)
        binding.logoutTextview.listItemSubtitleTextview.visibility = View.INVISIBLE
        binding.logoutTextview.libraryItemTextview.text = getString(R.string.logout)

        binding.logoutTextview.root.setOnClickListener {
            preference[AccountState.IS_REDIRECTION] = false
//            mainActivityViewmodel.stopPlayback()
            this.toControllerActivity().Logout(requireContext())
            LoginManager.getInstance().logOut()
            var prefmanager = PreferenceHelper.getInstance(requireContext())
            prefmanager.delete(AccountState.ACCESS_FACEBOOK_FRIENDS)
            sendIntent(OnboardingActivity::class.java , mainActivityViewmodel.signUpSource)
        }

//        download_nav_btn.setOnClickListener {
//            findNavController().navigate(R.id.downloadListFragment)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookcallbackManager.onActivityResult(requestCode , resultCode , data)
    }

    fun showUserInfo(context: Context) {
        var preference = PreferenceHelper.getInstance(context)
        var username = preference.getString(AccountState.USERNAME, "")
        var email = preference.getString(AccountState.EMAIL, "")
        var profilePicture = preference.getString(AccountState.PROFILE_IMAGE, "")
    }

    fun requestUserFriendFromFacebookAndSaveToDb(token : AccessToken){
        var userInfoREquest = GraphRequest.newMeRequest(token){_ , resp ->
            var ownerInfo = resp?.jsonObject
            var email = if (ownerInfo?.has("email") == true) ownerInfo.getString("email") else null
            var id = ownerInfo?.getString("id")
            var imageUrl = ownerInfo?.getJSONObject("picture")?.getJSONObject("data")?.getString("url")
            preference[AccountState.PROFILE_IMAGE] = imageUrl ?: ""
        }

        var friendequest = GraphRequest.newMyFriendsRequest(token) { _, response ->
            var userFriendsId = mutableListOf<String>()
            var jsonResponse = response?.jsonObject?.getJSONArray("data")
            if(jsonResponse != null){
                for (i in 0 until jsonResponse.length()) {
                    var jsonObject = jsonResponse.getJSONObject(i)
                    var id = jsonObject.getString("id")
                    userFriendsId.add(id)
                }
            }

            var loggedInUserId = preference.get(AccountState.USER_ID , "")
            if(loggedInUserId.isNotBlank() && !userFriendsId.isNullOrEmpty()){
                userViewmodel.saveUserFriendsData(loggedInUserId , userFriendsId).observe(viewLifecycleOwner , Observer{
                    it?.let {
                        requireContext().showDialog("Friends" , "You found ${it.size} Facebook Friends" , "Go to friends" , null){
                            findNavController().navigate(R.id.friendsListFragment)
                        }
//                        binding.connectToFbBtn.text = "Connected to Facebook"
//                        binding.connectToFbBtn.isEnabled = false
                        preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
                    }
                })
            }
            else{
                requireContext().showDialog("Friends" , "You don't have facebook friends who uses this app" , "Ok" , null){}
            }

        }
        var parameter = bundleOf("fields" to "id , friends")
        friendequest.parameters = parameter
        friendequest.executeAsync()

//        var  userREqParams =  Bundle()
//        userREqParams.putString("fields", "id,name,link,email , picture");
//        userInfoREquest.parameters = userREqParams
//        userInfoREquest.executeAsync()
    }


}