package com.wp.csmu.classschedule.network.cookie

import okhttp3.OkHttpClient
import retrofit2.Retrofit

object CookieClient {
    private val client = OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request()
                        .newBuilder()
                        .removeHeader("user-agent")
                        .addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
                        .build()
                return@addInterceptor it.proceed(request)
            }.build()
    private val retrofit = Retrofit.Builder()
            .baseUrl(CookieApi.BASE_URL)
            .client(client)
            .build()
            .create(CookieApi::class.java)

    suspend fun getCookie(): String {
        val header = retrofit.getCookie().headers()["set-cookie"]!!
        return header
    }
}