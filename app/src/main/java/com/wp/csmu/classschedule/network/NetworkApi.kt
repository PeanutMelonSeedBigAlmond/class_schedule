package com.wp.csmu.classschedule.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal interface LoginApi {
    @GET("./")
    fun getCookie(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("xk/LoginToXk")
    fun login(@Header("cookie") cookie: String, @Field("encoded") encoded: String): Call<ResponseBody>

    @POST("xk/LoginToXk")
    @FormUrlEncoded
    fun loginWithVerifyCode(@Header("cookie") cookie: String, @Field("encoded") encoded: String, @Field("RANDOMCODE") verifyCode: String): Call<ResponseBody>

    @GET("verifycode.servlet")
    fun getVerifyCode(@Header("cookie") cookie: String): Call<ResponseBody>
}

internal interface DataApi {
    @GET("xskb/xskb_list.do")
    fun getSchedule(@Header("cookie") cookie: String): Call<ResponseBody>

    @POST("kscj/cjcx_list")
    @FormUrlEncoded
    fun getGrades(
            @Header("cookie") cookie: String,
            @Field("kksj") term: String,
            @Field("kcxz") classAttr: String = "",
            @Field("kcmc") className: String = "",
            @Field("xsfs") orderBy: String = ""
    ): Call<ResponseBody>

    @GET("jxzl/jxzl_query")
    fun getTermBeginsTime(
            @Header("cookie") cookie: String,
            @Header("referer") referer: String = Config.baseUrl + "jxzl/jxzl_query",
            @Query("xnxq01id") termId: String = ""
    ): Call<ResponseBody>

    @GET("kscj/cjcx_query")
    fun queryTerms(@Header("cookie") cookie: String): Call<ResponseBody>
}