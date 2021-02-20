package com.zomatunes.zomatunes.ui.book

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.data.repo.BookRepository
import com.zomatunes.zomatunes.data.repo.DownloadRepository
import com.zomatunes.zomatunes.data.repo.UserRepository
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import kotlinx.coroutines.launch


class BookViewModel @ViewModelInject constructor(@Assisted var  savedStateHandle: SavedStateHandle , var downloadRepo : DownloadRepository ,
                                                 var bookRepo : BookRepository , var paymentManager: PaymentManager ,
                                                 var  downloadTracker: DownloadTracker , var userRepo : UserRepository) : ViewModel() {
    val error = bookRepo.error
    var audiobookResult = MutableLiveData<Audiobook<Chapter , Author<String , String>>>()
    var eBookResult = MutableLiveData<EBook<Author<String,String>>>()

    var facebookFriendsBooks = MutableLiveData<List<Audiobook<String,String>>>()
    var bookSuggestions = MutableLiveData<BookSuggestion>()
    var bookhomeResultByGenre = MutableLiveData<BookHomeModel>()

    var bookHomeResult = MutableLiveData<BookHomeModel>()

    fun getBookHomeResult() = viewModelScope.launch {
        bookHomeResult.value = bookRepo.getBookHomeResult().data
    }

    suspend fun getBookHomeResultBeta() = bookRepo.getBookHomeResult()

    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(bookRepo.error){ error.value = it }
        a.addSource(userRepo.error){ error.value = it}
        return a
    }

    fun getBooksHomebyGenre(genre : String) = viewModelScope.launch {
        bookhomeResultByGenre.value  = bookRepo.getBookHomeResultt(genre).data
    }

    fun getAudiobookSuggestionResult(bookId : String) = viewModelScope.launch {
        var result = bookRepo.getAudiobookSuggestion(bookId)
        bookSuggestions.value = result.data
    }

    fun getEbookSuggestionResult(bookId : String) = viewModelScope.launch {
        var result = bookRepo.getEbookSuggestion(bookId)
        bookSuggestions.value = result.data
    }

    fun getAudiobook(bookId : String, loadFromCache : Boolean = false) = viewModelScope.launch {
        var result = bookRepo.getAudiobook(bookId , loadFromCache)
        audiobookResult.value = result.data
    }

    fun getEbook(bookId : String, loadFromCache: Boolean = false) = viewModelScope.launch {
        var result = bookRepo.getEbook(bookId , loadFromCache)
        eBookResult.value = result.data
    }

    fun rateBook(ratingInfo : Rating , bookFormat : String) = liveData {
       var result =  if(bookFormat == Book.AudiobookFormat) bookRepo.rateAudiobook(ratingInfo)
        else bookRepo.rateEbook(ratingInfo)
        emit(result.data!!)
    }

    suspend fun getBookChaptersFromDb(bookId: String) = bookRepo.getBookChapters(bookId)

     fun isAudiobookIsDownloaded(bookId: String) = liveData {
         var result = downloadRepo.isDownloadAvailable(bookId)
         emit(result)
     }

    fun downloadAudiobook(audiobook : Audiobook<Chapter , Author<String , String>>, lifecycleOwner: LifecycleOwner) = liveData {
        var updateUserInfo = bookRepo.saveAudiobookDownloadInfo(audiobook._id!!)
        val result =  downloadRepo.downloadAudioBook(audiobook)
        downloadTracker.addDownloads(audiobook.chapters!! , lifecycleOwner)
        emit(result)
    }


    fun downloadEbook(ebook : EBook<Author<String,String>>) = liveData {
        var saveDownloadInfoToApiResponse = bookRepo.saveEbookDownloadInfo(ebook._id!!).data
        var fileUrl = ebook.bookFile!!.replace("localhost" , AdapterDiffUtil.URL)
        var downloadRef = downloadTracker.addEbookDownload(ebook._id!! , fileUrl , ebook.name!!)
        var result = downloadRepo.saveEbookDownloadToDb(ebook)
        emit(result)
    }

    fun updateCurrentReadingPage(bookId : String , page : Int) = liveData {
        var updateResult = bookRepo.updateReadingPage(bookId , page)
        emit(updateResult)
    }

    fun getPaymentInfo(bookpriceInBirr : Float , bookPriceInDollar : Float) = paymentManager.handlePaymentConfiguration(bookpriceInBirr , bookPriceInDollar)

    fun getFacebookfriendsBook(userId : String) = viewModelScope.launch {
        userRepo.getFacebookFriendsFromDb(userId)?.let {
            var friendsId = it.map { friend -> friend.friendId }
            var result = bookRepo.getFacebookFriendsPurchasedBooks(friendsId)
            facebookFriendsBooks.value = result.data
        }
    }

    fun purchaseBookUsingYenepay(price : Double , context : Context , orderId : String , orderName : String , errorCallback : (message : String?) -> Unit){
       paymentManager.purchaseUsingYenepay(price , orderId , orderName , context , errorCallback)

//        var merchantId = "0235"
//        var orderItem = OrderedItem(orderId , orderName ,  1 , price)
//        var successUrl = "http://192.168.1.3:4000/api/yenepay/succussurl"
//        var returnUrl = "com.example.com.zomatunes.zomatunes.yenepay:/payment2redirect"
//        var failureUrl = "http://192.168.1.3:4000/api/yenepay/failureurl"
//
//        var paymentOrder = PaymentOrderManager(merchantId , orderId)
//        paymentOrder.paymentProcess = PaymentOrderManager.PROCESS_EXPRESS
//        paymentOrder.isUseSandboxEnabled = true
//        paymentOrder.isShoppingCartMode = false
//        paymentOrder.returnUrl = returnUrl
//        try {
//            paymentOrder.addItem(orderItem)
//            paymentOrder.startCheckout(context)
//
//        }catch (ex : InvalidPaymentException){
//           errorCallback(ex.message)
//        }
    }
}