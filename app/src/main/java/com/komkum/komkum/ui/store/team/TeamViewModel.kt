package com.komkum.komkum.ui.store.team

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.ProductPackage
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.data.model.TeamList
import com.komkum.komkum.data.repo.ProductRepository
import com.komkum.komkum.data.repo.TeamRepository
import com.komkum.komkum.data.repo.UserRepository
import com.komkum.komkum.usecase.WalletUsecase
import kotlinx.coroutines.launch
import java.util.*

class TeamViewModel @ViewModelInject constructor(@Assisted  savedStateHandle: SavedStateHandle, var walletUsecase: WalletUsecase, var productRepo : ProductRepository,
                                                 var teamRepo : TeamRepository, var userRepo : UserRepository) : ViewModel() {
    var error = MutableLiveData<String>()

    var packageLists = MutableLiveData<List<ProductPackage<Product>>>()
    var originalPackageList = mutableListOf<ProductPackage<Product>>()

    fun getError() : MediatorLiveData<String>{
        var errorSource = MediatorLiveData<String>()
        errorSource.addSource(productRepo.error){error.value = it}
        errorSource.addSource(teamRepo.error){error.value  = it}
        errorSource.addSource(walletUsecase.error){error.value = it}
        errorSource.addSource(userRepo.error){error.value = it}
        return errorSource
    }

    fun removeOldErrors(){
        productRepo.error.value = null
        teamRepo.error.value = null
        walletUsecase.error.value = null
        userRepo.error.value = null
    }

    var teamDetails = MutableLiveData<Team<Product>>()
    var teamProducts  = MutableLiveData<List<Product>>()
    var teamList = MutableLiveData<List<Team<String>>>()
    var teamListWithProductInfo = MutableLiveData<List<Team<Product>>>()

    var nearAndTrendingTeamList = MutableLiveData<TeamList>()

    fun getTeamDetails(teamId : String) = viewModelScope.launch {
        var result = teamRepo.getTeamDetails(teamId).data

        if (result != null){
            teamProducts.value = result!!.products!!
            teamDetails.value = result!!
        }
    }

    fun getTeamDetailsLiveData(teamId : String) = liveData {
        var result = teamRepo.getTeamDetails(teamId).data
        emit(result)
    }

    fun getTeamForPackage(packageId : String , longtiude : Double , latitude : Double) = viewModelScope.launch {
        var result = teamRepo.getTeamForPackage(packageId , longtiude , latitude).data

        if (result != null){
            teamProducts.value = result!!.products!!
            teamDetails.value = result!!
        }
    }

    fun getAvailablePackages() = viewModelScope.launch {
        var result = teamRepo.getAllPackages().data
        if (result != null){
            originalPackageList = result.toMutableList()
            packageLists.value = result!!
        }
    }

    fun getGameTeamDetails(adId : String , long : Double? = null, lat : Double? = null) = viewModelScope.launch {
        var result = teamRepo.getGameTeamDetails(adId , long , lat).data

        if (result != null){
            teamDetails.value = result!!
        }
    }

    fun getActiveTeams(productId : String , long : Double , lat : Double) = viewModelScope.launch{
        var result = teamRepo.getActiveTeamsForProduct(productId , long , lat).data
        if(result != null) teamListWithProductInfo.value = result!!
    }

    fun createTeam(teamInfo : Team<String>) = liveData {
        var result = teamRepo.createTeam(teamInfo).data
        emit(result)
    }

    fun addProductToTeam(teamId: String , productId: String ) = liveData {
        var result = teamRepo.addProductToTeam(teamId , productId).data
        emit(result)
    }

    fun removeProductFromTeam(teamId: String , productId: String) = liveData {
        var result = teamRepo.removeProductFromTeam(teamId , productId).data
        emit(result)
    }

    fun activateTeam(teamId: String , teamSize : Int) = liveData {
        var result = teamRepo.activateTeam(teamId).data
        emit(result)
    }

    fun joinTeam(teamId: String , inviterId: String? = null) = liveData {
        var result = if(inviterId != null)  teamRepo.joinTeamWithInvitation(teamId , inviterId ).data
            else  teamRepo.joinTeam(teamId ).data
        emit(result)
    }

    fun cliamTeamCommission(teamId: String) = liveData{
        var result = teamRepo.claimTeamCommission(teamId).data
        emit(result)
    }

    fun getTrendingPackages(long : Double? = null , lat : Double? = null) = viewModelScope.launch{
        var result = productRepo.getTrendingPackagesofProducts(long , lat).data
        if(result != null) nearAndTrendingTeamList.value = result!!
    }

    fun getProductsExpireSoon(long : Double? = null , lat : Double? = null) = viewModelScope.launch{
        var result = productRepo.getProductsExpiresoon(long , lat).data
        if(result != null) nearAndTrendingTeamList.value = result!!
    }

    fun getUserTeams() = viewModelScope.launch{
        var result = teamRepo.getAllUserTeams().data
        result?.let { teams ->
            var sortedTeams = teams.sortedByDescending { it.endDate }
            var userTeams = sortedTeams
            var collectionTeams = sortedTeams.filter { team -> team.type == Team.MULTI_PRODUCT_TEAM }
            var gameTeams = sortedTeams.filter { team -> team.type == Team.TRIVIA_TEAM }
            nearAndTrendingTeamList.value =
                TeamList(userTeams = userTeams , userCollections = collectionTeams , gameTeams = gameTeams)
        }
    }


    fun removeProductFromList(index : Int) = viewModelScope.launch {
        var productList = teamProducts.value?.toMutableList()
        productList?.removeAt(index)
        teamProducts.value = productList!!
    }

    fun getDiscountCode(teamId: String , adId : String) = liveData {
        var result = teamRepo.getDis0countCode(teamId , adId).data
        emit(result)
    }

    fun transferGamereward(amount : Int , adId : String , teamId : String , discountcode :String) = liveData {
        var result = walletUsecase.transferGameRewardToWallet(amount , adId , teamId , discountcode)
        emit(result)
    }

    fun saveTriviaAnswer(teamId: String , qId : String , ans : Int) = liveData {
        var result = teamRepo.saveTriviaAnswer(teamId , qId , ans).data
        emit(result)
    }

    fun completeTriviaGame(teamId: String) = liveData {
        var result = teamRepo.completeTriviaGame(teamId).data
        emit(result)
    }

    fun getCitiesForTeamPurchase() = liveData {
        var result = teamRepo.getCityforValidForTeamPurchase()
        emit(result)
    }


    fun isAuthenticated() = userRepo.userManager.isLoggedIn()

}