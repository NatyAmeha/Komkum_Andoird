package com.zomatunes.zomatunes.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.MainActivityViewmodel
import com.zomatunes.zomatunes.OnboardingActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.data.model.UserWithSubscription
import com.zomatunes.zomatunes.databinding.UserFragmentBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.delete
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.viewhelper.CircleTransformation
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.user_fragment.*
import java.util.*

@AndroidEntryPoint
class UserFragment : Fragment() {

    lateinit var facebookcallbackManager : CallbackManager
    lateinit var binding : UserFragmentBinding

    val userViewmodel: UserViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by viewModels()
    var user : UserWithSubscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = UserFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        this.toControllerActivity().hideBottomView()

        showUserInfo(requireContext())
        userViewmodel.user.handleSingleDataObservation(viewLifecycleOwner){
            user = it
            val cal = Calendar.getInstance()
            cal.time = it.subscription?.expireDate
            binding.nextBillingDateTextview.text = "Next billing date  ${cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())} " +
                    "${cal.get(Calendar.DATE)}  ${cal.get(Calendar.YEAR)}"

            binding.planNameTextview.text = it.subscription?.subscriptionId?.name
            var preference = PreferenceHelper.getInstance(requireContext())
            var fbResult =  preference.get(AccountState.ACCESS_FACEBOOK_FRIENDS , false)
            var isFbAuthenticated = AccessToken.getCurrentAccessToken()?.token !=null &&
                    !AccessToken.getCurrentAccessToken().isExpired && !it.facebookId.isNullOrEmpty() && fbResult
            if(isFbAuthenticated){
                binding.connectToFbBtn.text = "Connected to facebook"
                binding.connectToFbBtn.isEnabled = false
                var preference = PreferenceHelper.getInstance(requireContext())
                preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
            }

        }

//        appsetting_layout.setOnClickListener {
//            sendIntent(SettingsActivity::class.java)
//        }



        (binding.userEditProfile.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_audiotrack_black_24dp)
        (binding.userEditProfile.findViewById(R.id.library_item_textview) as TextView).text = "Edit Your Profile"
        binding.userEditPrifleCard.setOnClickListener {
            var bundle = bundleOf("USER" to user)
            findNavController().navigate(R.id.profileFragment , bundle)
        }

        (binding.musicCostomize.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_album_24)
        (binding.musicCostomize.findViewById(R.id.library_item_textview) as TextView).text = "Improve Recommendation"
        binding.musicCostomize.setOnClickListener {
            var bundle = bundleOf("USER" to user)
            findNavController().navigate(R.id.recommendationFragment , bundle)
        }

        (binding.logoutTextview.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_graphic_eq_24)
        (binding.logoutTextview.findViewById(R.id.library_item_textview) as TextView).text = "Logout"

        binding.logoutTextview.setOnClickListener {
            this.toControllerActivity().Logout(requireContext())
//            mainActivityViewmodel.disconnectToService()
            LoginManager.getInstance().logOut()
            var prefmanager = PreferenceHelper.getInstance(requireContext())
            prefmanager.delete(AccountState.USER_ID)
            prefmanager.delete(AccountState.USERNAME)
            prefmanager.delete(AccountState.EMAIL)
            prefmanager.delete(AccountState.PROFILE_IMAGE)
            prefmanager.delete(AccountState.USED_AUDIOBOOK_CREDIT)
            prefmanager.delete(AccountState.ACCESS_FACEBOOK_FRIENDS)
            sendIntent(OnboardingActivity::class.java)
        }



//        download_nav_btn.setOnClickListener {
//            findNavController().navigate(R.id.downloadListFragment)
//        }

        connect_to_fb_btn.setOnClickListener {
            var permissions = listOf("user_friends")
            var loginManager = LoginManager.getInstance()
            facebookcallbackManager = CallbackManager.Factory.create()
            loginManager.registerCallback(facebookcallbackManager , object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    result?.let {
                        requestUserFriendFromFacebookAndSaveToDb(it.accessToken)
                    }
                }

                override fun onCancel() {
                    Toast.makeText(requireContext() , "Authentication Cancelled" , Toast.LENGTH_LONG).show()
                }

                override fun onError(error: FacebookException?) {
                    Toast.makeText(requireContext() , "Authentication Error" , Toast.LENGTH_LONG).show()
                }
            })

            loginManager.logIn(this, permissions)
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
        username_textview.text = username
        if (!profilePicture.isNullOrBlank()) Picasso.get().load(profilePicture)
            .placeholder(R.drawable.circularimg).transform(CircleTransformation())
            .into(user_profile_picture_imageview)
    }

    fun requestUserFriendFromFacebookAndSaveToDb(token : AccessToken){
        var request = GraphRequest.newMyFriendsRequest(token) { _, response ->
            var userFriendsId = mutableListOf<String>()
            var jsonResponse = response.jsonObject.getJSONArray("data")
            for (i in 0 until jsonResponse.length()) {
                var jsonObject = jsonResponse.getJSONObject(i)
                Log.i("resultdata", jsonObject.toString())
                var id = jsonObject.getString("id")
                Log.i("resultdataid", id)
                userFriendsId.add(id)
            }
            var preference = PreferenceHelper.getInstance(requireContext())
            var loggedInUserId = preference.get(AccountState.USER_ID , "")
            Log.i("resultdatauserid", loggedInUserId)
            if(loggedInUserId.isNotBlank() && !userFriendsId.isNullOrEmpty()){
                userViewmodel.saveUserFriendsData(loggedInUserId , userFriendsId).observe(viewLifecycleOwner , Observer{
                    it?.let {
                        preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
                        Toast.makeText(requireContext() , it.size.toString() , Toast.LENGTH_LONG).show()
                        (requireActivity() as MainActivity).showSnacbar("You can access ${it.size} friends activity")
                        findNavController().navigate(R.id.friendsListFragment)
                    }
                })
            }
            else{
                Toast.makeText(requireContext() , "no user friend found" , Toast.LENGTH_LONG).show()
            }

        }
        var parameter = bundleOf("fields" to "id , friends")
        request.parameters = parameter
        request.executeAsync()
    }


}