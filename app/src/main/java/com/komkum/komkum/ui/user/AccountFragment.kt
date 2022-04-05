package com.komkum.komkum.ui.user

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import com.skydoves.balloon.showAlignTop
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.data.model.SubscriptionWithPlan
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.FragmentAccountBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.setting.SettingsActivity
import com.komkum.komkum.ui.store.ProductListFragment
import com.komkum.komkum.ui.store.team.TeamGridListFragment
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class AccountFragment : Fragment() {
    lateinit var binding : FragmentAccountBinding
    val userViewmodel : UserViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()
    var user : UserWithSubscription? = null

    var noData = false

    init {
        lifecycleScope.launchWhenCreated {
            userViewmodel.getUserData()
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
        binding = FragmentAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(noData){
            menu.removeItem(R.id.account_sync_menu_item)
            menu.removeItem(R.id.cart_menu_item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")

        var pref = PreferenceHelper.getInstance(requireContext())
        pref[AccountState.IS_REDIRECTION] = false

        showUserInfo(requireContext())
        userViewmodel.userData.observe(viewLifecycleOwner){
            binding.accountLoadingProgressbar.isVisible = false
            binding.accountContainer.alpha = 1f
            binding.activityListGroup.isVisible = true
            user = it
            noData = user == null
            requireActivity().invalidateOptionsMenu()
            binding.balanceTextview.text = "${requireContext().getString(R.string.birr)} ${user?.walletBalance?.roundToInt()}"

            val currentTime = Calendar.getInstance().time
//            it.subscription?.let {
////                binding.planNameTextview.text = it.subscriptionId?.name
//                 if(it.expireDate!= null && it.expireDate!! > currentTime){
//                    binding.upgradeAccountCardview.visibility = View.INVISIBLE
//                    binding.subscriptionCardview.visibility = View.VISIBLE
//                    showSubscriptionInfo(it)
//                }
//                else if(it.expireDate!= null && it.expireDate!! < currentTime){
//                    binding.upgradeSubsHeaderTextview.text = getString(R.string.subscription_expired)
//                    binding.textView168.text = "Expired on ${it.expireDate?.convertoLocalDate(longMonthName =  false)}"
//                    binding.upgradeAccountCardview.visibility = View.VISIBLE
//                    binding.subscriptionCardview.visibility = View.INVISIBLE
//                }
//                else{
//                     binding.upgradeAccountCardview.visibility = View.VISIBLE
//                     binding.subscriptionCardview.visibility = View.INVISIBLE
//                }
//            }

//            val cal = Calendar.getInstance()
//            it.subscription?.let {
//                binding.planNameTextview.text = it.subscriptionId?.name
//                it.expireDate?.let {
//                    cal.time = it
//                    binding.nextBillingDateTextview.text = "Next billing date  ${cal.getDisplayName(
//                        Calendar.MONTH, Calendar.SHORT, Locale.getDefault())} " +
//                            "${cal.get(Calendar.DATE)}  ${cal.get(Calendar.YEAR)}"
//                }
//            }
//
//            var preference = PreferenceHelper.getInstance(requireContext())
//            var fbResult =  preference.get(AccountState.ACCESS_FACEBOOK_FRIENDS , false)
//            var isFbAuthenticated = AccessToken.getCurrentAccessToken()?.token !=null &&
//                    !AccessToken.getCurrentAccessToken().isExpired && !it.facebookId.isNullOrEmpty() && fbResult
//            if(isFbAuthenticated){
//                binding.connectToFbBtn.text = "Connected to Facebook"
//                binding.connectToFbBtn.isEnabled = false
//                var preference = PreferenceHelper.getInstance(requireContext())
//                preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
//            }
//            else {
//                binding.connectToFbBtn.text = "Connect with friends"
//                binding.connectToFbBtn.isEnabled = true
//            }
//
//
//            binding.connectToFbBtn.setOnClickListener {
//                var permissions = listOf("user_friends")
//
//                var loginManager = LoginManager.getInstance()
//                facebookcallbackManager = CallbackManager.Factory.create()
//                loginManager.registerCallback(facebookcallbackManager , object :
//                    FacebookCallback<LoginResult> {
//                    override fun onSuccess(result: LoginResult?) {
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
//                    override fun onError(error: FacebookException?) {
//                        Toast.makeText(requireContext() , "Authentication Error" , Toast.LENGTH_LONG).show()
//                    }
//                })
//
//                loginManager.logIn(this, permissions)
//            }



            binding.orderListItem.libraryItemImageView.setImageResource(R.drawable.ic_baseline_event_note_24)
            binding.orderListItem.libraryItemTextview.text = this.getString(R.string.orders)
            binding.orderListItem.root.setOnClickListener {
                (requireActivity() as MainActivity).moveToOrderList()
            }

            binding.teamsListItem.libraryItemImageView.setImageResource(R.drawable.ic_baseline_group_24)
            binding.teamsListItem.libraryItemTextview.text = getString(R.string.your_teams)
            binding.teamsListItem.root.setOnClickListener {
                (requireActivity() as MainActivity).moveToTeamList(TeamGridListFragment.LOAD_USER_TEAMS)
            }

            var teamBallon = requireActivity()
                .getBalloon(getString(R.string.user_team_description) ,
                    "TEAM_LIST", lifeyCycleOwner = viewLifecycleOwner)

            var rewardBallon = requireContext()
                .getBalloon(getString(R.string.reward_description) ,
                    "REWARD_FINDER" , 0.55f , viewLifecycleOwner)
            teamBallon.relayShowAlignTop(rewardBallon , binding.rewardFinderImageview)
            teamBallon.showAlignTop(binding.teamsListItem.imageView16)


            binding.wishlistListItem.libraryItemImageView.setImageResource(R.drawable.ic_gray_favorite_border_24)
            binding.wishlistListItem.libraryItemTextview.text = getString(R.string.wishlist)
            binding.wishlistListItem.root.setOnClickListener {
                (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_WISHLIST)
            }


            binding.changeDefaultAddress.libraryItemImageView.setImageResource(R.drawable.ic_baseline_location_on_24)
            binding.changeDefaultAddress.libraryItemTextview.text = getString(R.string.change_home_address)
            var pref = PreferenceHelper.getInstance(requireContext())
            var defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
            var defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)
            if(defaultLatitudeValue != 0f && defaultLongitudeValue != 0f){
                var address = convertLocationToAddress(requireContext() , defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble())
                var savedAddressName = pref.get(PreferenceHelper.ADDRESS_NAME , address.address ?: "Not set")
                binding.changeDefaultAddress.listItemSubtitleTextview.text = savedAddressName
            }
            else {
                requireActivity().requestAndAccessLocationBeta(){
                    var address = convertLocationToAddress(requireContext() , it.longitude , it.latitude)
                    binding.changeDefaultAddress.listItemSubtitleTextview.text = address.address ?: "unable to convert"
                }
            }

            binding.changeDefaultAddress.root.setOnClickListener {
                var homeAddressOptions = listOf(getString(R.string.use_current_address_for_default_location) , getString(
                    R.string.find_my_location_for_default_location))

                var Mdialog = MaterialDialog(requireContext()).show {
                    title(text = getString(R.string.change_home_address))
                    var list = listItemsSingleChoice(items = homeAddressOptions){ dialog, index, text ->
                        when(index){
                            0 -> {
                                binding.accountLoadingProgressbar.isVisible = true
                                requireActivity().requestAndAccessLocationBeta(onFailure = {
                                    if(it == false){
                                        (requireActivity() as MainActivity).showDialog(getString(R.string.error) , getString(R.string.location_permission_info) ,
                                            getString(R.string.grant_permission) , showNegative = true , owner = viewLifecycleOwner){
                                            requireActivity().gotoPermissionSetting()
                                        }
                                    }
                                    else{
                                        (requireActivity() as MainActivity).showDialog(getString(R.string.error) , getString(R.string.location_permission_info) ,
                                            getString(R.string.grant_permission) , showNegative = true , owner = viewLifecycleOwner){
                                            requireActivity().gotoPermissionSetting()
                                        }
                                    }
                                }){
                                    pref[PreferenceHelper.DEFAULT_LONGITUDE_VALUE] = it.longitude.toFloat()
                                    pref[PreferenceHelper.DEFAULT_LATITUDE_VALUE] = it.latitude.toFloat()

                                    pref[PreferenceHelper.IS_CURRENT_LOCATION_SELECTED] = true
                                    var address = convertLocationToAddress(requireContext() , it.longitude , it.latitude)
                                    pref[PreferenceHelper.ADDRESS_NAME] = address.address ?: "Not set"
                                    binding.accountLoadingProgressbar.isVisible = false
                                    Toast.makeText(requireContext() , getString(R.string.current_address_selected_as_home_address) , Toast.LENGTH_LONG).show()
                                    binding.changeDefaultAddress.listItemSubtitleTextview.text = address.address
                                }
                            }
                            1 -> {
                                findNavController().navigate(R.id.findMyAddressFragment)
                            }

                        }
                    }
                }
            }

//            binding.libraryItem.libraryItemImageView.setImageResource(R.drawable.ic_baseline_library_music_24)
//            binding.libraryItem.libraryItemTextview.text = this.getString(R.string.library)
//            binding.libraryItem.root.setOnClickListener {
//              findNavController().navigate(R.id.libraryFragment)
//            }

            binding.accountHeaderView.setOnClickListener {
                findNavController().navigate(R.id.userFragment)
            }

            binding.rechargeWalletImageview.setOnClickListener {
                var isFirstTime = pref.get(PreferenceHelper.FIRST_TIME_FOR_WALLET , true)
                if(isFirstTime) findNavController().navigate(R.id.walletWelcomeFragment)
                else findNavController().navigate(R.id.walletRechargeFragment)
            }

            binding.withdrawImageview.setOnClickListener {
                Toast.makeText(requireContext() , getString(R.string.coming_soon) , Toast.LENGTH_LONG).show()
            }

            binding.transactionImageview.setOnClickListener {
                findNavController().navigate(R.id.transactionFragment)
            }


            binding.rewardFinderImageview.setOnClickListener {
                findNavController().navigate(R.id.rewardDashboardFragment)
            }

            binding.upgradeAccountCardview.setOnClickListener {
                findNavController().navigate(R.id.subscriptionFragment2)
            }
        }

        binding.tryAgainButton.setOnClickListener {
            binding.errorGroup.isVisible = false
            findNavController().navigate(R.id.accountFragment)
        }

        userViewmodel.getError().observe(viewLifecycleOwner){}
        userViewmodel.error.observe(viewLifecycleOwner){
            binding.accountLoadingProgressbar.isVisible = false
           if(userViewmodel.userData.value == null){
               binding.errorGroup.isVisible = true
               noData = true
               requireActivity().invalidateOptionsMenu()

           }
            it?.handleError(requireContext() , signupSource = mainActivityViewmodel.signUpSource){
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                binding.errorTextview.isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.account_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                findNavController().navigateUp()
                true
            }
            R.id.account_sync_menu_item ->{
                binding.accountLoadingProgressbar.isVisible = true
                CoroutineScope(Dispatchers.IO).launch {
                    userViewmodel.getUserData()
                }
                true
            }
            R.id.cart_menu_item -> {
                findNavController().navigate(R.id.cartListFragment)
                true
            }
//            R.id.library_menu_item -> {
//                findNavController().navigate(R.id.libraryFragment)
//                true
//            }
            R.id.setting_menu_item ->{
                requireActivity().sendIntent(SettingsActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun showUserInfo(context: Context) {
        var preference = PreferenceHelper.getInstance(context)
        var username = preference.getString(AccountState.USERNAME, "")
        var email = preference.getString(AccountState.EMAIL, "")
        var profilePicture = preference.getString(AccountState.PROFILE_IMAGE, "placeholderimage")
        binding.accountNameTextview.text = username
        if(!username.isNullOrEmpty()) binding.accountProfileImageview.displayAvatar(profilePicture , username?.first().toString() , 18f , backgroundColor =  R.drawable.circle_2)
    }

    fun showSubscriptionInfo(subscriptionInfo : SubscriptionWithPlan){
        val cal = Calendar.getInstance().time
        binding.subsPlannameTextview.text = subscriptionInfo.subscriptionId?.name
        binding.subsStartDateTextview.text = subscriptionInfo.startDate?.convertoLocalDate()
        binding.subsEnddateTextview.text = subscriptionInfo.expireDate?.convertoLocalDate()
//        if(subscriptionInfo.expireDate!! > cal)
    }

}