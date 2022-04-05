package com.komkum.komkum.ui.store.seller

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.Seller
import com.komkum.komkum.data.repo.ProductRepository
import kotlinx.coroutines.launch

class SellerViewModel @ViewModelInject constructor(@Assisted savedStateHandle: SavedStateHandle, var productRepo : ProductRepository) : ViewModel() {

    var sellerInfo = MutableLiveData<Seller<Product>>()
    fun getSellerDetails(sellerId : String) = viewModelScope.launch {
        var result = productRepo.getSellerDetails(sellerId).data
        if(result != null) sellerInfo.value = result!!
    }
}