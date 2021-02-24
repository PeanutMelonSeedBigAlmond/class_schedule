package com.wp.csmu.classschedule.network.cookie

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object CookieClient {
    private val client = OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request()
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