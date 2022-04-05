package com.komkum.komkum.ui.store.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.logEvent
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.databinding.FragmentOrderBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.ui.component.imageComposable
import com.komkum.komkum.ui.theme.Yellow
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.PaymentMethodAdapter
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RadioSpan
import dagger.hilt.android.AndroidEntryPoint

import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class OrderFragment : Fragment() {

    lateinit var binding : FragmentOrderBinding
    val mainactivityViewmodel : MainActivityViewmodel by activityViewModels()
    val orderViewmodel : OrderViewmodel by viewModels()

    var orderInfo : Order<String , Address>? = null
    var purchaseType : Int = Product.STANDARD_PURCHASE
    var totalPrice : Int = 0
    var shippingAndDeliveryInfo : ShippingAddressInfo? = null
    var selectedAddress : Address? = null

    var totalDiscountedAmount = MutableLiveData(0)

    var totalAmount = MutableLiveData(0)// used to send actual order price to server
    var totalAmountDisplayToUser = MutableLiveData(0)  // total amount showed to user , inlude discount made by discount codes

    var maxDeliveryDay = 1  // since product has different delivery day, select the max delivery day


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            orderInfo = it.getParcelable("ORDER")
            purchaseType = it.getInt("PURCHASE_TYPE")
            selectedAddress = it.getParcelable("SELECTED_ADDRESS") // IT HAS A VALUE WHEN WE CREATE NEW ADDRESS AND BACK TO THIS FRAGMENT
            maxDeliveryDay = it.getInt("MAX_DELIVERY_DAY")
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOrderBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.summary))
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        pref[AccountState.IS_REDIRECTION] = true

        totalAmountDisplayToUser.observe(viewLifecycleOwner){
            binding.totalAmountTextview.text = "${getString(R.string.birr)} $it"
        }

        totalDiscountedAmount.observe(viewLifecycleOwner){
            if(it > 0){
                binding.totalDiscountAmountTextview.text = "- ${getString(R.string.birr)} $it"
                binding.totalDiscountAmountTextview.isVisible = true
                binding.discountTextView.isVisible = true
            }
            else {
                binding.totalDiscountAmountTextview.isVisible = false
                binding.discountTextView.isVisible = false
            }
        }

        totalAmount.value = 0
        totalAmountDisplayToUser.value = 0



        orderInfo?.let { orderInf ->
            totalPrice = orderInf.totalPrice ?: 0
            var totalItems = orderInf.items!!.map { item -> item.qty }.reduce { acc, qty -> acc?.plus(qty!!)}
            binding.totalItemTextview.text = "$totalItems ${getString(R.string.items)}"
            binding.totalPriceTextview.text = "${getString(R.string.birr)} ${orderInfo?.totalPrice}"
            totalAmount.value = totalAmount.value?.plus(orderInf.totalPrice!!)
            totalAmountDisplayToUser.value = totalAmountDisplayToUser.value?.plus(orderInf.totalPrice!!)
            var productIds = orderInf.items!!.map { item -> item.product }.joinToString(",")
            var addressId = selectedAddress?._id

            orderViewmodel.getShippingAndDeliveryInfo(productIds , addressId).observe(viewLifecycleOwner){
                binding.createOrderProgressbar.isVisible = false
                binding.errorTextview.isVisible = false

                it?.let {
                    shippingAndDeliveryInfo = it
//                    if(it.selectedAddress?.nearLocation != null){
//                        Toast.makeText(requireContext() , getString(R.string.not_exact_address) , Toast.LENGTH_SHORT).show()
//                    }

                    orderInf.address = it.selectedAddress

                    if(!shippingAndDeliveryInfo?.shippingAddresses.isNullOrEmpty()){
                        binding.discountCodeSectionComposeview.setContent {
                            ZomaTunesTheme {
                                var productIds = orderInf.items!!.joinToString(",") { it.product.toString() }
                                disountCodeSection(orderInf ,  userId , productIds){info -> }
                            }
                        }

                        binding.payBtn.isVisible = true
                        binding.payBtn.isEnabled = true
                        binding.defaultAddress.root.isVisible = true
                        selectedAddress = shippingAndDeliveryInfo!!.selectedAddress
                        var addressNamePhoneText = "${selectedAddress?.name} span ${selectedAddress?.phone}"
                        var spanner = Spanner(addressNamePhoneText).span("span", Spans.custom { RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin)) })
                        binding.defaultAddress.addressNamePhoneTextview.text = spanner
                        binding.defaultAddress.addressLocationTextview.text = "${selectedAddress?.city ?: ""} , ${selectedAddress?.address}"

                        requireActivity().getBalloon(getString(R.string.available_delivery_days) , "AVAILABLE_DELIVERY_DAYS" , lifeyCycleOwner = viewLifecycleOwner).showAlignBottom(binding.deliveryDateTextview)


                        // show average delivery date

                        var atleastOneTeamPurchaseAvailable = orderInf.items?.any { it.purchaseType != Product.STANDARD_PURCHASE }
                        if(atleastOneTeamPurchaseAvailable == true){
                            // delivery day will be on every other day (max) for packages
                            var calendar = Calendar.getInstance()
                            var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                            var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                            when(dayOfWeek){
                                Calendar.TUESDAY, Calendar.THURSDAY ->{
                                    if(hourOfDay > 12)
                                        binding.deliveryDateTextview.text = "${getString(R.string.with_in)} 2 ${getString(R.string.days)}"

                                    else binding.deliveryDateTextview.text = getString(R.string.today)
                                }
                                Calendar.SATURDAY -> {
                                    if(hourOfDay > 12)
                                        binding.deliveryDateTextview.text = "${getString(R.string.with_in)} 3 ${getString(R.string.days)}"

                                    else binding.deliveryDateTextview.text = getString(R.string.today)
                                }
                                else -> binding.deliveryDateTextview.text = getString(R.string.next_day_delivery)
                            }

                        }
                        else { // normal delivery schedule
                            var totalDeliveryTime = it.deliveryTiime?.plus(maxDeliveryDay)
                            binding.deliveryDateTextview.text = if(maxDeliveryDay == 1) getString(R.string.next_day_delivery)
                            else "${getString(R.string.with_in)} $totalDeliveryTime ${getString(R.string.days)}"
                        }



                        // showing delivery price

                        if(it.deliveryPrice != null &&  it.deliveryPrice!! > 0){
                            var deliveryPrice = it.deliveryPrice!!
                            binding.deliveryPriceTextview.text = "${getString(R.string.birr)} $deliveryPrice"

                            if(atleastOneTeamPurchaseAvailable == true){
                                deliveryPrice = (it.deliveryPrice!!.times(50)).div(100)
                                binding.deliveryPriceTextview.text = "${getString(R.string.birr)} $deliveryPrice"
                                binding.orderDiscountTextview.isVisible = true
                                binding.orderDiscountTextview.text = getString(R.string.team_purchase_delivery_discount)
                            }

                            // vat price
//                            var vatPrice = (orderInf.totalPrice?.plus(deliveryPrice ?: 0)?.times(15)?.div(100))
//                            binding.vatPriceTextview.text = "${getString(R.string.birr)} ${vatPrice ?: 15}"
//
                            totalAmount.value = totalAmount.value?.plus(deliveryPrice)
                            totalAmountDisplayToUser.value = totalAmountDisplayToUser.value?.plus(deliveryPrice)
                        }

                    }
                    else binding.createNewAddressBtn.isVisible = true
                }
