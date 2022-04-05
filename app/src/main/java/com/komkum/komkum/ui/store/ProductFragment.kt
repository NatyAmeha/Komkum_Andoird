package com.komkum.komkum.ui.store

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.skydoves.balloon.showAlignTop
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.ProductDetailViewmodel
import com.komkum.komkum.databinding.FragmentProductBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.book.ReviewListFragment
import com.komkum.komkum.ui.component.productSku
import com.komkum.komkum.ui.component.viewPagerComponent
import com.komkum.komkum.ui.store.team.TeamGridListFragment
import com.komkum.komkum.ui.store.team.TeamListFragment
import com.komkum.komkum.ui.store.team.TeamViewModel
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.ProductAdapter
import com.komkum.komkum.util.adaper.ReviewAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.roundToInt

@ExperimentalPagerApi
@AndroidEntryPoint
class ProductFragment : Fragment() , IRecyclerViewInteractionListener<Product> {

    lateinit var binding : FragmentProductBinding
    val productViewmodel : ProductViewModel by viewModels()
    val teamViewmodel : TeamViewModel by viewModels()

    val mainactivityViewmodel : MainActivityViewmodel by activityViewModels()

    var productId : String? = null
    var hideAction : Boolean = false

    var productDetail : ProductDetailViewmodel? = null
    var productGallery = MutableLiveData<List<ProductGallery>>()
    var selectedSkuIndex = 0

