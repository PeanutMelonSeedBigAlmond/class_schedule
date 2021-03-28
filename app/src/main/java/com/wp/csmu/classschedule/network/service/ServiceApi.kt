package com.wp.csmu.classschedule.network.service

import okhttp3.ResponseBody
import retrofit2.http.*

internal interface ServiceApi {
    companion object {
        val BASE_URL = "http://oa.csmu.edu.cn:8099/jsxsd/"
    }

    @POST("xskb/xskb_list.do")
    @FormUrlEncoded
    suspend fun getSchedule(
            @Field("xnxq01id") termId: String = ""
    ): ResponseBody

    @POST("kscj/cjcx_list")
    @FormUrlEncoded
    suspend fun getGrades(
            @Field("kksj") term: String,
            @Field("kcxz") classAttr: String = "",
            @Field("kcmc") className: String = "",
            @Field("xsfs") orderBy: String = "all"
    ): ResponseBody

    @GET("jxzl/jxzl_query")
    suspend fun getTermBeginsTime(
            @Query("xnxq01id") termId: String = "",
            @Header("referer") referer: String = BASE_URL + "jxzl/jxzl_query"
    ): ResponseBody
}