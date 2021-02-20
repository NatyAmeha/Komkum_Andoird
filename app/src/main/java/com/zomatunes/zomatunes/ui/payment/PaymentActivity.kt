package com.zomatunes.zomatunes.ui.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.OnboardingActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import kotlinx.android.synthetic.main.activity_payment.*

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        continue_btn.setOnClickListener {
            var preference = PreferenceHelper.getInstance(this)
            var yenepayPaymentFor = preference.get("PAYMENT_FOR" , -1)
            when(yenepayPaymentFor){
                PaymentManager.PAYMENT_FOR_SUBSCRIPTION -> {
                    var intent = Intent(this , OnboardingActivity::class.java)
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
            }
        }
    }
}