    var selectedSku : SKUCombination? = null


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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.statusBarColor = resources.getColor(R.color.light_secondaryDarkColor)
        }
        arguments?.let {
            productId = it.getString("PRODUCT_ID")
            hideAction = it.getBoolean("HIDE_ACTION")
        }
        binding = FragmentProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.product_details))
        var productDisplayinfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner , listItemType = ProductAdapter.MINI_PRODUCT_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(verticalOffset == 0) binding.toolbar.visibility = View.INVISIBLE
            else if(verticalOffset == -appBarLayout.totalScrollRange) binding.toolbar.visibility = View.VISIBLE
        })

        binding.productOptionsComposeview.setContent {
            ZomaTunesTheme {
                SkuOptionScreen{sku ->
                    selectedSku = sku
                    productDetail?.product?.let {
                        var newSTdPrice = it.product?.stdPrice?.plus(sku.addPrice!!)
                        var newDscPrice = it.product?.dscPrice?.plus(sku.addPrice!!)
                        binding.prDiscountPriceTextview.text = "${getString(R.string.birr)} ${newDscPrice?.roundToInt()}"
                        binding.prStandardPriceTextview.text = "${getString(R.string.birr)} ${newSTdPrice?.roundToInt()}"
                        binding.prPriceTextview.text = "${getString(R.string.birr)} ${newDscPrice?.roundToInt()} - ${getString(R.string.birr)} ${newSTdPrice?.roundToInt()}"
                        sku.images?.map {  skuImage  -> ProductGallery(path = skuImage , type = "image")}
                            .let { productGallery.value = it?.sortedByDescending { it.type } }
                    }
                }
            }
        }
        binding.productGalleryViewpager.setContent {
            ZomaTunesTheme {
                productGallerSEction()
            }
        }

        productId?.let {
            productViewmodel.getProductDetails(it)
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.miniDisplayInfo = productDisplayinfo
        binding.productViewmodel = productViewmodel

        if(hideAction) binding.productBottomMenu.isVisible = false


        productViewmodel.productDetails.observe(viewLifecycleOwner){
            binding.errorTextview.isVisible = false
            productDetail = it
            displayUI(it)
        }

        binding.prDescriptionTextview.setOnClickListener {
            if(binding.prDescriptionTextview.maxLines == 6) binding.prDescriptionTextview.maxLines = 1000
            else binding.prDescriptionTextview.maxLines = 6
        }

        binding.sellerStoreCardview.setOnClickListener {
            var bundle = bundleOf("SELLER_ID" to productDetail?.product?.seller)
            findNavController().navigate(R.id.sellerFragment , bundle)
        }

        binding.addToWishlistImageview.setOnClickListener {
            var pref = PreferenceHelper.getInstance(requireContext())
            pref[AccountState.IS_REDIRECTION] = true
            productDetail?.product?.product?.let {
                binding.productLoadingProgressbar.isVisible = true
                productViewmodel.addProductTowishlist(it._id!!).observe(viewLifecycleOwner){
                    binding.productLoadingProgressbar.isVisible = false
                    if(it == true){
                        binding.addToWishlistImageview.visibility = View.INVISIBLE
                        binding.removeFromWishlistImageview.visibility = View.VISIBLE
                        Toast.makeText(requireContext() , getString(R.string.added_to_list) , Toast.LENGTH_LONG).show()
                    }
                    else Toast.makeText(requireContext() , "Unable to remove from your list"  , Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.removeFromWishlistImageview.setOnClickListener {
            productDetail?.product?.product?.let {
                binding.productLoadingProgressbar.isVisible = true
                productViewmodel.removeProductFromWishlist(it._id!!).observe(viewLifecycleOwner){
                    binding.productLoadingProgressbar.isVisible = false
                    if(it == true){
                        binding.addToWishlistImageview.visibility = View.VISIBLE
                        binding.addToWishlistImageview.setTextColor(resources.getColor(R.color.primaryColor))
                        binding.removeFromWishlistImageview.visibility = View.INVISIBLE
                        Toast.makeText(requireContext() , getString(R.string.remove_from_list) , Toast.LENGTH_LONG).show()
                    }
                    else Toast.makeText(requireContext() , "Unable to remove from your list" , Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.seeMoreReviewBtn.setOnClickListener {
            productId?.let {  (requireActivity() as MainActivity).moveToReviewList(it , ReviewListFragment.LOAD_PRODUCT_REVIEW) }
        }

        productViewmodel.getErrors().observe(viewLifecycleOwner){}
        productViewmodel.error.observe(viewLifecycleOwner){
            binding.productLoadingProgressbar.isVisible = false
            it?.handleError(requireContext() , {productViewmodel.removeOldErrors()},
                signupSource = mainactivityViewmodel.signUpSource){
                if(productDetail == null) binding.errorTextview.isVisible = true
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
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

    fun displayUI(productInfo : ProductDetailViewmodel){

        productInfo.product?.product?.gallery?.let {
            productGallery.value = it.sortedByDescending { it.type }
        }

        if(productInfo.favorite){
            binding.addToWishlistImageview.visibility = View.INVISIBLE
            binding.removeFromWishlistImageview.visibility = View.VISIBLE
        }

        if(productInfo.product?.product?.refund == true){
            binding.returnPolicyHeaderTextview.isVisible = true
            binding.returnPolicyDescTextview.isVisible = true
        }
        else binding.returnEligiibleCardView.isVisible = false


        var stdProductPrices = productInfo.product?.product?.stdPrice
        var discountPRices =  productInfo.product?.product?.dscPrice


        var discountedPercent = (stdProductPrices?.let { (discountPRices?.div(it))?.times(100) })
        discountedPercent?.let { binding.prPriceTextview.text = "upto ${it}% discount" }

        binding.prPriceTextview.text = "${getString(R.string.birr)} ${productInfo.product?.product?.dscPrice?.roundToInt()} - ${getString(R.string.birr)} ${productInfo.product?.product?.stdPrice?.roundToInt()}"
        binding.prTitleTextview.text = productInfo.product?.product?.title
        binding.prStandardPriceTextview.text = "${getString(R.string.birr)} ${productInfo.product?.product?.stdPrice?.roundToInt()}"

        binding.minOrderTextview.text = "${productInfo.product?.product?.minQty ?: 1} ${productInfo.product?.product?.unit ?: ""}"
        if(productInfo.product?.teamPurchase == false){
            binding.addToPackage.isVisible = false
            binding.teamPurchaseCardview.alpha = 0.5f
            binding.prDiscountPriceTextview.text = getString(R.string.team_purchase_unavailable)
        }
        else binding.prDiscountPriceTextview.text = "${getString(R.string.birr)} ${productInfo.product?.product?.dscPrice?.roundToInt()}"

        var activeTEams = productInfo.product?.product?.teams?.filter { team ->
            if(team.expDate == null) true
            else team.expDate!! > Calendar.getInstance().time
        }

        if(productInfo.product?.product?.category?.contains("Groceries") == true){
            binding.reviewHeaderTextview.isVisible = false
            binding.writeReviewOptionBtn.isVisible = false
            binding.noReviewIndeicatorTextview.isVisible = false
        }

        if(productInfo.product?.reviews.isNullOrEmpty()) binding.noReviewIndeicatorTextview.isVisible = true
        binding.totalRatingReviewTextview.text = "${productInfo.totalRating ?: 0} (${productInfo.product?.reviews?.size ?: 0} Reviews)"
        binding.prRatingRatingbar.rating = productInfo.totalRating ?: 0f
        binding.prSellerNameTextview.text = "Sold by  ${productInfo.product?.product?.sellerName}"
        binding.seeMoreReviewBtn.isVisible = (productInfo.product?.reviews?.size ?: 0) > 10

        var description = productInfo.product?.desc?.joinToString("\n")
        binding.prDescriptionTextview.text = description

        binding.avgDeliveryDayTextview.text =
            if(productInfo.product?.product?.avgDeliveryDay == 1)
                "${getString(R.string.avg_delivery)}   ${getString(R.string.next_day_delivery)}"
            else
                "${getString(R.string.avg_delivery)}   ${getString(R.string.up_to)} ${productInfo.product?.product?.avgDeliveryDay ?: 3} ${getString(R.string.days)}"


        binding.productTagsChiipGroup.removeAllViews()
        productInfo.product?.product?.tags?.forEach { tag ->
            var chip = Chip(requireContext())
            chip.text = tag
//            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light_secondaryDarkColor))
            chip.setOnClickListener {
                (requireActivity() as MainActivity).moveToProductList(ProductListFragment.LOAD_BY_TAG , tag = tag)
            }
            binding.productTagsChiipGroup.addView(chip)
        }

        if(!productInfo.recentReviews.isNullOrEmpty()){
            var reviewAdapter = ReviewAdapter()
            binding.reviewRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.reviewRecyclerview.adapter = reviewAdapter
            reviewAdapter.submitList(productInfo.recentReviews.sortedByDescending { rating ->rating.date })
        }

        var teamPurchaseBallon = requireActivity().getBalloon(getString(R.string.team_purchase_description) , "TEAM_PURCHASE" , 0.8f , viewLifecycleOwner )
        var standardPurchaseBallon = requireActivity().getBalloon(getString(R.string.standard_purchase_description) , "STANDARD_PURCHASE" , 0.3f , viewLifecycleOwner )
//        var  packageBallon =  requireActivity().getBalloon(getString(R.string.add_to_package_description) , "ADD_TO_PACKAGE" , 0.1f, viewLifecycleOwner )

        teamPurchaseBallon.relayShowAlignTop(standardPurchaseBallon , binding.standardPurchaseCardview)

        teamPurchaseBallon.showAlignTop(binding.teamPurchaseCardview)


        binding.teamPurchaseCardview.setOnClickListener {
            if(productInfo.product?.teamPurchase == false){
                (requireActivity() as MainActivity).showDialog(getString(R.string.sorry) , getString(R.string.team_purchase_unavailable_msg)){}
            }
            else (requireActivity() as MainActivity).moveToTeamList(TeamListFragment.LOAD_ACTIVE_TEAMS_FOR_PRODUCT ,  productInfo.product!! , "Addis ababa")
        }
        binding.standardPurchaseCardview.setOnClickListener {
            showBuyNowDialog(productInfo)
//            var productIds = productInfo.product?.product?._id
//            productIds?.let { it1 -> (requireActivity() as MainActivity).moveToProductCustomization(it1 , Product.STANDARD_PURCHASE) }
        }

        binding.addToPackage.setOnClickListener {
            addtoPackageHandler(productInfo.product!!.product!!._id!! , productInfo.product.teamSize ?: 10 ,
                onfailure = {binding.productLoadingProgressbar.isVisible = false}){id , teamName ->
                binding.productLoadingProgressbar.isVisible = false
                (requireActivity() as MainActivity).showSnacbar("Product added to $teamName" , "view"){
                    (requireActivity() as MainActivity).movetoPackageTeamDetails(id)
                }
            }
        }

        binding.writeReviewOptionBtn.setOnClickListener {
            productInfo.product?.product?.let {
                var image = it.gallery?.first()?.path ?: ""
                (requireActivity() as MainActivity).movetoCreateReview(it._id!! , Review.CONTENT_TYPE_PRODUCT ,  image , it.title!! , it.sellerName!!)
            }
        }
    }

    override fun onItemClick(data: Product, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoProductDetails(data._id!!)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


    fun addtoPackageHandler(productId : String , teamSize : Int   , onfailure : () -> Unit , onSuccess : (teamId : String , teamName : String) -> Unit){
        binding.productLoadingProgressbar.isVisible = true
        productViewmodel.getUserTeamsForPackages().observe(viewLifecycleOwner){
            if (!it.isNullOrEmpty()){
                binding.productLoadingProgressbar.isVisible = false
                MaterialDialog(requireContext()).show {
                    var teamNames = it.filter { team -> (!team.active && team.type == Team.MULTI_PRODUCT_TEAM) }.map { team -> team.name!! }
                    title(text = getString(R.string.choose_package))
                    cancelOnTouchOutside(false)
                    var dialog = listItems(items = teamNames , waitForPositiveButton = false) { _, index, text ->
                        var teamId = it[index]._id!!
                        teamViewmodel.addProductToTeam(teamId , productId).observe(viewLifecycleOwner){
                            if(it == true) onSuccess(teamId , teamNames[index])
                            else onfailure()
                        }
                        binding.productLoadingProgressbar.isVisible = true
                        dismiss()
                    }
                    positiveButton(text = getString(R.string.create_new_package)) {
                        (requireActivity() as MainActivity).moveToCreateTEam(listOf(productId) , teamSize , teamType = Team.MULTI_PRODUCT_TEAM)
                    }
                    negativeButton(text = getString(R.string.cancel)){dismiss()}
                }
            }
            else (requireActivity() as MainActivity).moveToCreateTEam(listOf(productId) , teamSize , teamType = Team.MULTI_PRODUCT_TEAM)
        }
    }


    @Composable
    fun SkuOptionScreen(onClick : (selectedsku : SKUCombination) -> Unit){
        val productMetadata by productViewmodel.productDetails.observeAsState()
        var selectedIndex by remember{ mutableStateOf(-1)}
        selectedSkuIndex = if (selectedIndex == -1) 0 else selectedSkuIndex

        var skus = productMetadata?.product?.skuComb
        if(!skus.isNullOrEmpty()){
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)){
                itemsIndexed(skus){ index , skuCombination ->
                    var image = skuCombination.images?.firstOrNull()
                    var skuValues = skuCombination.value?.map { (key , value) -> "$key   $value"  }
                    productSku(index , selectedIndex , image , skuValues){
                        selectedIndex = it
                        onClick(skus[it])
                    }
                }
            }
        }
    }


    fun showBuyNowDialog(fullProductInfo : ProductDetailViewmodel){
        var dialog =  MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .cancelOnTouchOutside(true)
            .cornerRadius(literalDp = 14f).customView(R.layout.add_to_cart_dialog_view , scrollable = true)

        var customview =  dialog.getCustomView()
        var productImageview = customview.findViewById<ImageView>(R.id.selected_product_imageview)
        var productPriceTextview = customview.findViewById<TextView>(R.id.selected_product_price_textview)
        var productTitleTextview = customview.findViewById<TextView>(R.id.selected_sku_title_textview)
        var addToCartBtn = customview.findViewById<Button>(R.id.add_to_cart_button)
        var buyNowBtn = customview.findViewById<Button>(R.id.buy_now_btn)
        var qtyTextview = customview.findViewById<TextView>(R.id.selected_product_qty_textview)
        var qtyAddBtn = customview.findViewById<ImageView>(R.id.qty_add_imageview)
        var qtyRemoveBtn = customview.findViewById<ImageView>(R.id.qty_remove_imageview)


        var productSkus = fullProductInfo.product?.skuComb
        var productTitle = fullProductInfo.product?.product!!.title
        var skuName = selectedSku?.value?.map { (key , value) -> "$value" }?.joinToString(",")
            ?: productSkus?.firstOrNull()?.value?.map { (key , value) -> "$value" }?.joinToString(",") ?: ""
        var selectedSkuImage = selectedSku?.images?.firstOrNull()
            ?: productSkus?.getOrNull(selectedSkuIndex)?.images?.firstOrNull()
            ?: fullProductInfo.product?.product?.gallery?.firstOrNull{it.type == "image"}?.path
        var productPrice = fullProductInfo.product!!.product!!.stdPrice!! + (selectedSku?.addPrice ?: 0)
        var minQty = fullProductInfo.product?.product?.minQty ?: 1
        var unit = fullProductInfo.product.product?.unit ?: "unit"

        Picasso.get().load(selectedSkuImage)
            .placeholder(R.drawable.product_placeholder).fit().centerCrop().into(productImageview)
        productTitleTextview.text = "$productTitle \n$skuName"
        productPriceTextview.text = "${getString(R.string.birr)} ${productPrice.times(minQty)}"
        qtyTextview.text = "$minQty"

        qtyAddBtn.setOnClickListener {
            qtyTextview.text = "${qtyTextview.text.toString().toInt().plus(1)}"
            productPriceTextview.text = "${getString(R.string.birr)} ${productPrice.times(qtyTextview.text.toString().toInt())}"
        }
        qtyRemoveBtn.setOnClickListener {
            if(qtyTextview.text.toString().toInt() > minQty){
                qtyTextview.text = "${qtyTextview.text.toString().toInt().minus(1)}"
                productPriceTextview.text = "${getString(R.string.birr)} ${productPrice.times(qtyTextview.text.toString().toInt())}"
            }
            else Toast.makeText(
                requireContext(), "${getString(R.string.minimum_order_msg)} $minQty $unit ${getString(R.string.above_min_qty)}",
                Toast.LENGTH_LONG
            ).show()
        }


        buyNowBtn.setOnClickListener {
            dialog.dismiss()
            productViewmodel.prepareOrder(fullProductInfo.product.product!!._id!!,
                selectedSku?.value ?: productSkus?.getOrNull(0)?.value,
                productPrice.toInt(),
                qtyTextview.text.toString().toInt(),
                selectedSkuImage ?: (fullProductInfo.product.product?.gallery?.first { it.type == "image" }?.path
                        ?: ""),
                isfinal = true
            )

            (requireActivity() as MainActivity).movetoOrderFragment(
                productViewmodel.orderInfo!!,
                purchaseType = Product.STANDARD_PURCHASE,
                maxDeliveryDay = fullProductInfo.product.product?.avgDeliveryDay
            )
        }

        addToCartBtn.setOnClickListener {
            binding.productLoadingProgressbar.isVisible = true
            dialog.dismiss()
            productViewmodel.addToCart(fullProductInfo.product.product!!._id!!,
                selectedSku?.value,
                productPrice.toInt(),
                qtyTextview.text.toString().toInt(),
                selectedSkuImage ?: (fullProductInfo.product.product?.gallery?.first { it.type == "image" }?.path
                    ?: ""),
                Product.STANDARD_PURCHASE
            ).observe(viewLifecycleOwner) {
                binding.productLoadingProgressbar.isVisible = false
                mainactivityViewmodel.totalCartCounter.value = mainactivityViewmodel.totalCartCounter.value?.plus(1)
                if (it == true) {
                    Toast.makeText(requireContext(), getString(R.string.added_to_cart), Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()


    }

    @ExperimentalPagerApi
    @Composable
    fun productGallerSEction(){
        val galleryList by productGallery.observeAsState()
        if(!galleryList.isNullOrEmpty()){
            viewPagerComponent(Modifier.height(dimensionResource(R.dimen.product_gallery_height)) , imageList = galleryList!!){
                var bundle = bundleOf("VIDEO_URL" to it)
                findNavController().navigate(R.id.videoPlayerFragment , bundle)
            }
        }
    }

}