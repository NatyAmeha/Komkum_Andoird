package com.komkum.komkum.ui.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentCustomizeProductBinding
import com.komkum.komkum.ui.component.productListItemComposable
import com.komkum.komkum.ui.store.order.OrderViewmodel
import com.komkum.komkum.ui.theme.Red
import com.komkum.komkum.ui.theme.Yellow
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.showDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@AndroidEntryPoint
class CustomizeProductFragment : Fragment() {

    lateinit var binding: FragmentCustomizeProductBinding
    val productViewModel: ProductViewModel by viewModels()

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()
    val orderViewmodel: OrderViewmodel by viewModels()


    var productIdCsv: String? = null
    var purchaseType: Int = Product.STANDARD_PURCHASE
    var teamId: String? = null
    var additionalQty : Int = 0
    var productForCustomization: List<ProductMetadata<Product, String>>? = null

    var productsInCart = mutableSetOf<Int>()  // containt the index of product added to the cart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            productIdCsv = it.getString("PRODUCT_ID_CSV")
            purchaseType = it.getInt("PURCHASE_TYPE")
            teamId = it.getString("TEAM_ID")
            additionalQty = it.getInt("ADDITIONAL_QTY")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).hideBottomView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomizeProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar, getString(R.string.customize_product))

        productIdCsv?.let {
            productViewModel.getMetadataofProducts(it)
        }

        productViewModel.showBottomMenu.observe(viewLifecycleOwner) {
            binding.cartContainer.isVisible = it
        }

        binding.productCustomizeComposeView.setContent {
            ZomaTunesTheme {
                CustomizeProductScreen()
            }
        }

        binding.continueBuyingCardview.setOnClickListener {
            findNavController().popBackStack(R.id.storeHomepageFragment , false)
        }
        binding.goToCartCardview.setOnClickListener {
            findNavController().navigate(R.id.cartListFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!productForCustomization.isNullOrEmpty()) {
                when {
                    productsInCart.size <= 0 -> findNavController().navigateUp()
                    productForCustomization!!.size > productsInCart.size -> {
                        (requireActivity() as MainActivity).showDialog(
                            getString(R.string.warning),
                            getString(R.string.add_all_to_cart),
                            getString(R.string.discard),
                            showNegative = true
                        ) {
                            binding.customizeProgressbar.isVisible = true
                            orderViewmodel.clearCart().observe(viewLifecycleOwner) {
                                binding.customizeProgressbar.isVisible = false
                                findNavController().navigateUp()
                            }
                        }
                    }
                    else -> findNavController().navigateUp()
                }
            } else findNavController().navigateUp()
        }


        productViewModel.getErrors().observe(viewLifecycleOwner) {}
        productViewModel.error.observe(viewLifecycleOwner) {
            binding.customizeProgressbar.isVisible = false
            it?.handleError(requireContext(), { productViewModel.removeOldErrors() }) {
                if (productViewModel.productMetadataList.value != null)
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                else binding.errorTextview.isVisible = true
            }
        }
    }

    override fun onPause() {
        if (!productForCustomization.isNullOrEmpty()) {
            if (productsInCart.size > 0 && productForCustomization!!.size > productsInCart.size) {
                PreferenceHelper.getInstance(requireContext())[PreferenceHelper.CLEAR_CART] = true
                Toast.makeText(requireContext() , "on pause called" , Toast.LENGTH_LONG).show()
                orderViewmodel.clearCart().observe(viewLifecycleOwner) {}
            }
        }
        super.onPause()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun CustomizeProductScreen() {
        val productMetadatas by productViewModel.productMetadataList.observeAsState()
        productMetadatas?.let { metadatList ->
            productForCustomization = metadatList
            binding.customizeProgressbar.isVisible = false
            var context = LocalContext.current
            var activeIndex by rememberSaveable { mutableStateOf(0) }
            var productIndexAddedToCart by rememberSaveable { mutableStateOf(mutableSetOf<Int>()) }
            var canScroll by remember { mutableStateOf(false) }

            var listState = rememberLazyListState()
            var coroutineScope = rememberCoroutineScope()


            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.Transparent)
            ) {
                Column(
                    Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    LazyColumn(
                        state = listState, verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp)
                    ) {
                        itemsIndexed(items = metadatList) { key, prodMd ->
                            productViewModel.selectedQty = (prodMd.product?.minQty ?: 1).plus(additionalQty)
                            var productQty by rememberSaveable { mutableStateOf(productViewModel.selectedQty) }
                            var selectedSku by rememberSaveable {
                                mutableStateOf(
                                    prodMd.skuComb?.getOrNull(
                                        0
                                    )
                                )
                            }

                            var productPrice =
                                if (purchaseType == Product.STANDARD_PURCHASE)
                                    (prodMd.product!!.stdPrice!! + (selectedSku?.addPrice ?: 0))
                                else (prodMd.product!!.dscPrice!! + (selectedSku?.addPrice ?: 0))

                            if (activeIndex != key) {
                                HeaderSection(key, prodMd.product!!.title!!, activeIndex) {
                                    if (productIndexAddedToCart.contains(key)) activeIndex = key
                                    else Toast.makeText(
                                        requireContext(),
                                        requireContext().getString(R.string.add_to_cart_message),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }


                            AnimatedVisibility(visible = (activeIndex == key)) {
                                productCustomizeBody(
                                    prodMd = prodMd,
                                    price = productPrice.toInt(),
                                    selectedSku = selectedSku,
                                    qty = productQty,
                                    onQtyChage = { productQty = it },
                                    onSkuChange = { key, value ->
                                        var newSku = selectedSku?.value?.toMutableMap()
                                        newSku?.set(key, value)
                                        var newselectedSku =
                                            prodMd.skuComb?.find { skuComb -> skuComb.value == newSku }
                                        if (newselectedSku == null) {
                                            Toast.makeText(
                                                context,
                                                "Not found with $newSku",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            var altSku =
                                                prodMd.skuComb?.find { skuComb -> skuComb.value!![key] == value }
                                            if (altSku != null) selectedSku = altSku
                                        } else {
                                            selectedSku = newselectedSku
//                                            if(productQty >  newselectedSku.stock ?:0)
//                                            productQty = selectedSku?.stock ?: 0
                                            productQty =
                                                if (newselectedSku.stock ?: 0 >= (prodMd.product?.minQty ?: 1).plus(additionalQty)) (prodMd.product?.minQty ?: 1).plus(additionalQty)
                                                    ?: 1 else 0
                                        }
                                    }) {
                                    Column(
                                        Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {

                                        if ((selectedSku?.stock != null && selectedSku!!.stock!! > 0 && (prodMd.product?.minQty ?: 1).plus(additionalQty) <= selectedSku!!.stock ?: 0) || (selectedSku == null && prodMd.stock ?: -1 > 0 && (prodMd.product?.minQty ?: 1).plus(additionalQty) <= prodMd.stock ?: 0)) {

                                            OutlinedButton(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .fillMaxWidth()
                                                    .background(colorResource(id = R.color.light_secondaryDarkColor)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray  , backgroundColor = Color.White) ,
                                                onClick = {
                                                    binding.customizeProgressbar.isVisible = true
                                                    if (activeIndex == metadatList.size - 1)
                                                        productViewModel.addToCart(prodMd.product!!._id!!,
                                                            selectedSku?.value,
                                                            productPrice.toInt(),
                                                            productQty,
                                                            selectedSku?.images?.first()
                                                                ?: (prodMd.product?.gallery?.first { it.type == "image" }?.path
                                                                    ?: ""),
                                                            purchaseType,
                                                            teamId,
                                                            additionalQty,
                                                            true
                                                        )
                                                            .observe(viewLifecycleOwner) {
                                                                mainActivityViewmodel.totalCartCounter.value =
                                                                    mainActivityViewmodel.totalCartCounter.value?.plus(1)
                                                                productIndexAddedToCart.add(
                                                                    activeIndex
                                                                )
                                                                productsInCart.add(activeIndex)
                                                                binding.customizeProgressbar.isVisible =
                                                                    false
                                                                if (it == true) Toast.makeText(
                                                                    context,
                                                                    context.getString(R.string.added_to_cart),
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                    else {
                                                        productViewModel.addToCart(prodMd.product!!._id!!,
                                                            selectedSku?.value,
                                                            productPrice.toInt(),
                                                            productQty,
                                                            selectedSku?.images?.first()
                                                                ?: (prodMd.product?.gallery?.first { it.type == "image" }?.path
                                                                    ?: ""),
                                                            purchaseType,
                                                            teamId,
                                                            additionalQty
                                                        ).observe(viewLifecycleOwner) {
                                                                mainActivityViewmodel.totalCartCounter.value =
                                                                    mainActivityViewmodel.totalCartCounter.value?.plus(1)
                                                                productIndexAddedToCart.add(
                                                                    activeIndex
                                                                )
                                                                productsInCart.add(activeIndex)
                                                                binding.customizeProgressbar.isVisible =
                                                                    false
                                                                if (it == true) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        context.getString(R.string.added_to_cart),
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                    canScroll = true
                                                                    activeIndex += 1
                                                                }
                                                            }
                                                    }
                                                }) {
                                                Text(
                                                    text = requireContext().getString(R.string.add_to_cart),
                                                    fontSize = dimensionResource(R.dimen.subtitle).value.sp
                                                )
                                            }
                                            if (metadatList.size == 1) {
                                                Button(
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(id = R.color.primaryColor)  , backgroundColor = Color.White) ,

                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth(), onClick = {
                                                        productViewModel.prepareOrder(prodMd.product!!._id!!,
                                                            selectedSku?.value,
                                                            productPrice.toInt(),
                                                            productQty,
                                                            selectedSku?.images?.first()
                                                                ?: (prodMd.product?.gallery?.first { it.type == "image" }?.path
                                                                    ?: ""),
                                                            teamId,
                                                            purchaseType,
                                                            additionalQty,
                                                            true
                                                        )

                                                        (requireActivity() as MainActivity).movetoOrderFragment(
                                                            productViewModel.orderInfo!!,
                                                            purchaseType = purchaseType,
                                                            maxDeliveryDay = prodMd.product?.avgDeliveryDay
                                                        )
                                                    }) {
                                                    Text(
                                                        text = requireContext().getString(R.string.order_now),
                                                        fontSize = dimensionResource(id = R.dimen.subtitle).value.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            productQty = 0
                                            Button(
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .fillMaxWidth(),
                                                onClick = {
                                                    binding.customizeProgressbar.isVisible = true
                                                    productViewModel.addProductTowishlist(prodMd.product!!._id!!)
                                                        .observe(viewLifecycleOwner) {
                                                            binding.customizeProgressbar.isVisible =
                                                                false
                                                            Toast.makeText(
                                                                context,
                                                                requireContext().getString(R.string.added_to_list),
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                }) {
                                                Text(
                                                    text = stringResource(R.string.add_to_wishlist),
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (canScroll) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(activeIndex)
                                    canScroll = false
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    @Composable
    fun ProductSourceComposable(
        locationList: List<String>,
        selected: String,
        onclick: (selected: String) -> Unit
    ) {
        var context = LocalContext.current

        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Ship from", color = Color.White, fontSize = 20.sp)
            Row(
                Modifier
                    .background(Color.DarkGray)
                    .fillMaxWidth()
                    .clickable {
                        MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                            var items = listItems(items = locationList) { _, _, text ->
                                onclick(text.toString())
                            }
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = selected, fontSize = 20.sp)
                Icon(
                    tint = Color.White,
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down_black_24dp),
                    contentDescription = "",
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }


    @Composable
    fun QtySection(
        qty: Int,
        maxStock: Int = 1,
        unit: String? = null,
        minQty: Int = 1,
        onclick: (qty: Int) -> Unit
    ) {
        Row(
            Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var qtyUnit = if (unit != null) "( $unit )" else ""
            Text(
                text = "${requireContext().getString(R.string.qty)} $qtyUnit",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1
            )
            Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                    contentDescription = "", modifier = Modifier.clickable {
                        if (qty > minQty) {
                            var newQty = qty - 1
                            onclick(newQty)
                        } else Toast.makeText(
                            requireContext(),
                            "${getString(R.string.minimum_order_msg)} $minQty ${unit ?: ""} ${getString(R.string.above_min_qty)}",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                Surface(
                    shape = RoundedCornerShape(0.dp), modifier = Modifier
                        .padding(16.dp)
                        .background(MaterialTheme.colors.onSurface)
                ) {
                    Text(
                        text = "$qty", fontSize = 20.sp, modifier = Modifier
                            .padding(8.dp)
                            .defaultMinSize(minWidth = 25.dp), textAlign = TextAlign.Center
                    )
                }
                Icon(painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "", modifier = Modifier.clickable {
                        if (qty < maxStock) {
                            var newQty = qty + 1
                            onclick(newQty)
                        }
                    })

            }
        }

    }

    @Composable
    fun HeaderSection(
        key: Int,
        productName: String,
        selectedIndex: Int,
        onclick: (qty: Int) -> Unit
    ) {
        Card(
            modifier = Modifier
                .clickable { onclick(key) }
                .fillMaxWidth(), elevation = 2.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "${key + 1}", fontSize = 16.sp)
                    Text(text = productName)
                }
                Icon(
                    painter = painterResource(
                        id = if (key == selectedIndex) R.drawable.ic_keyboard_arrow_down_black_24dp
                        else R.drawable.ic_round_keyboard_arrow_right_24
                    ),
                    contentDescription = ""
                )
            }
        }
    }


    @Composable
    fun skuOptionSection(
        skuOptionKey: String,
        skuoptionValue: List<String>,
        selectedSku: Map<String, String>,
        onclick: (skuSelected: String) -> Unit
    ) {
        Column(
            Modifier
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = skuOptionKey,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            var selectedSkuValue = selectedSku[skuOptionKey]
            var index = skuoptionValue.indexOf(selectedSkuValue)

            var selectedIndex = if (index > -1) index else 0

            LazyRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(skuoptionValue) { index, optionValue ->
                    var boarderMOdifier = if (selectedIndex == index) Modifier.border(
                        1.dp,
                        colorResource(id = R.color.light_primaryLightColor),
                        RoundedCornerShape(1.dp)
                    )
                    else Modifier.border(1.dp, Color.Gray, RoundedCornerShape(1.dp))

                    Box(contentAlignment = Alignment.Center,
                        modifier = boarderMOdifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .clickable {
                                onclick(optionValue)
                            }

                    ) {
                        Text(text = optionValue, fontSize = 16.sp)

                    }
                }
            }
        }
    }

    //    @Preview
    @Composable
    fun productCustomizeBody(
        prodMd: ProductMetadata<Product, String>,
        price: Int,
        selectedSku: SKUCombination?,
        qty: Int,
        onQtyChage: (qty: Int) -> Unit,
        onSkuChange: (key: String, value: String) -> Unit,
        slot: @Composable () -> Unit
    ) {
        Column(
            Modifier
                .padding(top = 10.dp, bottom = 100.dp)
        ) {
            var productStock = selectedSku?.stock ?: prodMd.stock ?: 0
            var message = if (productStock in 1..9)
                "Only $productStock ${stringResource(R.string.available_in_store)}"
            else if (productStock in 1..99)
                "$productStock ${stringResource(R.string.available_in_store)}"
            else if (productStock <= 0) stringResource(R.string.currently_unavailable)
            else ""

            productListItemComposable(onclick = {},
                title = prodMd.product!!.title!!,
                price = price.times(qty),
                image = selectedSku?.images?.first()
                    ?: (prodMd.product?.gallery?.first { it.type == "image" }?.path ?: "")) {
                Column(Modifier.padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (prodMd.product?.refund == true) {
                        Text(
                            text = stringResource(id = R.string.return_eligible),
                            modifier = Modifier
                                .border(BorderStroke(1.dp, Yellow))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Light,
                            fontSize = dimensionResource(id = R.dimen.caption2).value.sp
                        )
                    }
                    Text(
                        text = "${stringResource(id = R.string.minimum_order)}   ${(prodMd.product?.minQty ?: 1).plus(additionalQty)} ${prodMd.product?.unit ?: ""}",
                        fontWeight = FontWeight.Light,
                        fontSize = dimensionResource(id = R.dimen.caption).value.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = message,
                        fontSize = dimensionResource(id = R.dimen.caption2).value.sp,
                        color = Red
                    )

                }
            }

            Column(
                Modifier
                    .background(colorResource(id = R.color.light_secondaryLightColor))
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                prodMd.skuOptions?.let {
                    if (it.isNotEmpty() && !selectedSku?.value.isNullOrEmpty()) {
                        for ((keyy, value) in it) {
                            skuOptionSection(keyy, value, selectedSku?.value!!) {
                                onSkuChange(
                                    keyy,
                                    it
                                )
                            }
                        }
                    }
                }


                QtySection(
                    qty,
                    selectedSku?.stock ?: prodMd.stock ?: 10,
                    prodMd.product?.unit ?: "",
                    (prodMd.product?.minQty ?: 1).plus(additionalQty)
                ) { onQtyChage(it) }

                slot()
            }


        }
    }

}