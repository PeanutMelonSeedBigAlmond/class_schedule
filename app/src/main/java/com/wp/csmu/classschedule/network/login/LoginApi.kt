package com.wp.csmu.classschedule.network.login

import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.http.*
import java.io.InputStream

interface LoginApi {
    companion object {
        const val BASE_URL = "http://oa.csmu.edu.cn:8099/jsxsd/"
    }

    @FormUrlEncoded
    @POST("xk/LoginToXk")
    suspend fun login(
            @Field("encoded") encoded: String,
    ): ResponseBody

    @POST("xk/LoginToXk")
    @FormUrlEncoded
    suspend fun loginWithVerifyCode(
            @Field("encoded") encoded: String,
            @Field("RANDOMCODE") verifyCode: String
    ): ResponseBody

    @GET("verifycode.servlet")
    suspend fun getVerifyCode(): InputStream
}