package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.OrderApi
import com.komkum.komkum.data.api.TeamApi
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.ProductPackage
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.util.extensions.dataRequestHelper
import retrofit2.http.Query
import javax.inject.Inject

class TeamRepository @Inject constructor(var teamApi : TeamApi , var orderApi: OrderApi) {
    var error = MutableLiveData<String>()

    suspend fun getTeamDetails(teamId : String) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.getTeam(it!!)
    }

    suspend fun getTeamForPackage(teamId : String , long : Double ,lat: Double) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.getTeamForPackage(it!! , long , lat)
    }

    suspend fun getAllPackages() = dataRequestHelper<Any , List<ProductPackage<Product>>>(errorHandler =  fun(message) = error.postValue(message)){
        teamApi.getAllPackages()
    }

    suspend fun getGameTeamDetails(teamId : String, long : Double? = null, lat : Double? = null) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.getGameTeam(it!! , long , lat)
    }

    suspend fun getActiveTeamsForProduct(productId : String , long : Double , lat : Double) = dataRequestHelper(productId , fun(message) = error.postValue(message)){
        teamApi.getActiveTeamsforProduct(productId , long , lat)
    }

    suspend fun createTeam(teamInfo : Team<String>) = dataRequestHelper(teamInfo , fun(message) = error.postValue(message)){
        teamApi.createTeam(it!!)
    }

    suspend fun addProductToTeam(teamId : String , productId: String) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.addProductToTeam(it!! , productId )
    }

    suspend fun removeProductFromTeam(teamId : String , productId: String ) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.removeProducdtFromTeam(it!! , productId )
    }

    suspend fun activateTeam(teamId : String ) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.activateTeam(it!!)
    }

    suspend fun joinTeam(teamId : String ) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.joinTeam(it!!)
    }

    suspend fun joinTeamWithInvitation(teamId : String , inviterId : String ) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.joinTeamwithInvitation(it!! , inviterId)
    }

    suspend fun claimTeamCommission(teamId : String ) = dataRequestHelper(teamId , fun(message) = error.postValue(message)){
        teamApi.claimCommission(it!!)
    }

    suspend fun getAllUserTeams() = dataRequestHelper<Any , List<Team<Product>>>(errorHandler = fun(message) = error.postValue(message)){
        teamApi.getAllUserTeams()
    }

    suspend fun getTeamsForPackages() = dataRequestHelper<Any , List<Team<String>>>(errorHandler = fun(message) = error.postValue(message)){
        teamApi.getPackages()
    }


    suspend fun getDis0countCode(teamId : String, adId : String) = dataRequestHelper(teamId , errorHandler = fun(message) = error.postValue(message)){
        teamApi.getDiscountCode(it!! , adId)
    }

    suspend fun saveTriviaAnswer(teamId : String, qId : String, ans : Int) = dataRequestHelper(teamId , errorHandler = fun(message) = error.postValue(message)){
        teamApi.saveAnswer(it!! , qId , ans)
    }

    suspend fun completeTriviaGame(teamId : String) = dataRequestHelper(teamId , errorHandler = fun(message) = error.postValue(message)){
        teamApi.completeTrivia(it!!)
    }


    suspend fun getCityforValidForTeamPurchase() = dataRequestHelper<Any , MutableList<String>>(null,  fun(message) = error.postValue(message)){
        orderApi.getDeliveryDestination()
    }
}