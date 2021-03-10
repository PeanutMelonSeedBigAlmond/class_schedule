package com.wp.csmu.classschedule.network.cookie

import retrofit2.http.GET

interface CookieApi {
    companion object {
        const val BASE_URL = "http://oa.csmu.edu.cn:8099/jsxsd/"
    }

    @GET("./")
    suspend fun getCookie(): retrofit2.Response<Unit>
}