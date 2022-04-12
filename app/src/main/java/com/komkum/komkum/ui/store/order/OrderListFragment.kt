package com.komkum.komkum.ui.store.order

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Order
import com.komkum.komkum.data.model.Payment
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.databinding.FragmentOrderListBinding
import com.komkum.komkum.ui.component.imageComposable
import com.komkum.komkum.ui.component.productListItemComposable
import com.komkum.komkum.ui.theme.Primary
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderListFragment : Fragment() , IRecyclerViewInteractionListener<Order<Product,String>> {
    lateinit var binding : FragmentOrderListBinding
    val orderViewmodel : OrderViewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOrderListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.orders))
        orderViewmodel.getUserOrders()
        binding.composeview.setContent {
            ZomaTunesTheme(true) {
                orderListComposable()
            }
        }

        orderViewmodel.orderLists.observe(viewLifecycleOwner){
            binding.orderListProgressbar.isVisible = false
            binding.errorTextview.isVisible = false
            if(it.isNullOrEmpty()){
                binding.errorTextview.text = getString(R.string.no_order_found)
                binding.errorTextview.isVisible = true
            }
        }


        orderViewmodel.getError().observe(viewLifecycleOwner){}
        orderViewmodel.error.observe(viewLifecycleOwner){
            binding.orderListProgressbar.isVisible = false
            it.handleError(requireContext()){
               if(orderViewmodel.orderLists.value.isNullOrEmpty()) binding.errorTextview.isVisible = true
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


    override fun onItemClick(data: Order<Product, String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoOrderDetailsFragment(data._id!! , OrderDetailFragment.LOAD_FROM_API)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    @Composable
    fun orderListComposable(){
        val orderList by orderViewmodel.orderLists.observeAsState()
        if(!orderList.isNullOrEmpty()){
            var sortedOrders = orderList!!.sortedByDescending { order -> order.date }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.light_secondaryLightColor))
                    .padding(8.dp) ,
                verticalArrangement = Arrangement.spacedBy(16.dp)){
                itemsIndexed(sortedOrders){index, order ->
                    order.totalPrice?.let {
                        orderListitem(order)
                    }
                }
            }
        }
    }

    @Composable
    fun orderListitem(order : Order<Product , String>){
        var images = order.items?.map { it.image }?.filterNotNull()
        var paymentType = if(order.paymentMethod == Payment.PAYMENT_METHOD_CASH_ON_DELIVERY)
            getString(R.string.pay_on_delivery)
        else getString(R.string.wallet_payment)

        Column {
            Row(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable {
                        (requireActivity() as MainActivity).movetoOrderDetailsFragment(
                            order._id!!,
                            OrderDetailFragment.LOAD_FROM_API
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Box(
                    Modifier
                        .widthIn(min = 80.dp, max = 80.dp)
                        .heightIn(min = 40.dp, max = 40.dp)
                        .background(Color.Transparent)) {
                    if (images?.size == 1){
                        imageComposable(image = images.getOrNull(0) , placeholder = R.drawable.product_placeholder , modifier = Modifier
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            )
                        )
                    }
                    else if(images?.size == 2){
                        imageComposable(image = images.getOrNull(0) , placeholder = R.drawable.product_placeholder , modifier = Modifier
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            )
                        )
                        imageComposable(image = images.getOrNull(1) , placeholder = R.drawable.product_placeholder , modifier = Modifier
                            .padding(start = 24.dp)
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            ))
                    }
                    else if(images?.size == 3){
                        imageComposable(image = images.getOrNull(0) , placeholder = R.drawable.product_placeholder , modifier = Modifier
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            )
                        )
                        imageComposable(image = images.getOrNull(1) , placeholder = R.drawable.radio_placeholder , modifier = Modifier
                            .padding(start = 20.dp)
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            ))
                        imageComposable(image = images.getOrNull(2), placeholder = R.drawable.product_placeholder , modifier = Modifier
                            .padding(start = 32.dp)
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            ))
                    }
                    else{
                        imageComposable(image = images?.getOrNull(0) , placeholder = R.drawable.album_placeholder , modifier = Modifier
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            )
                        )
                        imageComposable(image = images?.getOrNull(1) , placeholder = R.drawable.radio_placeholder , modifier = Modifier
                            .padding(start = 20.dp)
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            ))
                        imageComposable(image = images?.getOrNull(2) , placeholder = R.drawable.product_placeholder , modifier = Modifier
                            .padding(start = 32.dp)
                            .size(
                                dimensionResource(id = R.dimen.medium_icon_size),
                                dimensionResource(id = R.dimen.medium_icon_size)
                            ))
                        Row(
                            Modifier
                                .padding(start = 40.dp)
                                .size(
                                    dimensionResource(id = R.dimen.medium_icon_size),
                                    dimensionResource(id = R.dimen.medium_icon_size)
                                )
                                .background(Color.LightGray),

                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center){
                            Text(text = "+ ${images?.size?.minus(3) ?: 1}")
                        }
                    }
                }
                Column(Modifier.widthIn(min = 150.dp),
                    horizontalAlignment = Alignment.Start) {
                    var remainingDay = order.estDelTime?.getRemainingDayInNumber(requireContext()) ?: 0
                    var orderStatusMsg = if(order.status == Order.STATUS_DELIVERED) "${getString(R.string.order_delivered)}"
                    else if(order.estDelTime?.getRemainingDay(requireContext()) == null){
                        stringResource(R.string.delivery_date_has_expired)
                    }
                    else if(remainingDay >= 2){
                        "${stringResource(R.string.arrivied_with_in)} ${order.estDelTime?.getRemainingDay(requireContext())}"
                    }
                    else if (remainingDay == 1){
                        "${stringResource(R.string.delivery_date)}    ${stringResource(R.string.next_day_delivery)}"
                    }

                    else if(remainingDay == 0){
                        "${stringResource(R.string.delivery_date)}    ${stringResource(R.string.today)}"
                    }

                    else stringResource(R.string.delivery_date_has_expired)

                    var color = if(order.status == Order.STATUS_DELIVERED) R.color.primaryColor
                    else if(remainingDay >= 0) R.color.yellow
                    else if(order.estDelTime?.getRemainingDay(requireContext()) == null) R.color.red
                    else R.color.red

                    Text(text = paymentType , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                    Text(text = "${getString(R.string.birr)} ${order.totalPrice}" , fontWeight= FontWeight.Bold , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)

                    Text(text = orderStatusMsg , fontWeight = FontWeight.Bold ,
                        color = colorResource(id = color) ,fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                }

                Column() {
                    Text(text = stringResource(R.string.code) , fontSize = dimensionResource(id = R.dimen.caption2).value.sp)
                    Text(text = order.code ?: "" , fontWeight = FontWeight.Bold , fontSize = dimensionResource(id = R.dimen.caption).value.sp)

                }
            }

            Divider(Modifier.padding(8.dp), color = Color.LightGray, thickness = 1.dp)
        }
       
    }
}