//                var showCashbackDialog = pref.get(PreferenceHelper.CASHBACK_DIALOG , true)
//                if(showCashbackDialog){
//                    pref[PreferenceHelper.CASHBACK_DIALOG] = false
//                        (requireActivity() as MainActivity).showDialog("Cashback" , isBottomSheet = true,
//                            message = "You will get 10% cashback from your order when you use your wallet as payment method"){}
//                }

            }

            binding.payBtn.setOnClickListener {
                selectedAddress?.let {
                    if(totalAmountDisplayToUser.value!=null && totalAmountDisplayToUser.value!! > 0 && totalAmount.value!=null && totalAmount.value!! > 0)
                        showPaymentDialog(orderInf , totalAmount.value!! , totalAmountDisplayToUser.value!!)
                    else Toast.makeText(requireContext() , "Amount must be greater than 0" , Toast.LENGTH_LONG).show()
                }
            }



            binding.defaultAddress.root.setOnClickListener {
                (requireActivity() as MainActivity).moveToAddresList(orderInf , shippingAndDeliveryInfo?.shippingAddresses ?: emptyList() , maxDeliveryDay = maxDeliveryDay)
            }
            binding.createNewAddressBtn.setOnClickListener {
                (requireActivity() as MainActivity).moveToCreateAddress(orderInfo!! , emptyList())
            }

