package com.komkum.komkum.ui.store.order

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.OrderedItem
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.databinding.FragmentCartListBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.component.imageComposable
import com.komkum.komkum.ui.theme.Yellow
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.showDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class CartListFragment : Fragment() {

    lateinit var binding : FragmentCartListBinding
    val orderViewmodel : OrderViewmodel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var emptyCart = false

    init {
        lifecycleScope.launchWhenCreated {
            orderViewmodel.getUserCart()
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
        binding = FragmentCartListBinding.inflate(inflater)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.cart))
        var pref = PreferenceHelper.getInstance(requireContext())
        pref[AccountState.IS_REDIRECTION] = true
        binding.composeView.setContent {
            ZomaTunesTheme(true) {
                cartScreen()
            }
        }
        var incompleteCart = pref.get(PreferenceHelper.CLEAR_CART , false)
        if(incompleteCart){
            orderViewmodel.clearCart().observe(viewLifecycleOwner) {
                pref[PreferenceHelper.CLEAR_CART] = false
                orderViewmodel.getUserCart()
            }
        }
        else{
            orderViewmodel.getUserCart()
        }


        orderViewmodel.cartInfo.observe(viewLifecycleOwner){ cartData ->
            binding.errorView.root.isVisible = false
            binding.errorTextview.isVisible = false
            if(cartData.isNullOrEmpty()){
                binding.errorTextview.text = getString(R.string.no_product_in_cart)
                binding.errorTextview.isVisible = true
            }
            else{
                binding.errorTextview.isVisible = false
                mainActivityViewmodel.totalCartCounter.value = cartData.size ?: 0

                //check if team purchase and standard purchase mixed together
                var isallteamPurchase = cartData.all { it.purchaseType == Product.MULTI_PRODUCT_TEAM_PURCHASE || it.purchaseType == Product.MULTI_PRODUCT_TEAM_PURCHASE }
                var isallNormalPurchase = cartData.all { it.purchaseType == Product.STANDARD_PURCHASE }
//                if(isallteamPurchase || isallNormalPurchase){
//                    //no action needed
//                }
//                else {
//                    requireActivity().showDialog(getString(R.string.warning) ,
//                        getString(R.string.team_purchase_mixed_with_standard_purchase) , isBottomSheet = true ,
//                        positiveButtonText = getString(R.string.i_know_continue)){}
//                }

                binding.proccedToCheckoutCardview.setOnClickListener {
                    binding.cartListProgressbar.isVisible = true
                    orderViewmodel.getOrderInformation().observe(viewLifecycleOwner){
                        binding.cartListProgressbar.isVisible = false

                        var maxDeliveryDay = cartData.map { it.product?.avgDeliveryDay ?: 1 }.maxByOrNull { it }
                        if(it != null) (requireActivity() as MainActivity).movetoOrderFragment(it , maxDeliveryDay = maxDeliveryDay)
//                        else Toast.makeText(requireContext() , "Error occurred" , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }




        orderViewmodel.getError().observe(viewLifecycleOwner){}
        orderViewmodel.error.observe(viewLifecycleOwner){
            binding.cartListProgressbar.isVisible = false
            binding.proccedToCheckoutCardview.isVisible = false
            emptyCart = true
            requireActivity().invalidateOptionsMenu()
            binding.errorTextview.isVisible = true
            binding.errorView.nonMainError.visibility = View.INVISIBLE
            binding.errorView.tryagainBtn.setOnClickListener {
                binding.errorView.root.isVisible = false
                binding.cartListProgressbar.isVisible = true
                orderViewmodel.getUserCart()
            }
            binding.errorView.root.isVisible = true
            it?.handleError(requireContext() , {orderViewmodel.removeOldError()},
                signupSource = mainActivityViewmodel.signUpSource){
                orderViewmodel.removeOldError()
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(emptyCart) menu.removeItem(R.id.remove_cart_menu_item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu ,  menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            R.id.remove_cart_menu_item ->{
                (requireActivity() as MainActivity).showDialog(getString(R.string.clear_cart) , null , getString(R.string.yes) , showNegative = true){
                    binding.cartListProgressbar.isVisible = true
                    orderViewmodel.clearCart().observe(viewLifecycleOwner){
                        binding.cartListProgressbar.isVisible = false
                        binding.proccedToCheckoutCardview.isVisible = false

                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    @Composable
    fun cartScreen(){
        val cart by orderViewmodel.cartInfo.observeAsState()
        cart?.let {cartItems ->
            binding.cartListProgressbar.isVisible = false
            if(cartItems.isNotEmpty()){
                binding.proccedToCheckoutCardview.isVisible = true
            }
            else {
                emptyCart = true
                requireActivity().invalidateOptionsMenu()
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 56.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LazyColumn(
                    Modifier.background(colorResource(id = R.color.light_secondaryLightColor)),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)){
                    itemsIndexed(items = cartItems){key , item ->
                        var productQty by rememberSaveable{ mutableStateOf(item.qty ?: 1)}
                        var message = item.sku?.entries?.joinToString("\n") { (key , value) -> "$value" } ?: ""
                        var minimumQty = if(item.purchaseType == Product.PACKAGE_PURCHASE) (item.product?.minQty ?: 1).plus(item.additionalQty)
                        else item.product?.minQty ?: 1

                        cartListItem(price = item.price!!, image = item.image!! , msg = message , qty = productQty , minQty = minimumQty, onQtyClick = { action , qty ->
                            if(action == "REMOVE" && qty <= 0){ }
                            else{
                                binding.cartListProgressbar.isVisible = true
                                var newOrderInfo = OrderedItem(item._id , product = item.product!!._id!! , qty = qty  , srcLocation = item.srcLocation ,
                                    price = item.price , purchaseType = item.purchaseType , image = item.image, sku = item.sku , )
                                orderViewmodel.addToCart(newOrderInfo).observe(viewLifecycleOwner){
                                    binding.cartListProgressbar.isVisible = false
                                    if(it == true) productQty = qty
                                    else  Toast.makeText(context , "Unable to update quantity" , Toast.LENGTH_LONG).show()

                                    if(action == "REMOVE") mainActivityViewmodel.totalCartCounter.value = mainActivityViewmodel.totalCartCounter.value?.minus(1)
                                    else mainActivityViewmodel.totalCartCounter.value = mainActivityViewmodel.totalCartCounter.value?.plus(1)

                                }
                            }
                        }) {
                            binding.cartListProgressbar.isVisible = true
                            if(item.purchaseType != Product.MULTI_PRODUCT_TEAM_PURCHASE){
                                orderViewmodel.removeFromCart(item._id!!).observe(viewLifecycleOwner){
                                    binding.cartListProgressbar.isVisible = false
                                    orderViewmodel.removeItemFromCartList(item)
                                }
                            }
                            else{
                                binding.cartListProgressbar.isVisible = false
                                requireActivity().showDialog(getString(R.string.error) , getString(R.string.remove_cart_message) , getString(R.string.ok)){}
                            }
                        }




                        if(key == cartItems.size -1){
                            var productIdsInCart = cartItems.map { it.product?._id }.filterNotNull().joinToString(",")
                            val relatedProducts by orderViewmodel.getRelatedProductsInCart(productIdsInCart).observeAsState()
                            Box(modifier = Modifier
                                .background(colorResource(id = R.color.light_secondaryLightColor))
                                .padding(top = 24.dp)
                                .fillMaxWidth()
                                .heightIn(max = 1000.dp)){
                                Column(Modifier.padding(8.dp) , verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = stringResource(R.string.product_you_might_like) , fontWeight = FontWeight.Bold ,  fontSize = dimensionResource(id = R.dimen.body_1).value.sp)
                                    if(relatedProducts == null) CircularProgressIndicator( modifier = Modifier.size(20.dp , 20.dp) , strokeWidth = 2.dp)
                                    relatedProducts?.let {
                                        if(it.isNotEmpty()){
                                            LazyRow{
                                                itemsIndexed(it){index, item ->
                                                    relatedProductForCartListItem(item){productId ->
                                                        (requireActivity() as MainActivity).movetoProductDetails(productId)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }




//                        productWthQuantityComposable(title = item.product!!.title!! , price = item.price!!,
//                            image = item.image!! , msg = message , qty = productQty , minQty = item.product?.minQty,
//                            onQtyClick = { action , qty ->
//                                if(action == "REMOVE" && qty <= 0){ }
//                                else{
//                                    binding.cartListProgressbar.isVisible = true
//                                    var newOrderInfo = OrderedItem(item._id , product = item.product!!._id!! , qty = qty  , srcLocation = item.srcLocation ,
//                                        price = item.price , purchaseType = item.purchaseType , image = item.image, sku = item.sku , )
//                                    orderViewmodel.addToCart(newOrderInfo).observe(viewLifecycleOwner){
//                                        binding.cartListProgressbar.isVisible = false
//                                        if(it == true) productQty = qty
//                                        else  Toast.makeText(context , "Unable to update quantity" , Toast.LENGTH_LONG).show()
//
//                                        if(action == "REMOVE") mainActivityViewmodel.totalCartCounter.value = mainActivityViewmodel.totalCartCounter.value?.minus(1)
//                                        else mainActivityViewmodel.totalCartCounter.value = mainActivityViewmodel.totalCartCounter.value?.plus(1)
//
//                                    }
//                                }
//                            }){
//                            binding.cartListProgressbar.isVisible = true
//                            if(item.purchaseType != Product.MULTI_PRODUCT_TEAM_PURCHASE){
//                                orderViewmodel.removeFromCart(item._id!!).observe(viewLifecycleOwner){
//                                    binding.cartListProgressbar.isVisible = false
//                                    orderViewmodel.removeItemFromCartList(item)
//                                }
//                            }
//                            else{
//                                binding.cartListProgressbar.isVisible = false
//                                requireActivity().showDialog(getString(R.string.error) , getString(R.string.remove_cart_message) , getString(R.string.ok)){}
//                            }
//                        }
                    }
                }


            }
        }
    }



    @Composable
    fun relatedProductForCartListItem(product: Product , onClick: (productId : String) -> Unit){
        var image = product.gallery?.firstOrNull { it.type == "image" }?.path
        Column(
            Modifier
                .width(dimensionResource(R.dimen.vertical_product_list_item_size))
                .padding(12.dp)) {
            imageComposable(image = image , placeholder = R.drawable.product_placeholder ,
            modifier = Modifier
                .height(dimensionResource(R.dimen.vertical_product_list_item_size))
                .fillMaxWidth()
                .aspectRatio(1f)
            )
            Text(text = product.title!! , fontSize = dimensionResource(id = R.dimen.caption).value.sp ,
                maxLines = 2 , overflow = TextOverflow.Ellipsis)
            Text(text = "${getString(R.string.birr)} ${product.dscPrice?.roundToInt()} - ${getString(R.string.birr)} ${product.stdPrice?.roundToInt()}" , fontSize = dimensionResource(id = R.dimen.caption2).value.sp , fontWeight = FontWeight.Bold)
            OutlinedButton(
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                onClick = { onClick(product._id!!)},
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(50), // = 50% percent
                // or shape = CircleShape
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Yellow , backgroundColor = Color.White)
            ){
                Text(text = stringResource(id = R.string.add_to_cart) ,  fontSize = dimensionResource(id = R.dimen.overline).value.sp , textAlign = TextAlign.Center)
            }
        }
    }

    @Composable
    fun cartListItem(price: Int, image: String, msg: String = "", qty: Int = 1, minQty : Int = 1,
                     onQtyClick: (action: String, qty: Int) -> Unit, onRemove: () -> Unit){

        var currentQt = qty
        var netPrice = price.times(qty)

        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                imageComposable(image = image , placeholder = R.drawable.product_placeholder,
                    modifier = Modifier
                        .size(50.dp, 50.dp)
                        .aspectRatio(1f)
                )
//            Text(text = msg,
////                modifier = Modifier.width(24.dp) ,
//                maxLines = 2, overflow = TextOverflow.Ellipsis ,
//                fontSize = dimensionResource(id = R.dimen.caption2).value.sp)

                Row(
                    Modifier
                        .background(colorResource(id = R.color.light_secondaryColor))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically){
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                        contentDescription = "",  modifier = Modifier.clickable {
                            if (currentQt > minQty){
                                currentQt -= 1
                                onQtyClick("REMOVE", currentQt)
                            }
                            else Toast.makeText(context , "${getString(R.string.minimum_order_msg)} $minQty ${getString(R.string.above_min_qty)}" , Toast.LENGTH_LONG).show()
                        })
                    Text(
                        text = "$qty", fontSize = 13.sp,
                        modifier = Modifier
                            .padding(8.dp)
                            .defaultMinSize(minWidth = 25.dp),
                        fontWeight = FontWeight.Bold ,  textAlign = TextAlign.Center
                    )
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_add_24),
                        contentDescription = "",  modifier = Modifier.clickable {
                            currentQt += 1
                            onQtyClick("ADD", currentQt)
                        })
                }

                Text(
                    text = "ETB $netPrice", fontSize = 13.sp,
                    modifier = Modifier
                        .widthIn(min = 60.dp)
                        .padding(8.dp) ,
                    fontWeight = FontWeight.Bold ,  textAlign = TextAlign.Start
                )

                Icon(painter = painterResource(id = R.drawable.ic_close_black_48dp),
                    contentDescription = "",  modifier = Modifier
                        .size(25.dp, 25.dp)
                        .clickable {
                            onRemove()
                        })
            }


            Divider(Modifier.padding(8.dp), color = Color.LightGray, thickness = 1.dp)

        }


    }

}