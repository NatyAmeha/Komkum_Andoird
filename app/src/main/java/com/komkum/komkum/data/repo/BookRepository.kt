package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.BookApi
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.*
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.dataRequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


class BookRepository @Inject constructor(var bookApi: BookApi , var appDb: AppDb) {
    var error = MutableLiveData<String>()

    suspend fun getBookHomeResult(genre : String = "all") = dataRequestHelper<Any , BookHomeModel>(errorHandler =  fun(message) = error.postValue(message)){
        bookApi.getBookHome(genre)
    }

    suspend fun getBookHomeResultt(genre : String = "all") = dataRequestHelper<Any , BookHomeModel>(errorHandler =  fun(message) = error.postValue(message)){
        bookApi.getBookHome(genre)
    }

    suspend fun getAudiobookSuggestion(bookId : String) = dataRequestHelper(bookId , fun(message) = error.postValue(message)){
        bookApi.getAudiobookSuggestion(it!!)
    }

    suspend fun getEbookSuggestion(bookId : String) = dataRequestHelper(bookId , fun(message) = error.postValue(message)){
        bookApi.getEbookSuggestion(it!!)
    }

    suspend fun getAudiobook(bookId : String, loadfromCache : Boolean = false) : Resource<Audiobook<Chapter , Author<String , String>>>{
       return  if(loadfromCache){
            withContext(Dispatchers.IO){
                var book = appDb.audiobookDao.getBook(bookId)
                var bookChapters = appDb.chapterDao.getBookChapters(bookId).map { chapInfo ->
                    Chapter(chapInfo._id , chapInfo.chapterNumber , chapInfo.name , mpdPath = chapInfo.mpdPath , songFilePath = chapInfo.chapterfilePath ,
                        duration = chapInfo.duration , chapterfilePath = chapInfo.chapterfilePath , thumbnailPath = book?.coverImagePath ,
                        currentPosition = chapInfo.currentPosition , completed = chapInfo.completed)
                }
                var bookREsult = Audiobook<Chapter , Author<String , String>>().apply {
                    _id = bookId; name = book?.name;  genre = book?.authorName?.split(", ");
                    releaseDate = Date(book?.releaseDate)
                    coverImagePath = book?.coverImagePath; authorName = book?.authorName?.split(",");
                    chapters = bookChapters; author = null; lastListeningChapterIndex = book?.lastListeningChapterIndex ?: 0
                    length = book?.length
                }

                Resource.Success(data = bookREsult)
            }
        }
        else{
            dataRequestHelper(bookId , errorHandler =  fun(message) = error.postValue(message)){
                bookApi.getAudiobook(it!!)
            }
        }
    }

    suspend fun getEbook(bookId : String , loadfromCache: Boolean = false) : Resource<EBook<Author<String,String>>>{
        return dataRequestHelper(bookId , fun(message) = error.postValue(message)){
            bookApi.getEbook(it!!)
        }
    }

    suspend fun updateReadingPage(bookId : String , page : Int) = withContext(Dispatchers.IO) {
        var updateResult = appDb.ebookDao.updateLastReadingPage(bookId , page)
        return@withContext true
    }

    suspend fun rateAudiobook(ratingInfo : Review) = dataRequestHelper(ratingInfo , errorHandler =  fun(message) = error.postValue(message)){
        bookApi.rateAudiobook(it!!)
    }

    suspend fun rateEbook(ratingInfo : Review) = dataRequestHelper(ratingInfo , errorHandler =  fun(message) = error.postValue(message)){
        bookApi.rateEbook(it!!)
    }

    suspend fun saveAudiobookDownloadInfo(bookId : String) = dataRequestHelper(bookId , fun(message) = error.postValue(message)){
        bookApi.saveAudiobookDownloadInfo(it!!)
    }

    suspend fun saveEbookDownloadInfo(bookId : String) = dataRequestHelper(bookId , fun(message) = error.postValue(message)){
        bookApi.saveEbookDownloadInfo(it!!)
    }

    suspend fun getBookChapters(bookId: String) : List<ChapterDbInfo>{
        return appDb.chapterDao.getBookChapters(bookId)
    }

    suspend fun getFacebookFriendsPurchasedBooks(friendsId : List<String>) = dataRequestHelper(friendsId ,  fun(message) = error.postValue(message)){
        bookApi.getFacebookFriendsBook(friendsId)
    }
}