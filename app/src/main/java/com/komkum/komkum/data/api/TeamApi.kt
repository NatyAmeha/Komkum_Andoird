package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface TeamApi {

    @POST("team/create")
    suspend fun createTeam(@Body teamInfo : Team<String>) : Response<String>

    @PUT("team/{id}/product/add")
    suspend fun addProductToTeam(@Path("id")teamId: String , @Query("pid")productId: String) : Response<Boolean>

    @PUT("team/{id}/product/remove")
    suspend fun removeProducdtFromTeam(@Path("id")teamId: String , @Query("pid")productId: String) : Response<Boolean>

    @PUT("team/{id}/activate")
    suspend fun activateTeam(@Path("id")teamId: String) : Response<Boolean>

    @PUT("team/{id}/join")
    suspend fun joinTeam(@Path("id")teamId: String) : Response<Boolean>

    @PUT("team/{id}/invitation")
    suspend fun joinTeamwithInvitation(@Path("id")teamId: String , @Query("uid")inviterId : String) : Response<Boolean>

    @PUT("team/{id}/claimcommission")
    suspend fun claimCommission(@Path("id")teamId: String) : Response<Int>


    @GET("team/{id}")
    suspend fun getTeam(@Path("id") teamId : String) : Response<Team<Product>>

    @GET("packageteam/{id}")
    suspend fun getTeamForPackage(@Path("id") teamId : String , @Query("long")long : Double , @Query("lat")lat : Double) : Response<Team<Product>>

    @GET("packages")
    suspend fun getAllPackages() : Response<List<ProductPackage<Product>>>

    @GET("game/team/{id}")
    suspend fun getGameTeam(@Path("id") teamId : String , @Query("long") long : Double? = null , @Query("lat") lat : Double? = null) : Response<Team<Product>>

    @GET("product/{id}/teams")
    suspend fun getActiveTeamsforProduct(@Path("id") productId : String , @Query("long") long : Double , @Query("lat") lat : Double) :Response<List<Team<Product>>>

    @GET("user/teams")
    suspend fun getAllUserTeams() : Response<List<Team<Product>>>

    @GET("user/packages")
    suspend fun getPackages() : Response<List<Team<String>>>

//    @GET("user/packages")
//    suspend fun get() : Response<List<Team<String>>>

    @GET("team/{id}/discountcode")
    suspend fun getDiscountCode(@Path("id")teamId: String , @Query("ad") adId : String) : Response<Code>

    @PUT("team/{id}/saveanswer")
    suspend fun saveAnswer(@Path("id")teamId: String , @Query("q") qId : String , @Query("ans") ans: Int) : Response<Boolean>

    @PUT("team/{id}/trivia/complete")
    suspend fun completeTrivia(@Path("id")teamId: String) : Response<Int>

}