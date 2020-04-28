package com.zaidan.removebg.helper

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class Authentication(authToken: String):Interceptor  {
    private val authToken: String = authToken

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("X-API-KEY", authToken)
        val request = builder.build()
        return chain.proceed(request)
    }
}