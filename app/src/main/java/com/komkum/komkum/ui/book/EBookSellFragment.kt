package com.komkum.komkum.ui.book

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.android.billingclient.api.BillingClient
import com.auth0.android.jwt.JWT
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.config.AppKey
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentEBookSellBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.ReviewAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.chip.Chip
import com.novoda.downloadmanager.DownloadBatchStatus
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.squareup.picasso.Picasso
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

@AndroidEntryPoint
class EBookSellFragment : Fragment() {
    lateinit var binding : FragmentEBookSellBinding

    val bookViewmodel : BookViewModel by viewModels()
    val downloadViewmodel : DownloadViewmodel by viewModels()

    var bookId : String? = null
    var loadFromCache : Boolean = false
    var eBook : EBook<Author<String, String>>? = null

    var paymentInfo : Payment? = null
    val PAYPAL_PAYMENT_CODE = 7777


    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            bookId = it.getString("BOOK_ID")
            loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
        }
        binding = FragmentEBookSellBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).showBottomViewBeta()

        binding.container.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if(scrollY < 100){
                binding.toolbar.title = ""
            }
            else if(scrollY > 500){
                binding.toolbar.title = binding.bookTitleTextview.text
            }
        }

        bookId?.let {
            bookViewmodel.isDownloaded(it).observe(viewLifecycleOwner , Observer{
                if(it){
                    binding.buyBookBtn.visibility = View.INVISIBLE
                    binding.regularPriceTextview.visibility = View.GONE
                    binding.upgradeSubscriptionBtn.visibility = View.GONE
                    binding.upgradeSubscriptionBtn.visibility = View.GONE
                    binding.startListeningBtn.visibility = View.VISIBLE
                }
            })

            bookViewmodel.getEbook(it)
            getSuggestions(it)
            bookViewmodel.eBookResult.observe(viewLifecycleOwner , Observer{
                it?.let {
                    if(it.promotion!!){
                        binding.buyBookBtn.visibility = View.INVISIBLE
                        binding.regularPriceTextview.visibility =View.GONE
                        binding.notifiyBookBtn.visibility = View.VISIBLE
                    }
                    if(it.audiobook.isNullOrBlank()) binding.audiobookFormatTextview.visibility = View.GONE
                    eBook = it
                    toControllerActivity().selectedEbookForPurchase = it
                    binding.bookLoadingProgressbar.visibility = View.GONE
                    binding.bookContainer.visibility = View.VISIBLE
                    configureActionBar(binding.toolbar)
                    binding.bookTitleTextview.text = it.name
                    binding.bookAuthorTextview.text = it.authorName?.joinToString(", ")
                    binding.durationTextview.text = it.length
                    Picasso.get().load(it.coverImagePath?.replace("localhost" , AdapterDiffUtil.URL)).fit().centerCrop().into(binding.bookCoverImageview)

                    binding.downloadAmountTextview.text = "${it.downloadCount} copy sold"
                    var ratingValue = it.rating?.map { rating -> rating.rating }
                    if(!ratingValue.isNullOrEmpty()){
                        var totalValue =  ratingValue.reduce { acc, rating -> acc.plus(rating) }
                        var averageRatingValue = totalValue.div(it.rating!!.size)
                        var df = DecimalFormat("#.#")
                        df.roundingMode = RoundingMode.CEILING
                        binding.ratingTextview.text = df.format(averageRatingValue)
                    }
                    binding.regularPriceTextview.text = "Regular Price ${it.priceInBirr} birr or $${it.priceInDollar}"

                    if(bookViewmodel.getPaymentInfo(it.priceInBirr!! , it.priceInDollar!!) != null){
                        paymentInfo = bookViewmodel.getPaymentInfo(it.priceInBirr!! , it.priceInDollar!!)
                        when(paymentInfo!!.type){
                            Payment.PAYMENT_TYPE_CREDIT -> {
                                binding.buyBookBtn.text =  "Buy for Free"
                                binding.readSampleBtn.isVisible = false
                            }
                            Payment.PAYMENT_TYPE_NO_CHARGE ->{
                                binding.buyBookBtn.text =  "Buy for Free"
                                binding.readSampleBtn.isVisible = false
                            }
                            else ->  binding.buyBookBtn.text =  "Buy for ${paymentInfo!!.priceInBirr} Birr or $${paymentInfo!!.priceInDollar}"
                        }
                    }
                    else{
                        paymentInfo = Payment(Payment.PAYMENT_TYPE_REGULAR , it.priceInBirr , it.priceInDollar)
                        binding.buyBookBtn.text =  "Buy for ${it.priceInBirr} Birr or $${it.priceInDollar}"
                    }

                    binding.bookDescriptionTextview.text = it.description
                    var chipGroup = binding.bookCategoryChipGroup
                    chipGroup.removeAllViews()
                    it.genre?.forEach { genre ->
                        var chip = Chip(requireContext())
                        chip.text = genre
                        chip.setOnClickListener {
                            var data = MusicBrowse(genre , "GENRE" , MusicBrowse.CONTENT_TYPE_BOOK , queryInfo = BrowseQueryInfo(genre = genre))
                            var bundle = bundleOf("BROWSE" to data)
                            findNavController().navigate(R.id.bookBrowseFragment , bundle)
                        }
                        chipGroup.addView(chip)
                    }
                    binding.languageTextview.text = it.language
                    binding.publisherTextview.text = it.publisher
                    val cal = Calendar.getInstance()
                    cal.time = it.releaseDate
                    binding.releaseDateTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())}  ${cal.get(Calendar.YEAR)}"

                    binding.writeReviewBtn.text = "Write review   (${it.rating?.size} Reviews)"

                    it.rating?.let {
                        var reviewAdapter = ReviewAdapter()
                        binding.bookRatingRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                        binding.bookRatingRecyclerview.adapter = reviewAdapter
                        reviewAdapter.submitList(it.sortedByDescending { rating ->rating.date })
                    }

                    arguments?.let {
                        var isFromPayment = it.getBoolean("FROM_PAYMENT" , false)
                        if(isFromPayment){
                            eBook?.let {
                                bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                                    if(it) (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                        var bundle = bundleOf("SELECTED_PAGE" to 2)
                                        findNavController().navigate(R.id.downloadListFragment , bundle)
                                    }
                                })
                            }
                        }
                    }
                }
            })
        }

        binding.startListeningBtn.setOnClickListener {
            eBook?.let { it1 -> continueReading(it1) }
        }

        binding.buyBookBtn.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                requireActivity().showDialog("Error" , "Unable to download this book because of your android version" , getString(R.string.ok)){}
                return@setOnClickListener
            }
            paymentInfo?.let {
                when(it.type){
                    Payment.PAYMENT_TYPE_NO_CHARGE ->{
                        eBook?.let {
                            bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                                if(it){
                                    (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                        var bundle = bundleOf("SELECTED_PAGE" to 2)
                                        findNavController().navigate(R.id.downloadListFragment , bundle)
                                    }
                                }
                            })
                        }
                    }
                    Payment.PAYMENT_TYPE_CREDIT ->  {
                        eBook?.let {
                            bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                                if(it){
                                    (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                        var bundle = bundleOf("SELECTED_PAGE" to 2)
                                        findNavController().navigate(R.id.downloadListFragment , bundle)
                                    }
                                    var pref = PreferenceHelper.getInstance(requireContext())
                                    var usedCredit = pref.get(AccountState.USED_AUDIOBOOK_CREDIT , 0)
                                    pref[AccountState.USED_AUDIOBOOK_CREDIT] = usedCredit +1
                                    requireActivity().invalidateOptionsMenu()
                                }
                            })
                        }
                    }
                    else -> {
                        var paymentmethods = listOf<GridItem>(
                            BasicGridItem(R.drawable.ic_yenepaylogo , "YenePay"),
//                            BasicGridItem(R.drawable.ic_paypal , "PayPal"),
                            BasicGridItem(R.drawable.google_logo , "Google Play")
                        )
                        MaterialDialog(requireContext() , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                            title(text = "Payment Method")
                            message(text = "Paymet deducted based on your payment method choice")
                            cornerRadius(14f)
                            cancelOnTouchOutside(true)
                            gridItems(items = paymentmethods){dialog, index, item ->
                                when(index){
                                    0 -> handleYenepayPayment(it , eBook!!._id!! , eBook!!.name!!)
//                                    1 -> paypalPaymentHandler(it , eBook!!.name!!)
                                    1 ->{
                                        var productId = "product_price_${eBook!!.priceInBirr?.toInt()}"
                                        handleGooglePlaybillingPUrchase(productId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        binding.bookAuthorTextview.setOnClickListener { moveToAuthor() }



        binding.readSampleBtn.setOnClickListener {
            eBook?.let {
                var ss ="http://www.africau.edu/images/default/sample.pdf"
                var bundle = bundleOf("BOOK_URL" to ss , "LOAD_FROM_CACHE" to false)
                var intent = Intent(requireContext() , BookReaderActivity::class.java)
                intent.putExtras(bundle)
                requireContext().startActivity(intent)
            }
        }

        binding.writeReviewBtn.setOnClickListener {
            eBook?.let {
                (requireActivity() as MainActivity).movetoCreateReview(it._id!! , Review.CONTENT_TYPE_EBOOK ,  it.coverImagePath!! , it.name!! , it.authorName!!.joinToString(","))
            }
        }

        binding.audiobookFormatTextview.setOnClickListener {
            var bundle = bundleOf("BOOK_ID" to eBook?.audiobook)
            findNavController().navigate(R.id.audiobookSellFragment , bundle)
        }

        bookViewmodel.getError().observe(viewLifecycleOwner){}
        bookViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.eBookErrorTextview.isVisible = true
                binding.bookLoadingProgressbar.isVisible = false
                binding.bookContainer.isVisible = false
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var preference = PreferenceHelper.getInstance(requireContext())
        var token = preference.get(AccountState.TOKEN_PREFERENCE, AccountState.INVALID_TOKEN_PREFERENCE_VALUE)
        var jwt = JWT(token)
        var subscriptionInfo =  jwt.claims.get("realSubscriptionInfo")?.asObject(SubscriptionPlan::class.java)
        subscriptionInfo?.let {
            if(it.level == SubscriptionPlan.SUBSCRIPTION_LEVEL_3){
                var usedCredit = preference.get(AccountState.USED_AUDIOBOOK_CREDIT , 0)
                var creditLeft = it.bookCredit?.minus(usedCredit)
                if (creditLeft != null) {
                    if(creditLeft < 0) menu.findItem(R.id.available_credit_menu_item).title = "0 Credit"
                    else menu.findItem(R.id.available_credit_menu_item).title = "$creditLeft Credit"
                }
            }
            else  menu.removeItem(R.id.available_credit_menu_item)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.book_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

            R.id.share_menu_item ->{
                eBook?.let {
                    ShareCompat.IntentBuilder.from(requireActivity())
                        .setChooserTitle("Share ${it.name}")
                        .setText("Check out ${it.name}on ZomaTunes. https://play.google.com/store/apps/details?id=com.komkum.komkum")
                        .setType("text/plain")
                        .startChooser()
                }
                true
            }
            R.id.available_credit_menu_item ->{
                var msg = "You have ${item.title} left to download either Ebook or Audiobook for free"
                requireContext().showDialog("Credits" , msg , "Ok" , null){}
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PAYPAL_PAYMENT_CODE){
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(requireContext() , "Payment Completed" , Toast.LENGTH_LONG).show()
                    eBook?.let {
                        bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                            if(it){
                                (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                    var bundle = bundleOf("SELECTED_PAGE" to 2)
                                    findNavController().navigate(R.id.downloadListFragment , bundle)
                                }

                                binding.buyBookBtn.visibility = View.INVISIBLE
                                binding.regularPriceTextview.visibility = View.GONE
                                binding.upgradeSubscriptionBtn.visibility = View.GONE
                                binding.upgradeSubscriptionBtn.visibility = View.GONE
                                binding.startListeningBtn.visibility = View.VISIBLE
                            }
                        })
                    }
                    //update user info
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(requireContext() , "Payment Cancelled" , Toast.LENGTH_LONG).show()
                }
                PaymentActivity.RESULT_EXTRAS_INVALID -> {
                    Toast.makeText(requireContext() , "Invalid Payment session" , Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            1-> {
                eBook?.let {
                    bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                        if(it) (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                            var bundle = bundleOf("SELECTED_PAGE" to 2)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                    })
                }
            }
        }
    }

    fun moveToAuthor(){
        if(eBook!!.author!!.size > 1){
            eBook!!.authorName?.showListDialog(requireContext() , "Choose Author"){index, value ->
                (requireActivity() as MainActivity).movetoAuthor(eBook!!.author!![index]._id!!)
            }
        }
        else (requireActivity() as MainActivity).movetoAuthor(eBook!!.author!![0]._id!!)
    }

    fun getSuggestions(bookId : String){
        var bookListener = object : IRecyclerViewInteractionListener<Book<String>> {
            override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
                var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
                findNavController().navigate(R.id.EBookSellFragment , bundle)
            }

            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }
        var info = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = bookListener , owner = viewLifecycleOwner,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.bookInfo = info
        binding.bookViewmodel = bookViewmodel
        bookViewmodel.getEbookSuggestionResult(bookId)
    }

    fun paypalPaymentHandler(info : Payment , bookName : String){
        var paypalServiceConfig = PayPalConfiguration().acceptCreditCards(true)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(AppKey.paypalClientId).merchantName("ZomaTunes")

        var serviceIntent = Intent(requireContext() , PayPalService::class.java)
        serviceIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION , paypalServiceConfig)
        requireActivity().startService(serviceIntent)

        var paymentInfo = PayPalPayment(info.priceInDollar!!.toBigDecimal() , "USD" , bookName , PayPalPayment.PAYMENT_INTENT_SALE)
        var paymentIntent = Intent(requireContext() , PaymentActivity::class.java)
        paymentIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION , paypalServiceConfig)
        paymentIntent.putExtra(PaymentActivity.EXTRA_PAYMENT , paymentInfo)
        startActivityForResult(paymentIntent , PAYPAL_PAYMENT_CODE)

    }

    fun handleYenepayPayment(paymentInfo : Payment , orderId : String , orderName : String){
        var preference = PreferenceHelper.getInstance(requireContext())
        preference.set("BOOK_ID" , orderId)
        preference.set("PAYMENT_FOR" , PaymentManager.PAYMENT_FOR_EBOOK_PURCHASE)
        bookViewmodel.purchaseBookUsingYenepay(paymentInfo.priceInBirr!!.toDouble() , requireActivity() , orderId , orderName){
            Toast.makeText(requireContext() , it ?: "error occured" , Toast.LENGTH_LONG).show()
        }
    }

    fun handleGooglePlaybillingPUrchase(productId : String){
        binding.bookLoadingProgressbar.isVisible = true
        bookViewmodel.purchaseState.observe(viewLifecycleOwner){
            binding.bookLoadingProgressbar.isVisible = false
            it?.let { resource ->
                when(resource){
                    is  Resource.Success -> {
                        eBook?.let {
                            bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                                (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                    var bundle = bundleOf("SELECTED_PAGE" to 2)
                                    findNavController().navigate(R.id.downloadListFragment , bundle)
                                }
                                bookViewmodel.consumePurchase(resource.data!!.purchaseToken).observe(viewLifecycleOwner){
                                    if(it.responseCode != BillingClient.BillingResponseCode.OK){
                                        Log.i("billing ebook pur" , it.debugMessage + " " + it.responseCode.toString())
                                        (requireActivity() as MainActivity).showSnacbar("Unable to start download")
                                    }
                                }
                            })
                        }
                    }
                    is  Resource.Error -> requireActivity().showDialog("Error", resource.message ?: "Error occured payment will refunded" , getString(R.string.ok)){}
                    else -> {}
                }
            }
        }
        bookViewmodel.purchaseUsingGooglePlay(productId , BillingClient.SkuType.INAPP)
    }



    fun continueReading(ebook : EBook<Author<String , String>>){
        bookViewmodel.downloadTracker.getNovodaDownload(ebook._id!!).observe(viewLifecycleOwner , Observer{
            if(it.status() == DownloadBatchStatus.Status.DOWNLOADED){
                bookViewmodel.downloadTracker.getNovodaDownloadedFile(ebook._id!!).observe(viewLifecycleOwner , Observer{file ->
                    downloadViewmodel.getDownloadedEbook(ebook._id!!).observe(viewLifecycleOwner){
                        it?.let {
                            var bundle = bundleOf("BOOK_URL" to file?.localFilePath()?.path() , "BOOK_ID" to ebook._id ,
                                "CURRENT_POSITION" to it.lastReadingPage, "LOAD_FROM_CACHE" to true)
                            var intent = Intent(requireContext() , BookReaderActivity::class.java)
                            intent.putExtras(bundle)
                            requireContext().startActivity(intent)
                        }
                    }
                })
            }
            else {
                var bundle = bundleOf("SELECTED_PAGE" to 2)
                findNavController().navigate(R.id.downloadListFragment , bundle)
            }
        })
    }


    fun requestPermission(){
        if(ContextCompat.checkSelfPermission(requireContext() , Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            eBook?.let {
                bookViewmodel.downloadEbook(it).observe(viewLifecycleOwner , Observer{
                    if(it) (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                        var bundle = bundleOf("SELECTED_PAGE" to 2)
                        findNavController().navigate(R.id.downloadListFragment , bundle)
                    }
                })
            }
        }
        else{
            ActivityCompat.requestPermissions(requireActivity() , arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) ,  1)
        }
    }

}