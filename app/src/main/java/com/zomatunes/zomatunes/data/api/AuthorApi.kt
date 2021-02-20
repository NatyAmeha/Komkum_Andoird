package com.zomatunes.zomatunes.data.api

import com.zomatunes.zomatunes.data.model.AuthorMetadata
import retrofit2.Response
import retrofit2.http.*

interface AuthorApi {
    @GET("author/{id}")
    suspend fun getAuthorInfo(@Path("id")authorId : String) : Response<AuthorMetadata>

    @PUT("author/follow")
    suspend fun followAuthor(@Body authorsId : List<String>) : Response<Boolean>

    @PUT("author/unfollow")
    suspend fun unfollowAuthor(@Body authorsId : List<String>) : Response<Boolean>

    @GET("author/isfavorite")
    suspend fun isAuthorInFavorite(@Query("id")authorId : String) : Response<Boolean>
}