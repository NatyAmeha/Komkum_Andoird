package com.komkum.komkum.ui.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.databinding.ActivityPaymentBinding
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import dagger.hilt.android.AndroidEntryPoint


//@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {
    lateinit var binding : ActivityPaymentBinding

//    val activityViewmodel : MainActivityViewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueBtn.setOnClickListener {
            var preference = PreferenceHelper.getInstance(this)
            var yenepayPaymentFor = preference.get("PAYMENT_FOR" , -1)
            when(yenepayPaymentFor){
                PaymentManager.PAYMENT_FOR_SUBSCRIPTION -> {
                    var intent = Intent(this , MainActivity::class.java)
                    intent.putExtra("PAYMENT_FOR" , PaymentManager.PAYMENT_FOR_SUBSCRIPTION)
                    startActivity(intent)
                }
                PaymentManager.PAYMENT_FOR_AUDIOBOOK_PURCHASE ->{
                    var bookId = preference.get("BOOK_ID" , "")
                    var intent = Intent(this , MainActivity::class.java)
                    intent.putExtra("PAYMENT_FOR" , PaymentManager.PAYMENT_FOR_AUDIOBOOK_PURCHASE)
                    intent.putExtra("BOOK_ID" , bookId)
                    startActivity(intent)
                }
                PaymentManager.PAYMENT_FOR_EBOOK_PURCHASE -> {
                    var bookId = preference.get("BOOK_ID" , "")
                    var intent = Intent(this , MainActivity::class.java)
                    intent.putExtra("PAYMENT_FOR" , PaymentManager.PAYMENT_FOR_EBOOK_PURCHASE)
                    intent.putExtra("BOOK_ID" , bookId)
                    startActivity(intent)
                }

                PaymentManager.PAYMENT_FOR_WALLET_RECHARGE ->{
                    var rechargeAmount = preference.get("RECHARGE_AMOUNT" , -1)
                    var trId = preference.get("PAYMENT_IDENTIFIER" , "")
                    intent = Intent(this , MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("PAYMENT_FOR" , PaymentManager.PAYMENT_FOR_WALLET_RECHARGE)
                    intent.putExtra("RECHARGE_AMOUNT" , rechargeAmount)
                    intent.putExtra("PAYMENT_IDENTIFIER" , trId)
                    startActivity(intent)
                }

                PaymentManager.PAYMENT_FOR_PRODUCT_PURCHASE ->{

                }
            }
        }
    }
}