package com.wp.csmu.classschedule.network.login

import android.util.Base64
import com.wp.csmu.classschedule.network.LoginState
import com.wp.csmu.classschedule.network.NetworkConfig
import com.wp.csmu.classschedule.network.cookie.CookieClient
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import retrofit2.Retrofit

object LoginClient {
    private var tempCookie = ""
    private val client = OkHttpClient.Builder().addInterceptor {
        val request = it.request()
        if (NetworkConfig.cookie == "") {
            val cookie = runBlocking { CookieClient.getCookie() }
            tempCookie = cookie
        }
        val newRequest = request.newBuilder()
                .removeHeader("user-agent")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
                .removeHeader("cookie")
                .addHeader("cookie", tempCookie)
                .removeHeader("referer")
                .addHeader("referer", LoginApi.BASE_URL)
                .build()
        return@addInterceptor it.proceed(newRequest)
    }.build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(LoginApi.BASE_URL)
            .client(client)
            .build()
            .create(LoginApi::class.java)

    suspend fun login(userName: String, password: String): LoginState {
        val encoded = encode(userName, password)
        val response = retrofit.login(encoded).string()
        val document = Jsoup.parse(response)
        if (document.title() == "学生个人中心") {
            NetworkConfig.cookie = tempCookie
            return LoginState.SUCCESS
        } else {
            return LoginState.WRONG_PASSWORD
        }
    }

    private fun encode(userName: String, password: String) = String(Base64.encode(userName.toByteArray(), Base64.DEFAULT)).plus("%%%").plus(String(Base64.encode(password.toByteArray(), Base64.DEFAULT)))
}