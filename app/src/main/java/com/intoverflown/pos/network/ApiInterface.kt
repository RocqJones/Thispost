package com.intoverflown.pos.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {
    // because we use field annotation let's user 'form encoder'
    @FormUrlEncoded
    @POST("/Service/POS/API/v1/Connect/Login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ) : Any
}