package com.komkum.komkum.ui.store

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.OrderRepository
import com.komkum.komkum.data.repo.ProductRepository
import com.komkum.komkum.data.repo.TeamRepository
import com.komkum.komkum.data.repo.UserRepository
import com.komkum.komkum.data.viewmodel.ProductDetailViewmodel
import com.komkum.komkum.data.viewmodel.ProductHomepageViewmodel
import kotlinx.coroutines.launch

class ProductViewModel @ViewModelInject constructor(@Assisted var savedStateHandle: SavedStateHandle , var orderRepo : OrderRepository,
                                                    var productRepo : ProductRepository , var teamRepo : TeamRepository , var userRepo : UserRepository) : ViewModel() {

    var error = MutableLiveData<String>()

    fun getErrors() : MediatorLiveData<String>{
        var errorSource = MediatorLiveData<String>()
        errorSource.addSource(productRepo.error){error.value  = it}
        errorSource.addSource(teamRepo.error){error.value  = it}
        errorSource.addSource(orderRepo.error){error.value  = it}
        errorSource.addSource(userRepo.error){error.value = it}
        return errorSource

    }

    fun removeOldErrors(){
        productRepo.error.value = null
        userRepo.error.value = null
        orderRepo.error.value = null
        teamRepo.error.value = null
    }

    var productDetails = MutableLiveData<ProductDetailViewmodel>()
    var productMetadataList = MutableLiveData<List<ProductMetadata<Product , String>>>()

    var productList = MutableLiveData<List<Product>>()
    var packageList = MutableLiveData<List<ProductPackage<Product>>>()
    var productBrowseResult = MutableLiveData<ProductHomepageViewmodel>()
    var productCategoris = MutableLiveData<List<ProductCategory>>()


    var productWithOrderSet = mutableSetOf<OrderedItem<String>>()
    var selectedQty = 1
    var selectedSku : SKUCombination? = null
    var orderInfo :  Order<String,Address>? = null
    var showBottomMenu = MutableLiveData(false)

    fun prepareOrder(productId : String , sku : Map<String,String>? , price : Int , qty : Int ,
                    image : String , teamId : String? = null , purchaseType: Int = Product.STANDARD_PURCHASE ,
                     additionalQty : Int = 0 , isfinal : Boolean = false){
        selectedQty = qty
        var itemInfo = OrderedItem(null , productId , qty , null , price , image = image ,
            sku = sku , purchaseType =  purchaseType , team = teamId , additionalQty = additionalQty)
//        productWithOrderSet.add(itemInfo)
        if(isfinal){
            var items = listOf(itemInfo)
            var order = Order<String , Address>(items = items.toList() , totalPrice = price.times(qty))
            orderInfo = order
        }
    }

    fun addToCart(productId : String , sku : Map<String,String>? , price : Int , qty : Int ,
                 image : String ,  purchaseType : Int , teamId : String? = null,
                  additionalQty : Int = 0 , isfinal : Boolean = false) = liveData{
        var itemInfo = OrderedItem(null , productId , qty ,  null , price , image = image ,
            sku = sku , purchaseType = purchaseType , team = teamId , additionalQty = additionalQty)

        var result = orderRepo.addtoCart(itemInfo).data
//        prepareOrder(productId , sku , price , qty , prodSrc , isfinal)
        if(result == true){
            showBottomMenu.value = isfinal
            emit(result)
        }
    }

    fun searchProducts(query : String) = viewModelScope.launch {
        var result = productRepo.searchProducts(query).data
        productList.value = result?.products ?: emptyList()
        packageList.value = result?.packages ?: emptyList()
    }

    fun getProductDetails(productId : String) = viewModelScope.launch {
        var result = productRepo.getProductDetails(productId).data
        if(result != null) productDetails.value = result!!
    }

    fun getSmallProductInfo(productId : String) = liveData {
        var result = productRepo.getSmallProductInfo(productId).data
        emit(result)
    }

    fun getMetadataofProducts(productIds : String) = viewModelScope.launch {
        var result = productRepo.getProductMetadata(productIds).data
        if(result!= null)productMetadataList.value = result!!
    }

    fun getProductBrowseCategories() = viewModelScope.launch {
        var result = productRepo.getProductCategories().data
        if(result != null) productCategoris.value = result!!
    }

    fun getBrowseListbyDepResult(department : String) = viewModelScope.launch {
        var result = productRepo.getBrowseListbyDepartment(department).data
        if(result != null) productBrowseResult.value = result!!
    }

    fun getBrowsebyCategoryResult(category : String) = viewModelScope.launch {
        var result = productRepo.getBrowseListbyCategory(category).data
        if(result!= null) productList.value = result!!
    }


    fun getWishListProducts() = viewModelScope.launch {
        var result = userRepo.getWishList().data
        if(result!= null) productList.value = result!!
    }
    fun addProductTowishlist(productId : String) = liveData {
        var result = userRepo.addProductToWishlist(productId).data
        emit(result)
    }

    fun removeProductFromWishlist(productId : String) = liveData {
        var result = userRepo.removeFromWishlist(productId).data
        emit(result)
    }


    fun getTrendingProducts(limit : Int) = viewModelScope.launch{
        var result = productRepo.getTrendingProducts(limit).data
        if(result!= null) productList.value = result!!
    }

    fun getNewArrivals() = viewModelScope.launch{
        var result = productRepo.getNewArrivalProducts().data
        if(result!= null) productList.value = result!!
    }

    fun getBestsellingProducts(limit : Int) = viewModelScope.launch{
        var result = productRepo.getBestsellingProducts(limit).data
        if(result!= null) productList.value = result!!
    }

    fun getUserTeamsForPackages() = liveData{
        var result = teamRepo.getTeamsForPackages().data
        emit(result)
    }

    fun reviewProduct(reviewInfo : Review) = liveData{
        var result = productRepo.reviewProduct(reviewInfo).data
        emit(result)
    }

    suspend fun getProductReviews(productId : String , page : Int) = productRepo.getProductReview(productId , page).data


    suspend fun getStoreReviews(sellerId : String , rating : String) = productRepo.getSellerReview(sellerId , rating).data

}