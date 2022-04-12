package com.komkum.komkum.ui.payment.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Transaction
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_COMMISSION
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_DEPOSIT
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_DONATION
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_PURCHASE
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_REWARD
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE
import com.komkum.komkum.data.model.Transaction.Companion.TRANSACTION_TYPE_WITHDRAWAL
import com.komkum.komkum.databinding.FragmentTransactionBinding
import com.komkum.komkum.ui.component.imageListItem
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.ui.user.UserViewModel
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.convertoLocalDate
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt


@ExperimentalFoundationApi
@AndroidEntryPoint
class TransactionFragment : Fragment() {
    lateinit var binding : FragmentTransactionBinding
    val userViewmodel : UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTransactionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.transactions))
        binding.transactionListComposeview.setContent {
            ZomaTunesTheme(true) {
                transactionListSection()
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


    @Composable
    fun transactionListSection(){
        val transactions by userViewmodel.getWalletTransactions().observeAsState()
        var sortedTransactions = transactions?.sortedByDescending { tr -> tr.created }
        if(!sortedTransactions.isNullOrEmpty()){
            binding.progressBar5.isVisible = false
            var groupedTransaction = groupTransactionByDate(sortedTransactions)

            LazyColumn(Modifier.fillMaxWidth() , contentPadding = PaddingValues(top = 16.dp , bottom = 32.dp)){
                for((key , value) in groupedTransaction){
                    stickyHeader{
                        Box( Modifier.fillMaxWidth().padding(4.dp)){
                            Text(text = value[0].created!!.convertoLocalDate(false , false),
                                fontWeight = FontWeight.Light , modifier = Modifier.padding(start = 8.dp) , color = Color.Gray)

                        }
                    }
                    items(value.sortedByDescending { it.created }){transaction ->
                        imageListItem(title = getTransactionTitle(transaction.type!!),
                            subtitle = transaction.description ?: "",
                            image = R.drawable.ic_baseline_payment_24.toString(),
                            placeholder = if (amountStatus(transaction.type!!) == "+") R.drawable.ic_outline_arrow_downward_24
                            else R.drawable.ic_baseline_arrow_upward_24,
                            message = "ETB ${transaction.amount!!.roundToInt()}") {
                            showFullTransactionInfo(transaction)
                        }
                    }
                }
            }
        }
    }

    fun groupTransactionByDate(transactions : List<Transaction>) =  transactions.groupBy { tr -> tr.created!!.date }

    fun getTransactionTitle(type : Int) : String{
        return when(type){
            TRANSACTION_TYPE_DEPOSIT -> getString(R.string.deposit)
            TRANSACTION_TYPE_WITHDRAWAL -> getString(R.string.cash_out)
            TRANSACTION_TYPE_DONATION -> getString(R.string.donations)
            TRANSACTION_TYPE_PURCHASE -> getString(R.string.purchase)
            TRANSACTION_TYPE_REWARD -> getString(R.string.reward)
            TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE -> getString(R.string.subscription_upgrade)
            TRANSACTION_TYPE_COMMISSION -> getString(R.string.commission)
            else -> ""
        }
    }
    fun amountStatus(type : Int) : String?{
        return when(type){
            TRANSACTION_TYPE_DEPOSIT, TRANSACTION_TYPE_REWARD , TRANSACTION_TYPE_COMMISSION -> "+"
            TRANSACTION_TYPE_WITHDRAWAL , TRANSACTION_TYPE_DONATION,TRANSACTION_TYPE_PURCHASE, TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE -> "-"
            else -> null
        }
    }

    fun showFullTransactionInfo(transaction : Transaction){
        var info = listOf(
            "${getString(R.string.amount)}   ${transaction.amount}",
            "${getString(R.string.type)}   ${getTransactionTitle(transaction.type!!)}",
            "${transaction.description}",
            "${getString(R.string.source)}   ${transaction.sourceName}",
            "${getString(R.string.recipient)}   ${transaction.recepientName}",
            "${getString(R.string.created)}   ${transaction.created?.convertoLocalDate(false , false)}"
        )
        var dialog = MaterialDialog(requireContext()).show {
            title(text = getString(R.string.transaction_details))
            listItems(items = info)
            positiveButton(text = "OK")
        }
    }

}
