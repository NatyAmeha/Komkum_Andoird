package com.komkum.komkum.ui.store

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.InAppMessage
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.StoreHomepageFragmentBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.component.imageComposable
import com.komkum.komkum.ui.component.searchBar
import com.komkum.komkum.ui.home.HomeViewModel
import com.komkum.komkum.ui.store.team.TeamFragment
import com.komkum.komkum.ui.store.team.TeamGridListFragment
import com.komkum.komkum.ui.theme.*
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.PackageAdapter
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.adaper.TeamAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.*
import kotlin.concurrent.schedule

@ExperimentalMaterialApi
@ExperimentalPagerApi
@AndroidEntryPoint
class StoreHomepageFragment : Fragment(), IRecyclerViewInteractionListener<Product>
    , FirebaseInAppMessagingClickListener {

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()
    val homeViewmodel: HomeViewModel by viewModels()

    lateinit var binding : StoreHomepageFragmentBinding

    lateinit var timer : TimerTask


    init {
        lifecycleScope.launchWhenCreated {
            var pref = PreferenceHelper.getInstance(requireContext())
            var defaultLatitudeValue = pref.get(PreferenceHelper.DEFAULT_LATITUDE_VALUE , 0f)
            var defaultLongitudeValue = pref.get(PreferenceHelper.DEFAULT_LONGITUDE_VALUE , 0f)
            if(defaultLatitudeValue != 0f || defaultLongitudeValue != 0f){
                homeViewmodel.getStoreHomepageResult(defaultLongitudeValue.toDouble() , defaultLatitudeValue.toDouble())
            }
            else{
                requireActivity().requestAndAccessLocationBeta(onFailure = {locationStatus ->
                    if(locationStatus == false){
                        (requireActivity() as MainActivity).showDialog(getString(R.string.error) ,
                            getString(R.string.app_need_location_feature) ,
                            getString(R.string.turn_on_location),
                            autoDismiss = false){
                            var intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(intent)
                        }
                    }
                    homeViewmodel.getStoreHomepageResult() }) {
                    homeViewmodel.getStoreHomepageResult(it.longitude , it.latitude)
                }
            }

            PreferenceHelper.getInstance(requireContext())[PreferenceHelper.RELOAD_STORE_PAGE] = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = StoreHomepageFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.reloadCardview.isVisible = false

        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        binding.adProductViewpager.setContent {
            ZomaTunesTheme() {
                headerScreen(userId)
            }
        }
        var largeProductDisplayinfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.MAIN_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_2)
        var miniProductDisplayinfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.MINI_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)


        var teamListener = object : IRecyclerViewInteractionListener<Team<Product>> {
            override fun onItemClick(data: Team<Product>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , backstackId = R.id.storeHomepageFragment)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var packageListener = object : IRecyclerViewInteractionListener<ProductPackage<Product>> {
            override fun onItemClick(data: ProductPackage<Product>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoTeamDetails(data._id!! , loadType = TeamFragment.LOAD_PACKAGE_TEAM , backstackId = R.id.storeHomepageFragment)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var categoryListener = object : IRecyclerViewInteractionListener<Category> {
            override fun onItemClick(data: Category, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToBrowseProductByDepartment(data.name!!)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var teamInfo = RecyclerViewHelper(interactionListener = teamListener , listItemType = TeamAdapter.MAIN_TEAM_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        var categoryInfo = RecyclerViewHelper(interactionListener = categoryListener , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
        var packageInfo = RecyclerViewHelper(interactionListener = packageListener , listItemType = PackageAdapter.PACKAGE_MAIN_LIST_ITEM  , owner = viewLifecycleOwner)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.miniDisplayInfo = miniProductDisplayinfo
        binding.mainDisplayInfo = largeProductDisplayinfo
        binding.teamInfo = teamInfo
        binding.packageInfo = packageInfo
        binding.categoryInfo = categoryInfo
        binding.homeViewmodel = homeViewmodel



        homeViewmodel.storeHomepageResult.observe(viewLifecycleOwner){
            binding.storeLoadingProgressbar.isVisible = false
            binding.errorView.root.isVisible = false

            mainActivityViewmodel.totalCartCounter.value = mainActivityViewmodel.totalCartCounter.value?.plus(it.cartSize?: 0)
            it.cartSize = 0
        }

        binding.moreTrendingProductTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_TRENDING_PRODUCTS)
        }


        binding.moreExpireSoonProductTeamTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToTeamList(TeamGridListFragment.LOAD_EXPIRE_SOON)
        }

        binding.moreTrendingPackageTeamTextview.setOnClickListener {
            findNavController().navigate(R.id.packageListFragment)
//            (requireActivity() as MainActivity).moveToTeamList(TeamGridListFragment.LOAD_TRENDING_PACKAGES)
        }

        binding.mroeBestsellingTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_BESTSELLING_PRODUCTS)
        }

        binding.seeMoreNewArrival.setOnClickListener {
            (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_NEW_ARRIVAL)
        }


        binding.reloadCardview.isVisible = pref.get(PreferenceHelper.RELOAD_STORE_PAGE , false)
        pref.set(PreferenceHelper.RELOAD_STORE_PAGE , false)

        binding.reloadCardview.setOnClickListener {
            binding.reloadCardview.isVisible = false
            pref[PreferenceHelper.RELOAD_STORE_PAGE] = false
            binding.storeLoadingProgressbar.isVisible = true
            requireActivity().requestAndAccessLocationBeta(onFailure = {
                if(it == false){
                    (requireActivity() as MainActivity).showDialog(getString(R.string.error) ,
                        getString(R.string.app_need_location_feature) ,
                        getString(R.string.turn_on_location),
                        autoDismiss = false){
                        var intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                }
                homeViewmodel.getStoreHomepageResult() }) {
                homeViewmodel.getStoreHomepageResult(it.longitude , it.latitude)
            }
        }


        // Show sign up bonus dialog
        var isDialogShowedBefore = pref.get(PreferenceHelper.WALLET_BONUS_DIALOG_BEFORE_SIGN_UP, true)
        var firstTimeForLanguageDialog = pref.get(PreferenceHelper.FIRST_TIME_FOR_LANGUAGE_DIALOG, true)
        if (isDialogShowedBefore && !firstTimeForLanguageDialog) {
            requireActivity().showDialog(
                getString(R.string.signup_bonus),
                getString(R.string.signup_bonus_description),
                getString(R.string.signup),
                showNegative = true,
                autoDismiss = false,
                owner = viewLifecycleOwner,
                isBottomSheet = true
            ) {
                mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.F_E_REGISTER_FOR_WALLET, null)
                sendIntent(OnboardingActivity::class.java , mainActivityViewmodel.signUpSource)
            }
            pref[PreferenceHelper.WALLET_BONUS_DIALOG_BEFORE_SIGN_UP] = false

            // show join telegram banner for first time user
            timer = Timer().schedule(30000){
                //Firebase in app messaging banner to nudge user to join our telegram channel
                var firebaseInAppmessaging = FirebaseInAppMessaging.getInstance()
                firebaseInAppmessaging.addClickListener(this@StoreHomepageFragment)
                var showJoinTelegramChannelBanner = pref.get(PreferenceHelper.SHOW_JOIN_TELEGRAM_CHANNEL_BANNER , true)
                if(showJoinTelegramChannelBanner){
                    firebaseInAppmessaging.triggerEvent("join_telegram")
                }
            }

        }
        else {
            var walletBonusSuccessDialog =
                pref.get(PreferenceHelper.WALLET_BONUS_DIALOG_AFTER_SIGN_UP, false)
            if (walletBonusSuccessDialog && (requireActivity() as MainActivity).isLoggedIn(requireContext())) {
                pref[PreferenceHelper.WALLET_BONUS_DIALOG_AFTER_SIGN_UP] = false
                requireActivity().showDialog(
                    getString(R.string.congrats),
                    getString(R.string.sign_up_bonus_description),
                    getString(R.string.go_to_wallet),
                    showNegative = true,
                    autoDismiss = true,
                    isBottomSheet = true
                ) {
                    findNavController().navigate(R.id.accountFragment)
                }
            }
        }


        homeViewmodel.getError().observe(viewLifecycleOwner){}
        homeViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.storeLoadingProgressbar.isVisible = false
            error?.handleError(requireContext() , {homeViewmodel.removeOldErrors()},
                signupSource = mainActivityViewmodel.signUpSource) {
                binding.errorView.root.isVisible = true
                binding.errorView.gotoDownloadBtn.isVisible = false
                binding.errorView.errorTextview.text = getString(R.string.error_message)
                binding.errorView.tryagainBtn.setOnClickListener {
                    findNavController().navigate(R.id.storeHomepageFragment)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
          999 -> {
              if(grantResults.isNotEmpty()){
                  getLocation(requireContext()){
                      homeViewmodel.getStoreHomepageResult(it.longitude , it.latitude)
                  }
              }
              else homeViewmodel.getStoreHomepageResult()
          }
      }
    }



    override fun onDestroyView() {
        if(::timer.isInitialized) timer.cancel()
        super.onDestroyView()
    }


    override fun onItemClick(data: Product, position: Int, option: Int?) {
        var preference = PreferenceHelper.getInstance(requireContext())
        preference[PreferenceHelper.PURCHASED_FROM] = null
        (requireActivity() as MainActivity).movetoProductDetails(data._id!!)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


    override fun messageClicked(p0: InAppMessage, action: Action) {
        requireActivity().runOnUiThread {
            var url = action.actionUrl
            if(url?.startsWith( "https://t.me/KomkumApp") == true)
                PreferenceHelper.getInstance(requireContext())[PreferenceHelper.SHOW_JOIN_TELEGRAM_CHANNEL_BANNER] = false
        }
    }


    @Composable
    fun headerScreen(userId : String){
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
           searchAndMainCategorySection()
            adBannerSection(Modifier.height(dimensionResource(id = R.dimen.ad_banner_height)) , userId)
//            yourTeamsSection(userId){team ->
//                if (team.type == Team.TRIVIA_TEAM) team.ad?._id?.let { it1 ->
//                    (requireActivity() as MainActivity).moveToGameTeamDetails(it1 , backstackId = R.id.storeHomepageFragment)
//                }
//                else (requireActivity() as MainActivity).movetoTeamDetails(team._id!!, backstackId = R.id.storeHomepageFragment)
//            }
        }
    }


    @Composable
    fun searchAndMainCategorySection(){
        val itemsInCart by mainActivityViewmodel.totalCartCounter.observeAsState()

        Column(Modifier.padding(bottom = 12.dp)) {
            searchBar(onclick = {findNavController().navigate(R.id.productSearchFragment)}){
                BadgeBox(modifier = Modifier.padding( 16.dp),
                    badgeContent = {
                        Text(text = "${itemsInCart ?: 0}")
                    } , backgroundColor = Color.Green) {
                    Icon(
                        Icons.Filled.ShoppingCart, tint = Color.DarkGray , contentDescription = "",
                        modifier = Modifier
                            .size(30.dp, 30.dp)
                            .clickable {
                                findNavController().navigate(R.id.cartListFragment)
                            })
                }
            }

            //main category rows
            var mainCat = listOf(
                "Groceries",
                "Apparel",
                "Fashion",
                "Electronics"
            )
            LazyRow{
                itemsIndexed(mainCat){index , dep ->
                    Text(text = dep ,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickable {
                                (requireActivity() as MainActivity).moveToBrowseProductByDepartment(dep)
                            },
                        style = TextStyle(fontSize = dimensionResource(id = R.dimen.caption).value.sp))
                }
            }
        }
    }

    @ExperimentalPagerApi
    @Composable
    fun adBannerSection(modifier: Modifier = Modifier , userId: String){
        val homepageResult  by homeViewmodel.storeHomepageResult.observeAsState()
        homepageResult?.let {
            var ads = it.ads

            Column(Modifier.padding(top = 8.dp , bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {

                if(!ads.isNullOrEmpty()){
                    var adsImage = ads?.map { ad -> ad.banner ?: ad.thumbnailPath!! }
                    // ad banner component
                    var pagerState = rememberPagerState(
                        pageCount = adsImage.size,
                        initialOffscreenLimit = 2,
                        infiniteLoop = true
                    )
                    Column {
                        HorizontalPager(state = pagerState) { page ->
                            imageComposable(image = adsImage[page] , R.drawable.product_placeholder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp)
                                    .height(dimensionResource(id = R.dimen.ad_banner_height).value.dp)
                                    .aspectRatio(1f)){
                                //banner click handler
//                                mainActivityViewmodel.firebaseAnalytics.logEvent(FcmService.FIREBASEANALYTICS_EVENT_AD_CLICK){
//                                    param(FcmService.F_EVENT_PARAM_AD_SOURCE , FcmService.F_EVENT_PARAM_VALUE_BANNER)
//                                }
//                                homeViewmodel.updateProductAdClick(ads[0]._id).observe(viewLifecycleOwner){}
//
//                                //save preference to identify from where the order came from in order fragment
//                                var preference = PreferenceHelper.getInstance(requireContext())
//                                preference[PreferenceHelper.PURCHASED_FROM] = FcmService.F_EVENT_VALUE_PURCHASED_FROM_AD
//
//                                (requireActivity() as MainActivity).movetoProductDetails(ads[page].adContent!!)

                            }
                        }

                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )

                        LaunchedEffect(pagerState.currentPage) {
                            delay(5000) // wait for 3 seconds.
                            // increasing the position and check the limit
                            var newPosition = pagerState.currentPage + 1
                            if (newPosition > adsImage.lastIndex) newPosition = 0
                            // scrolling to the new position.
                            pagerState.animateScrollToPage(newPosition)
                        }
                    }
                }


                // Main activities and tags

                var userTeams = it.yourTeams?.filter { team ->
                    var result = team.members?.find { member -> member.user == userId && member.ordered == true }
                    result == null
                }

                var activities = mutableListOf(
                    stringResource(R.string.groceries),
                    stringResource(R.string.track_your_teams) ,
                    stringResource(R.string.discount_code_for_sale),
                    stringResource(R.string.play_game_win_prize),
                    "Gift\nitems         ",
                    stringResource(R.string.contact_us)
                )

                var icons = mutableListOf(
                    R.drawable.ic_dashboard_black_24dp,
                    R.drawable.ic_baseline_group_24,
                    R.drawable.ic_baseline_discount_24,
                    R.drawable.ic_outline_emoji_events_24,
                    R.drawable.ic_baseline_card_giftcard_24,
                    R.drawable.ic_baseline_call_24
                    )
                if(userTeams.isNullOrEmpty()) {
                    activities.removeAt(1)
                    icons.removeAt(1)
                }

                mainHeadActivityList(activities = activities.toList() , icons){selectedValue ->
                    when(selectedValue){
                        getString(R.string.track_your_teams) -> (requireActivity() as MainActivity).moveToTeamList(TeamGridListFragment.LOAD_USER_TEAMS)
                        getString(R.string.play_game_win_prize) -> findNavController().navigate(R.id.gameListFragment)
                        getString(R.string.discount_code_for_sale) -> {Toast.makeText(requireContext() , getString(R.string.coming_soon) , Toast.LENGTH_SHORT).show()}
                        getString(R.string.groceries) -> (requireActivity() as MainActivity).moveToBrowseProductByDepartment("Groceries")
                        getString(R.string.contact_us) -> {
                            var intent = Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:0921320005"))
                            requireActivity().startActivity(intent)
                        }
                        else -> (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_BY_TAG , tag = selectedValue)
                    }
                }
            }
        }
    }



    @Composable
    fun yourTeamsSection(userId: String  , onrecentActivityClick : (teamInfo : Team<Product>) -> Unit){
        val homepageResult by homeViewmodel.storeHomepageResult.observeAsState()

        homepageResult?.yourTeams?.let{
            var userTeams = it.filter { team ->
                var result =
                    team.members?.find { member -> member.user == userId && member.ordered == true }
                result == null
            }
            if(it.isNotEmpty()){
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                    verticalArrangement = Arrangement.spacedBy(8.dp)){
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp),horizontalArrangement= Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically){
                        Text(text = stringResource(R.string.recent_activities) , color = Color.White , fontSize = 17.sp , fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForwardIos, tint= Color.White ,  contentDescription = "" ,
                            modifier = Modifier
                                .width(20.dp)
                                .height(20.dp))
                    }

                    LazyRow{
                        itemsIndexed(sortTeams(userTeams)){_, team ->
                            Box(
                                Modifier
                                    .width(100.dp)
                                    .padding(end = 8.dp)
                                    .clickable { onrecentActivityClick(team) },
                                contentAlignment = Alignment.Center){

                                var text = if(team.teamSize!! > team.members!!.size){
                                    val s = if (team.type == Team.TRIVIA_TEAM) stringResource(R.string.play_now)
                                    else "${team.teamSize?.minus(team.members!!.size)} ${stringResource(R.string.remaining)}"
                                    s
                                }
                                else if(team.type == Team.TRIVIA_TEAM) stringResource(R.string.view_details)
                                else stringResource(id = R.string.order_now)

                                var color = if((team.teamSize!! <= team.members!!.size)) MaterialTheme.colors.primary
                                else  Color.White
                                Column(Modifier.padding(8.dp)) {
                                    imageComposable(image = team.products?.firstOrNull()?.gallery?.firstOrNull { it.type == "image" }?.path,
                                        placeholder = R.drawable.gameimage , modifier = Modifier
                                            .width(90.dp)
                                            .height(80.dp)){
                                        onrecentActivityClick(team)
                                    }
                                    Text(text = team.name!!, color = Color.White, fontSize = 13.sp , maxLines = 1,
                                        overflow = TextOverflow.Ellipsis)

                                    Text(text = text , fontSize = 11.sp , maxLines = 1,  color = color ,
                                        overflow = TextOverflow.Ellipsis)
                                }

                                Surface(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .align(Alignment.TopStart),
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(0.dp),
                                    color = Color.Magenta,
                                ) {
                                    Row(modifier = Modifier.toggleable(true , onValueChange = {})) {
                                        Text(
                                            text = if(team.type == Team.TRIVIA_TEAM) "Game" else "${team.products?.size ?: 1} products",
                                            fontSize = 8.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Composable
    fun mainHeadActivityList(activities : List<String> , icons : List<Int> , onclick : (selectedValue : String) -> Unit){
        var colors = listOf(Yellow , dominant1 , dominant2 , dominant3)
        LazyRow{
            itemsIndexed(activities){index , ac ->
                Box(modifier = Modifier
                    .widthIn(min = 160.dp)
                    .padding(horizontal = 8.dp , vertical = 6.dp)
                    .background(colors[index % colors.size], RoundedCornerShape(6.dp))
                    .clickable { onclick(ac) }){
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(text = ac , color = Color.White , fontSize = dimensionResource(id = R.dimen.body_2).value.sp , fontWeight = FontWeight.Bold)
                        Icon(
                            painterResource(id = icons[index]),
                            tint = Color.White,
                            contentDescription = "",
                            modifier = Modifier.size(dimensionResource(id = R.dimen.medium_icon_size).value.dp,dimensionResource(id = R.dimen.medium_icon_size).value.dp))
                    }
                }
            }
        }
    }


    fun sortTeams(teams : List<Team<Product>>) : List<Team<Product>>{
        var result = teams.sortedBy { team -> team.teamSize?.minus(team.members?.size ?: 0) }
        return result
    }


}