//            binding.discountEditTextview.doOnTextChanged { text, start, before, count ->
//                if(text.isNullOrBlank()) binding.applyDiscountBtn.visibility = View.INVISIBLE
//                else  binding.applyDiscountBtn.visibility = View.VISIBLE
//            }

        }

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
//            var isInbackStack = findNavController().isFragmentInBackStack(R.id.customizeProductFragment)
//            if(findNavController().isFragmentInBackStack(R.id.customizeProductFragment))
//                findNavController().popBackStack(R.id.customizeProductFragment , false)
//            else  findNavController().popBackStack(R.id.cartListFragment , false)
//        }

        orderViewmodel.getError().observe(viewLifecycleOwner){}
        orderViewmodel.error.observe(viewLifecycleOwner){
            it?.handleError(requireContext() , {
                orderViewmodel.removeOldError()
                mainactivityViewmodel.removeOldErrors()
               Snackbar.make(binding.orderContainer, getString(R.string.auth_required), Snackbar.LENGTH_INDEFINITE)
                   .setAction(getString(R.string.retry)) { (requireActivity() as MainActivity).movetoOrderFragment(orderInfo!! , purchaseType =  purchaseType , maxDeliveryDay = maxDeliveryDay) }.show()

            },
                signupSource = mainactivityViewmodel.signUpSource) {
                binding.createOrderProgressbar.isVisible = false
                if(orderInfo == null) binding.errorTextview.isVisible = true
                it?.let {
                    if(it.contains("You don't have enough balance") or it.contains("Insufficient wallet balance")){
                        requireActivity().showCustomDialog(getString(R.string.insufficient_wallet_balance),
                            getString(R.string.insufficient_wallet_balance_msg),
                            getString(R.string.recharge_wallet_msg),
                            getString(R.string.yes),
                            getString(R.string.no_thanks),
                            negetiveAction = {}
                        ){
                            findNavController().navigate(R.id.walletRechargeFragment)
                        }
                    }

                    else if(it.contains("There must be at least 25 birr left")){
                        requireActivity().showCustomDialog(getString(R.string.sorry),
                            getString(R.string.min_25_birr),
                            getString(R.string.recharge_wallet_msg),
                            getString(R.string.yes),
                            getString(R.string.no_thanks),
                            negetiveAction = {}
                        ){
                            findNavController().navigate(R.id.walletRechargeFragment)
                        }
                    }
                    else Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                var isInbackStack = findNavController().isFragmentInBackStack(R.id.customizeProductFragment)
                if(isInbackStack) findNavController().popBackStack(R.id.customizeProductFragment , false)
                else  findNavController().popBackStack(R.id.cartListFragment , false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showPaymentDialog(orderInfo : Order<String , Address> , totalAmount: Int , discountedAmount: Int){
        var walletBalance = 0
        var dialog = MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT))
        dialog.cornerRadius(literalDp = 14f).customView(R.layout.payment_method_custom_view , scrollable = true)
            .cancelOnTouchOutside(false)
        var interactionListener = object : IRecyclerViewInteractionListener<PaymentMethod> {
            override fun onItemClick(data: PaymentMethod, position: Int, option: Int?) {
                when(position){
                    0 -> {
                       dialog.dismiss()
                        binding.createOrderProgressbar.isVisible = true
                        orderInfo.totalPrice = totalAmount
                        orderInfo.paymentMethod = Payment.PAYMENT_METHOD_WALLET
                        orderViewmodel.placeOrder(orderInfo , Payment.PAYMENT_METHOD_WALLET).observe(viewLifecycleOwner){
                            binding.createOrderProgressbar.isVisible = false
                            mainactivityViewmodel.totalCartCounter.value = 0

                            //send order event to firebase
                            if(mainactivityViewmodel.purchasedFrom != null){
                                mainactivityViewmodel.firebaseAnalytics.logEvent(FcmService.F_EVENT_ORDER_COMPLETE){
                                    param(FcmService.F_EVENT_PARAM_PURCHASED_FROM , mainactivityViewmodel.purchasedFrom!!)
                                    mainactivityViewmodel.purchasedFrom = null
                                }
                                if(shippingAndDeliveryInfo?.selectedAddress?.nearLocation != null){
                                    mainactivityViewmodel.firebaseAnalytics.logEvent(FcmService.F_EVENT_ORDERWITH_NEAR_LOCATION , null)
                                    mainactivityViewmodel.purchasedFrom = null
                                }
                            }
                            it?.let { (requireActivity() as MainActivity).movetoOrderCompletedFragment(it) }
                        }
                    }
                    1 ->{
                        dialog.dismiss()
                        orderInfo.paymentMethod = Payment.PAYMENT_METHOD_CASH_ON_DELIVERY
//
                        var d = (requireActivity() as MainActivity).showDialog(getString(R.string.warning) ,
                            "50 ${getString(R.string.cash_on_delivery_warning_msg)}" , showNegative = true ,  positiveButtonText = getString(R.string.ok_order) , autoDismiss = false){
                            if(walletBalance >= 30){
                                binding.createOrderProgressbar.isVisible = true
                                orderInfo.totalPrice = totalAmount
                                orderViewmodel.placeOrder(orderInfo , Payment.PAYMENT_METHOD_CASH_ON_DELIVERY).observe(viewLifecycleOwner){
                                    binding.createOrderProgressbar.isVisible = false
                                    mainactivityViewmodel.totalCartCounter.value = 0
                                    //send order event to firebase
                                    if(mainactivityViewmodel.purchasedFrom != null){
                                        mainactivityViewmodel.firebaseAnalytics.logEvent(FcmService.F_EVENT_ORDER_COMPLETE){
                                            param(FcmService.F_EVENT_PARAM_PURCHASED_FROM , mainactivityViewmodel.purchasedFrom!!)
                                            mainactivityViewmodel.purchasedFrom = null
                                        }
                                        if(shippingAndDeliveryInfo?.selectedAddress?.nearLocation != null){
                                            mainactivityViewmodel.firebaseAnalytics.logEvent(FcmService.F_EVENT_ORDERWITH_NEAR_LOCATION , null)
                                            mainactivityViewmodel.purchasedFrom = null
                                        }
                                    }
                                    it?.let { (requireActivity() as MainActivity).movetoOrderCompletedFragment(it) }
                                }
                            }
                            else {
                                (requireActivity() as MainActivity).showDialog(getString(R.string.sorry) , getString(R.string.wallet_balance_below_30) ,
                                    positiveButtonText = getString(R.string.recharge_wallet) , showNegative = true){
                                    findNavController().navigate(R.id.walletRechargeFragment)
                                }
                            }
                        }

                    }
                }
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        // fetch wallet balance from server
        binding.createOrderProgressbar.isVisible = true
        orderViewmodel.getWalletBalance().observe(viewLifecycleOwner){
            binding.createOrderProgressbar.isVisible = false
            walletBalance = it?.roundToInt() ?: 0
            var paymentmethods = listOf(
                PaymentMethod(getString(R.string.wallet) , "${getString(R.string.balance)}    ${getString(R.string.birr)} $walletBalance" ,
                    R.drawable.ic_baseline_wallet_gray_24
                ),
                PaymentMethod(getString(R.string.cash_on_delivery) , getString(R.string.cash_on_delivery_description) , R.drawable.ic_baseline_money_off_24)
            )

            var info = RecyclerViewHelper(interactionListener = interactionListener , owner = viewLifecycleOwner)
            var paymentAdapter = PaymentMethodAdapter(info , paymentmethods)

            var customview =  dialog.getCustomView()

            var totalPriceTextview = customview.findViewById<TextView>(R.id.total_price_tview)
            var paymentMethodsRecyclerview = customview.findViewById<RecyclerView>(R.id.payment_method_recyclerview)

            totalPriceTextview.text = "${getString(R.string.birr)} $discountedAmount"
            paymentMethodsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            paymentMethodsRecyclerview.adapter = paymentAdapter

            dialog.show()
        }
    }


    fun orderWithWallet(orderInfo : Order<String , Address>, totalAmount: Int , discountedAmount: Int){
        var message = "Make a payment from your wallet for this purchase"
        binding.createOrderProgressbar.isVisible = true
        orderViewmodel.getWalletBalance().observe(viewLifecycleOwner){
            binding.createOrderProgressbar.isVisible = false
            if(it != null){
                requireContext().showCustomDialog(
                    title = "ETB $discountedAmount",
                    message = message,
                    actionText = "Wallet balance - ETB $it",
                    pos_action_String = "Pay",
                    neg_action_String = "Cancel",
                    negetiveAction = {}
                ){
                    binding.createOrderProgressbar.isVisible = true
                    orderInfo.totalPrice = totalAmount
                    orderInfo.paymentMethod = Payment.PAYMENT_METHOD_WALLET
                    orderViewmodel.placeOrder(orderInfo , Payment.PAYMENT_METHOD_WALLET).observe(viewLifecycleOwner){
                        binding.createOrderProgressbar.isVisible = false
                        it?.let { (requireActivity() as MainActivity).movetoOrderCompletedFragment(it) }
                    }
                }
            }

        }

    }



    @Composable
    fun disountCodeSection(orderInfo : Order<String, Address> , userId : String , productIds : String , onclick : (discountInf : GiftViewData) -> Unit){
        Box(
            Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .heightIn(max = 1000.dp)
                .background(colorResource(id = R.color.light_secondaryLightColor))
//            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val availableCode by orderViewmodel.getDiscountCodesForProducts(productIds).observeAsState()

            var selectedDiscountCodeIndex by remember { mutableStateOf(-1)}
            var discountApplied  by remember{ mutableStateOf(false)}

            var couponCode by remember { mutableStateOf("")}
            Column {
                Row(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                    OutlinedTextField(value = couponCode,
                        singleLine = true, onValueChange = {couponCode = it},
                        placeholder = { Text(text = stringResource(R.string.enter_coupon_code))})

                    Button(
                        onClick = {
                            applyDiscountCode(orderInfo , couponCode ,
                                onfailure = {
                                    binding.createOrderProgressbar.isVisible = false
                                    requireActivity().showDialog("Error" , getString(R.string.discount_code_not_valid)){}
                                }
                            ){discountAmount ->
                                totalAmountDisplayToUser.value = totalAmountDisplayToUser.value?.minus(discountAmount ?: 0)
                                totalDiscountedAmount.value = totalDiscountedAmount.value?.plus(discountAmount ?: 0)
                                orderInfo.discountCodes.add(couponCode)
                                selectedDiscountCodeIndex = availableCode?.indexOfFirst { cp -> cp.code.value == couponCode} ?: -1
                                availableCode?.getOrNull(selectedDiscountCodeIndex)?.used = true
                                requireActivity().showDialog(getString(R.string.congrats) , "${getString(R.string.you_got)} ${getString(R.string.birr)} $discountAmount ${getString(R.string.discount_from_your_order)}"){}
                                couponCode = ""
                                discountApplied = true
                            }
                        },
                        enabled = couponCode.length > 0
                    ){
                        Text(text = stringResource(id = R.string.apply) , modifier = Modifier.padding(top = 4.dp , bottom = 4.dp))
                    }

                }
                Row(Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically){
                    if(availableCode == null){
                        CircularProgressIndicator( modifier = Modifier.size(20.dp , 20.dp) , strokeWidth = 4.dp)
                        TextButton(contentPadding = PaddingValues(0.dp) , onClick = { /*TODO*/ }) {
                            Text(text = stringResource(R.string.finding_discount_code) ,
                                fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                        }
                    }
                    else if(availableCode!!.isEmpty()){
                        TextButton(contentPadding = PaddingValues(0.dp) , onClick = { /*TODO*/ }) {
                            Text(text = stringResource(R.string.no_discount_code_found) ,
                                fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                        }
                    }
                    else {
                        TextButton(contentPadding = PaddingValues(0.dp) , onClick = { /*TODO*/ }) {
                            Text(text = "${availableCode?.size ?: 0} ${stringResource(R.string.discount_code_available)}" ,
                                fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                        }
                    }

                }

                if(!availableCode.isNullOrEmpty()){
                    LazyColumn(Modifier.padding(8.dp)){
                        itemsIndexed(availableCode!!){index , value ->
                            discountApplied = orderInfo.discountCodes.contains(value.code.value)
                            discountCodeListItem(value , index , selectedDiscountCodeIndex , userId = userId , discountApplied){selectedIndex, info ->
                                if(info.code.owner == userId || info.type == Coupon.COUPON_TYPE_RECURRING){
                                    binding.createOrderProgressbar.isVisible = true
                                    if(info.code.value != null){
                                        applyDiscountCode(orderInfo , info.code.value!! , info.productId ,
                                            onfailure = {
                                                binding.createOrderProgressbar.isVisible = false
                                                selectedDiscountCodeIndex = -1
                                            }
                                        ){discountAmount ->
                                            totalAmountDisplayToUser.value = totalAmountDisplayToUser.value?.minus(discountAmount ?: 0)
                                            totalDiscountedAmount.value = totalDiscountedAmount.value?.plus(discountAmount ?: 0)
                                            orderInfo.discountCodes.add(info.code.value ?: "")

                                            availableCode!![index].used = true
                                            requireActivity().showDialog(getString(R.string.congrats) , "${getString(R.string.you_got)} ${getString(R.string.birr)} $discountAmount ${getString(R.string.discount_from_your_order)}"){}
                                            discountApplied = true
                                        }


//                                        orderViewmodel.applyDiscountCode(info.code.value!! , info.productId).observe(viewLifecycleOwner){giftInfo ->
//                                            binding.createOrderProgressbar.isVisible = false
//                                            if(giftInfo != null){
//                                                var selectedProduct = orderInfo.items?.find { it.product == giftInfo.product}
//                                                if(selectedProduct != null){
//                                                    if(!orderInfo.discountCodes.contains(info.code.value)){
//                                                        var discountAmount = (selectedProduct.price?.times(giftInfo.discount ?: 0))?.div(100)
//                                                        totalAmountDisplayToUser.value = totalAmountDisplayToUser.value?.minus(discountAmount ?: 0)
//                                                        var discountINfo = "${getString(R.string.discounted_amount)}    ${getString(R.string.birr)} $discountAmount"
//                                                        totalDiscountedAmount.value = totalDiscountedAmount.value?.plus(discountAmount ?: 0)
//                                                        orderInfo.discountCodes.add(info.code.value!!)
//                                                        discountApplied = true
//                                                        requireActivity().showDialog(getString(R.string.congrats) , "${getString(R.string.you_got)} ${getString(R.string.birr)} $discountAmount ${getString(R.string.discount_from_your_order)}"){}
//                                                    }
//                                                    else Toast.makeText(requireContext() , getString(R.string.discount_code_already_applied) , Toast.LENGTH_LONG).show()
//
//                                                }
//                                                else Toast.makeText(requireContext() , getString(R.string.discount_code_not_applied) , Toast.LENGTH_LONG).show()
//                                            }
//                                            else{
//                                                requireActivity().showDialog("Error" , getString(R.string.discount_code_not_valid)){}
//                                                selectedDiscountCodeIndex = -1
//                                            }
//                                        }
                                    }
                                    else{
                                        binding.createOrderProgressbar.isVisible = false
                                        selectedDiscountCodeIndex = -1
                                        Toast.makeText(requireContext() , getString(R.string.apply_discount_code) , Toast.LENGTH_LONG).show()
                                    }

                                }
                                else {
                                    selectedDiscountCodeIndex = -1
                                    Toast.makeText(requireContext() , "Buying discount code" , Toast.LENGTH_LONG).show()
                                }

                                selectedDiscountCodeIndex = selectedIndex
                            }
                        }
                    }
                }

            }


//            Row(Modifier.padding(8.dp).fillMaxWidth() ,
//                horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically){
//                TextField(textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp) ,value = "Other discount code", onValueChange = {})
//                OutlinedButton(onClick = { /*TODO*/ }) {
//                    Text(text = "Apply")
//                }
//            }
        }
    }


    fun applyDiscountCode(order : Order<String , Address> , code : String , productIds : String? = null , onfailure : () ->Unit , onSuccess : (discount : Int) -> Unit){
        binding.createOrderProgressbar.isVisible = true
        // restrict user from applying more than 2 discount code
        if(order.discountCodes.size >=2){
            requireActivity().showDialog(getString(R.string.error) , getString(R.string.coupon_code_restriction)){}
            onfailure()
        }

        else {
            orderViewmodel.applyDiscountCode(code , productIds).observe(viewLifecycleOwner){couponInfo ->
                binding.createOrderProgressbar.isVisible = false
                if(couponInfo != null){
                    //coupon applied for all products orders
                    if(couponInfo.type == Coupon.COUPON_TYPE_RECURRING && couponInfo.product == null){
                        //restrict user from using same discount code twice
                        if(order.discountCodes.contains(code)){
                            Toast.makeText(requireContext() , getString(R.string.discount_code_already_applied) , Toast.LENGTH_LONG).show()
                        }
                        else{
                            var discountAmount = (order.totalPrice?.times(couponInfo.discount ?: 0))?.div(100)
                            onSuccess(discountAmount ?: 0)
                        }

                    }
                    else if((couponInfo.type == Coupon.COUPON_TYPE_RECURRING && couponInfo.product != null) ||
                        (couponInfo.type == Coupon.COUPON_TYPE_ONE_TIME && couponInfo.product != null)){

                        var selectedProduct = order.items?.find { it.product == couponInfo.product}
                        if(selectedProduct != null){
                            if(order.discountCodes.contains(code)){
                                Toast.makeText(requireContext() , getString(R.string.discount_code_already_applied) , Toast.LENGTH_LONG).show()
                            }
                            else{
                                var discountAmount = (selectedProduct.price?.times(couponInfo.discount ?: 0))?.div(100)
                                onSuccess(discountAmount ?: 0)
                            }
                        }
                    }

                    else Toast.makeText(requireContext() , getString(R.string.discount_code_not_applied) , Toast.LENGTH_LONG).show()
                }
                else{
                    requireActivity().showDialog("Error" , getString(R.string.discount_code_not_valid)){}
                    onfailure()
                }
            }
        }


    }


    @Composable
    fun discountCodeForSale(title : String){
        Card(
            modifier = Modifier
                .clickable {}
                .fillMaxWidth(), elevation = 4.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)){
                    CircularProgressIndicator()
                    Text(text = title)
                }
                Icon(painter = painterResource(id = if(true) R.drawable.ic_keyboard_arrow_down_black_24dp
                else R.drawable.ic_round_keyboard_arrow_right_24),
                    contentDescription = ""  )
            }
        }
    }


    @Composable
    fun discountCodeListItem(discountINfo : GiftViewData, key : Int, selectedCodeIndex : Int = -1, userId : String, discountApplied : Boolean = false,
                             buyCode : (() -> Unit)? = null , useDiscountCode : (selectedIndex : Int, info : GiftViewData) -> Unit){
        Row(
            Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Row {
                Text(discountINfo.code.value ?: "",
                    modifier = Modifier.widthIn(min = 30.dp), fontSize = 16.sp)
            }


            Row(Modifier.fillMaxWidth(0.4f), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically){
                imageComposable(image = discountINfo.productImage , placeholder = R.drawable.product_placeholder ,
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.medium_icon_size))
                        .width(dimensionResource(R.dimen.medium_icon_size))
                        .aspectRatio(1f))
                Text(text = "${discountINfo.discountPercent}%")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically){
                if(discountINfo.used || discountApplied){
                    TextButton(contentPadding = PaddingValues(0.dp) , enabled = false , onClick = { /*TODO*/ }) {
                        Text(text = stringResource(R.string.discount_applied) , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                    }
                }
                else if(key == selectedCodeIndex){
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically){
                        CircularProgressIndicator(modifier = Modifier.size(16.dp , 16.dp) , strokeWidth = 2.dp)
                        if(discountINfo.code.owner == userId || discountINfo.type == Coupon.COUPON_TYPE_RECURRING){
                            TextButton(contentPadding = PaddingValues(0.dp) , onClick = { /*TODO*/ }) {
                                Text(text = stringResource(R.string.applying_discount_code) , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                            }
                        }
                        else{
                            TextButton(contentPadding = PaddingValues(0.dp) , onClick = { /*TODO*/ }) {
                                Text(text = stringResource(R.string.buying) , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                            }
                        }
                    }
                }
                else {
                    if(discountINfo.code.owner == userId || discountINfo.type == Coupon.COUPON_TYPE_RECURRING){
                        OutlinedButton(contentPadding = PaddingValues(0.dp) , onClick = {
                            useDiscountCode(key , discountINfo)
                        }) {
                            Text(text =  stringResource(R.string.use_discount_code)  , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                        }
                    }
                    else {
                        OutlinedButton(contentPadding = PaddingValues(0.dp) ,  onClick = {
                            buyCode?.let { it() }
                        }) {
                            Text(text =  stringResource(R.string.buy_discount_code) , fontSize = dimensionResource(id = R.dimen.caption2).value.sp , color = Yellow)
                        }
                    }

                }

//                Icon(painter = painterResource(id = R.drawable.ic_more_vert_black_24dp), contentDescription ="" )
            }
        }
    }



    fun initYenePay(amount : Double , name : String){
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        if(userId.isNotBlank()){
            orderViewmodel.initiateYenePay(amount , userId , name , requireContext()){
                pref["PAYMENT_FOR"] = PaymentManager.PAYMENT_FOR_PRODUCT_PURCHASE
            }
        }
        else Toast.makeText(requireContext() , "user id is null" , Toast.LENGTH_LONG).show()
    }

}