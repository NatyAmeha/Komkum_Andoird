package com.komkum.komkum.ui.store.order

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.ui.res.stringResource
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentOrderDetailBinding
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class OrderDetailFragment : Fragment() , IRecyclerViewInteractionListener<OrderedItem<Product>> {

    lateinit var binding : FragmentOrderDetailBinding
    val orderViewmodel : OrderViewmodel by viewModels()

    var orderId : String? = null
    var loadType : Int? = -1
    var orderDetails = MutableLiveData<Order<Product,String>>()
    var popToStart : Boolean = false

    var isUnfulfilledOrder = false

    companion object{
        const val LOAD_FROM_API = 0
        const val LOAD_FROM_PREVIOUS_SCREEN = 1
    }

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                orderId = it.getString("ORDER_ID")
                loadType = it.getInt("LOAD_TYPE")
                popToStart = it.getBoolean("POP_TO_START")

                when(loadType){
                    LOAD_FROM_API -> orderDetails.value = orderViewmodel.getOrderDetails(orderId!!)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentOrderDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.order_detail))
        var orderInfo = RecyclerViewHelper(type = "ORDERED_PRODUCT" , interactionListener = this , owner = viewLifecycleOwner)
        binding.orderedItemInfo = orderInfo
        orderDetails.observe(viewLifecycleOwner){
            binding.errorTextview.isVisible = false
            it?.let {
                binding.orderDetailsProgressbar.isVisible = false
                binding.orderDetailsContainer.isVisible = true

                if(it.status == Order.STATUS_DELIVERED){
                    isUnfulfilledOrder = false
                    binding.mainOrderStatusTextview.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_circle_24, 0, 0, 0)
                    binding.mainOrderStatusTextview.text = getString(R.string.order_delivered)
                    binding.orderDeliveryDateTextview.text = getString(R.string.order_delivered)

                    binding.orderStatusCardview.setCardBackgroundColor(resources.getColor(R.color.primaryColor))
                }
                else if(it.status == Order.STATUS_CANCELLED){
                    binding.orderStatusCardview.setCardBackgroundColor(resources.getColor(R.color.red))
                    binding.mainOrderStatusTextview.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_error_outline_24, 0, 0, 0)
                    binding.mainOrderStatusTextview.text = getString(R.string.order_cancelled)
                    binding.orderDeliveryDateTextview.text = getString(R.string.order_cancelled)
                }
                else if(it.estDelTime?.getRemainingDay(requireContext()) != null){
                    binding.mainOrderStatusTextview.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_more_time_24, 0, 0, 0)
                    binding.mainOrderStatusTextview.text = getString(R.string.order_pending)
                    binding.orderStatusCardview.setCardBackgroundColor(resources.getColor(R.color.yellow))

                    var remainingDeliveryDays = it.estDelTime?.getRemainingDay(requireContext()) ?: getString(R.string.today)
                    var remainingDay = it.estDelTime?.getRemainingDayInNumber(requireContext()) ?: 0
                    binding.orderDeliveryDateTextview.text = when(remainingDay){
                        0 -> getString(R.string.today)
                        1 -> getString(R.string.next_day_delivery)
                        else  -> remainingDeliveryDays
                    }
                }
                else{
                    isUnfulfilledOrder = true
                    requireActivity().invalidateOptionsMenu()
                    binding.mainOrderStatusTextview.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_error_outline_24, 0, 0, 0)
                    binding.mainOrderStatusTextview.text = getString(R.string.delivery_date_has_expired)
                    binding.orderDeliveryDateTextview.text = getString(R.string.delivery_date_has_expired)
                    binding.orderStatusCardview.setCardBackgroundColor(resources.getColor(R.color.red))
                }

                binding.orderStatusTextview.text = when(it.status){
                    Order.STATUS_SELLER_STORE -> getString(R.string.in_seller_store)
                    Order.STATUS_IN_STORE -> getString(R.string.in_stock)
                    Order.STATUS_OUT_FOR_DELIVERY -> getString(R.string.out_for_delivery)
                    Order.STATUS_DELIVERED -> getString(R.string.order_delivered)
                    else -> getString(R.string.in_near_store)
                }



                binding.paymentTypeTextview.text = when(it.paymentMethod){
                    Payment.PAYMENT_METHOD_CASH_ON_DELIVERY -> "${getString(R.string.payment_method)}     ${getString(R.string.pay_on_delivery)}"
                    else -> "${getString(R.string.payment_method)}  ${getString(R.string.wallet_payment)}"
                }
                binding.orderedItemList = it.items


                var calendar = Calendar.getInstance()
                calendar.time = it.estDelTime
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                var oneDayAfter = calendar.time
                binding.deliveryCodeTextview2.text = it.code
                binding.orderPriceTextview.text = "${getString(R.string.birr)} ${it.totalPrice}"
                binding.itemCountTextview.text = "${it.items?.size ?: 0} ${getString(R.string.items)}"
                binding.discountIndicatorGroup.isVisible = it.discountApplied == true
                val cal = Calendar.getInstance()
                cal.time = it.date!!
                binding.orderDateTextview.text = "${getString(R.string.order_date)}  ${it.date?.convertoLocalDate(false , true)}"
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            when {
                findNavController().isFragmentInBackStack(R.id.orderListFragment) -> findNavController().popBackStack(R.id.orderListFragment , false)
                findNavController().isFragmentInBackStack(R.id.storeHomepageFragment) -> findNavController().popBackStack(R.id.storeHomepageFragment , false)
                findNavController().isFragmentInBackStack(R.id.teamFragment) -> findNavController().popBackStack(R.id.teamFragment , false)
                else -> findNavController().navigateUp()
            }
        }

        orderViewmodel.getError().observe(viewLifecycleOwner){}
        orderViewmodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                orderViewmodel.removeOldError()
                binding.orderDetailsProgressbar.isVisible = false
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
               if(orderViewmodel.orderDetails.value == null) binding.errorTextview.isVisible = true
            }
        }
    }

//    @Composable
//    fun orderedProductSection(orderedItems : List<OrderedItem<Product>>){
//        LazyColumn(
//            Modifier.fillMaxWidth().padding(8.dp),
//            contentPadding = PaddingValues(start = 8.dp , top = 16.dp , bottom = 16.dp)){
//            itemsIndexed(orderedItems){key , item ->
//                var image = item.image ?: ""
//                var orderTitle = item.product?.title ?: ""
//                var price = item.price
//                var message = "Qty  ${item.qty}"
//                productListItemComposable(onclick =  {}
//                    ,  title = orderTitle , price = price ?: 0 , image = image) {
//                    Text(text = message , maxLines = 1 ,  color = Color.Gray)
//                }
//            }
//        }
//
//    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(!isUnfulfilledOrder) menu.removeItem(R.id.contact_us)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                when {
                    findNavController().isFragmentInBackStack(R.id.orderListFragment) -> findNavController().popBackStack(R.id.orderListFragment , false)
                    findNavController().isFragmentInBackStack(R.id.storeHomepageFragment) -> findNavController().popBackStack(R.id.storeHomepageFragment , false)
                    findNavController().isFragmentInBackStack(R.id.teamFragment) -> findNavController().popBackStack(R.id.teamFragment , false)
                    else -> findNavController().navigateUp()
                }
                true
            }
            R.id.contact_us -> {
                var intent = Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:0921320005"))
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: OrderedItem<Product>, position: Int, option: Int?) {}
    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}