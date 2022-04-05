package com.komkum.komkum.ui.book

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
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
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.config.AppKey
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.FragmentAudiobookSellBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.ReviewAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.google.android.material.chip.Chip
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.squareup.picasso.Picasso
import com.yenepaySDK.PaymentOrderManager
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

@AndroidEntryPoint
class AudiobookSellFragment : Fragment() {
    lateinit var binding : FragmentAudiobookSellBinding
    val bookViewmodel : BookViewModel by viewModels()
    val mainactivityViewmodel : MainActivityViewmodel by activityViewModels()


    var loadFromCache : Boolean = false
    var paymentInfo : Payment? = null

    var bookId : String? = null
    var audioBook : Audiobook<Chapter, Author<String , String>>? = null
    var prevPlaylistQueue : List<Streamable<String , String>>? = null
    var isSampleStarted = false
    val PAYPAL_PAYMENT_CODE = 7777

    var availableCredit = 0


    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                bookId = it.getString("BOOK_ID")
                loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
            }
            bookId?.let {
                bookViewmodel.getAudiobook(it)

                bookViewmodel.getAudiobookSuggestionResult(it)
                requireActivity().invalidateOptionsMenu()
            }


        }
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        super.onAttach(context)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentAudiobookSellBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var fadeoutAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out)
        var fadeinAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        binding.container.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if(scrollY < 100){
                binding.toolbar.title = ""
            }
            else if(scrollY > 500){
                binding.toolbar.title = binding.bookTitleTextview.text
            }
        }
        var bookListener = object : IRecyclerViewInteractionListener<Book<String>> {
            override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
                var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
                findNavController().navigate(R.id.audiobookSellFragment , bundle)
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

        bookId?.let {
            bookViewmodel.isDownloaded(it).observe(viewLifecycleOwner , Observer{
                if(it){
                    binding.buyBookBtn.visibility = View.INVISIBLE
                    binding.regularPriceTextview.visibility = View.GONE
                    binding.upgradeSubscriptionBtn.visibility = View.GONE
                    binding.startListeningBtn.visibility = View.VISIBLE
                }
            })


            bookViewmodel.audiobookResult.observe(viewLifecycleOwner , Observer{
                it?.let {
                    if(it.promotion!!){
                        binding.buyBookBtn.visibility = View.INVISIBLE
                        binding.regularPriceTextview.visibility =View.GONE
                        binding.notifiyBookBtn.visibility = View.VISIBLE
                    }
                    if(it.book.isNullOrBlank()) binding.ebookFormatTextview.visibility = View.GONE
                    audioBook = it
                    toControllerActivity().selectedAudioBookForPurchase = it
                    binding.bookLoadingProgressbar.visibility = View.GONE
                    binding.bookContainer.visibility = View.VISIBLE
                    configureActionBar(binding.toolbar)
                    binding.bookTitleTextview.text = it.name
                    binding.bookAuthorTextview.text = it.authorName?.joinToString(", ")
                    binding.narratorNameTextview.text = "Narrated by ${it.narrator}"
                    Picasso.get().load(it.coverImagePath?.replace("localhost" , AdapterDiffUtil.URL))
                        .fit().centerCrop().placeholder(R.drawable.audiobook_placeholder).into(binding.bookCoverImageview)

                    binding.downloadAmountTextview.text = "${it.downloadCount} copy sold"
                    binding.durationTextview.text = it.length
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
                                binding.playSampleBtn.isVisible  = false
                            }
                            Payment.PAYMENT_TYPE_NO_CHARGE ->{
                                binding.buyBookBtn.text =  "Buy for Free"
                                binding.playSampleBtn.isVisible  = false
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
                    binding.moreReviewBtn.text = "Write review   (${it.rating?.size} Reviews)"
                    it.rating?.let {
                        var reviewAdapter = ReviewAdapter()
                        binding.bookRatingRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                        binding.bookRatingRecyclerview.adapter = reviewAdapter
                        reviewAdapter.submitList(it.sortedByDescending { rating ->rating.date })
                    }

                    arguments?.let {
                        var isFromPayment = it.getBoolean("FROM_PAYMENT" , false)
                        if(isFromPayment){
                            Toast.makeText(requireContext() , "Payment Completed" , Toast.LENGTH_LONG).show()
                            bookViewmodel.downloadAudiobook(audioBook!! , viewLifecycleOwner).observe(viewLifecycleOwner , Observer{
                                if(it){
                                    (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                        var bundle = bundleOf("SELECTED_PAGE" to 1)
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
                    }

                }
            })
        }

        binding.bookAuthorTextview.setOnClickListener { moveToAuthor() }

        binding.ebookFormatTextview.setOnClickListener {
            var bundle = bundleOf("BOOK_ID" to audioBook?.book)
            findNavController().navigate(R.id.EBookSellFragment , bundle)
        }

        binding.buyBookBtn.setOnClickListener {
            paymentInfo?.let {
                when(it.type){
                    Payment.PAYMENT_TYPE_NO_CHARGE ->{
                        bookViewmodel.downloadAudiobook(audioBook!! , viewLifecycleOwner).observe(viewLifecycleOwner , Observer{
                            if(it){
                                (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                    var bundle = bundleOf("SELECTED_PAGE" to 1)
                                    findNavController().navigate(R.id.downloadListFragment , bundle)
                                }
                            }
                        })
                    }
                    Payment.PAYMENT_TYPE_CREDIT ->  {
                        bookViewmodel.downloadAudiobook(audioBook!! , viewLifecycleOwner).observe(viewLifecycleOwner , Observer{
                            if(it){
                                (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                    var bundle = bundleOf("SELECTED_PAGE" to 1)
                                    findNavController().navigate(R.id.downloadListFragment , bundle)
                                }
                                var pref = PreferenceHelper.getInstance(requireContext())
                                var usedCredit = pref.get(AccountState.USED_AUDIOBOOK_CREDIT , 0)
                                pref[AccountState.USED_AUDIOBOOK_CREDIT] = usedCredit +1
                                requireActivity().invalidateOptionsMenu()
                            }
                        })
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
                                    0 ->  handleYenepayPayment(it , audioBook!!._id!! , audioBook!!.name!!)
//                                    1 -> paypalPaymentHandler(it , audioBook!!.name!!)
                                    1 ->{
                                        var productId = "product_price_${audioBook!!.priceInBirr?.toInt()}"
                                        handleGooglePlaybillingPUrchase(productId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        binding.startListeningBtn.setOnClickListener {
            var bundle = bundleOf("BOOK_ID" to bookId )
            findNavController().navigate(R.id.audiobookFragment , bundle)
        }

        binding.playSampleBtn.setOnClickListener {
            isSampleStarted = true
            mainactivityViewmodel.isAudiobookIntro = true
            prevPlaylistQueue = mainactivityViewmodel.playlistQueue.value
            audioBook?.let {
                var introInfo = it.intro!!.apply {
                    thumbnailPath = it.coverImagePath?.replace("localhost" , AdapterDiffUtil.URL)
                    tittle = it.intro!!.name
                    mpdPath = it.intro!!.mpdPath
                    songFilePath = it.intro!!.chapterfilePath
                    genre = it.authorName?.joinToString(", ")
                }
                mainactivityViewmodel.prepareAndPlayAudioBook(listOf(introInfo) , true , PlayerState.AudiobookState() , null)
                binding.playSampleBtn.visibility = View.INVISIBLE
                binding.stopSampleBtn.visibility = View.VISIBLE
            }
        }

        binding.stopSampleBtn.setOnClickListener {
            binding.playSampleBtn.visibility = View.VISIBLE
            binding.stopSampleBtn.visibility = View.INVISIBLE
            isSampleStarted = false

           continueSongPlayback()
        }

        binding.bookTitleTextview.setOnClickListener {
            bookViewmodel.downloadAudiobook(audioBook!! , viewLifecycleOwner).observe(viewLifecycleOwner , Observer{
                if(it) (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                    var bundle = bundleOf("SELECTED_PAGE" to 1)
                    findNavController().navigate(R.id.downloadListFragment , bundle)
                }
            })
        }

        binding.moreReviewBtn.setOnClickListener {
           audioBook?.let {
               (requireActivity() as MainActivity).movetoCreateReview(it._id!! , Review.CONTENT_TYPE_EBOOK ,  it.coverImagePath!! , it.name!! , it.authorName!!.joinToString(","))
           }
        }

        bookViewmodel.getError().observe(viewLifecycleOwner){}
        bookViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.audioBookErrorTextview.isVisible = true
                binding.bookContainer.isVisible = false
                binding.bookLoadingProgressbar.isVisible = false
            }
        }

//        binding.upgradeSubscriptionBtn.setOnClickListener {
//            findNavController().navigate(R.id.subscriptionFragment2)
//        }
    }



    override fun onPrepareOptionsMenu(menu: Menu) {
        var preference = PreferenceHelper.getInstance(requireContext())

        var token = preference.get(AccountState.TOKEN_PREFERENCE, AccountState.INVALID_TOKEN_PREFERENCE_VALUE)
        var jwt = JWT(token)
        var subscriptionInfo =  jwt.claims["realSubscriptionInfo"]?.asObject(SubscriptionPlan::class.java)

        subscriptionInfo?.let {
            if(it.level == SubscriptionPlan.SUBSCRIPTION_LEVEL_3){
                var usedCredit = preference.get(AccountState.USED_AUDIOBOOK_CREDIT , 0)
                Log.i("subsinfo" , subscriptionInfo.toString())
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

    override fun onPause() {
        if(isSampleStarted) continueSongPlayback()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

            R.id.share_menu_item ->{
                audioBook?.let {
                    requireActivity().showShareMenu("Share ${it.name}" ,
                        "Check out ${it.name}on Komkum. https://play.google.com/store/apps/details?id=com.komkum.komkum" )
                }
                true
            }
            R.id.available_credit_menu_item ->{
                var msg = "You have ${item.title} left to download either Ebook or Audiobook for free"
                requireContext().showDialog("Credits" , msg , getString(R.string.ok) , null){}
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Toast.makeText(requireContext() , requestCode.toString() , Toast.LENGTH_LONG).show()

        if(requestCode == PaymentOrderManager.YENEPAY_CHECKOUT_REQ_CODE){
//            Toast.makeText(requireContext() , requestCode.toString() , Toast.LENGTH_LONG).show()
        }
        if(requestCode == PAYPAL_PAYMENT_CODE){
            when (resultCode) {
                Activity.RESULT_OK -> {
                    bookViewmodel.downloadAudiobook(audioBook!! , viewLifecycleOwner).observe(viewLifecycleOwner , Observer{
                        if(it) (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                            var bundle = bundleOf("SELECTED_PAGE" to 1)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                    })
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

    fun moveToAuthor(){

        if(audioBook!!.author!!.size > 1){
            audioBook!!.authorName?.showListDialog(requireContext() , "Choose Author"){index, value ->
                (requireActivity() as MainActivity).movetoAuthor(audioBook!!.author!![index]._id!!)
            }
        }
        else (requireActivity() as MainActivity).movetoAuthor(audioBook!!.author!![0]._id!!)
    }

    fun continueSongPlayback(){
        mainactivityViewmodel.continueSongStream()
        if(!prevPlaylistQueue.isNullOrEmpty()){
            mainactivityViewmodel.isAudiobookIntro = false
            mainactivityViewmodel.playlistQueue.value = prevPlaylistQueue?.toMutableList()
            mainactivityViewmodel.playerState.value = PlayerState.PlaylistState()
            (requireActivity() as MainActivity).showPlayer()
        }
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
        preference["BOOK_ID"] = orderId
        preference["PAYMENT_FOR"] = PaymentManager.PAYMENT_FOR_AUDIOBOOK_PURCHASE
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
                        bookViewmodel.downloadAudiobook(audioBook!! , viewLifecycleOwner).observe(viewLifecycleOwner , Observer{
                            (requireActivity() as MainActivity).showSnacbar("Download added to the task" , "view"){
                                var bundle = bundleOf("SELECTED_PAGE" to 1)
                                findNavController().navigate(R.id.downloadListFragment , bundle)
                            }
                            bookViewmodel.consumePurchase(resource.data!!.purchaseToken).observe(viewLifecycleOwner){
                                if(it.responseCode != BillingClient.BillingResponseCode.OK){
                                    Log.i("billing adiobook pur" , it.debugMessage + " " + it.responseCode.toString())
                                    (requireActivity() as MainActivity).showSnacbar("Unable to start download")
                                }
                            }
                        })

                    }
                    is  Resource.Error -> requireActivity().showDialog("Error", resource.message ?: "Error occured payment will refunded" , getString(R.string.ok)){}

                }
            }
        }
        bookViewmodel.purchaseUsingGooglePlay(productId , BillingClient.SkuType.INAPP)
    }
}