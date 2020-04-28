package com.zaidan.removebg.helper

import android.text.TextUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {
    private val BASE_URL = "https://api.remove.bg/v1.0/"
    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())

    private lateinit var retrofit: Retrofit

    private val logging = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)
    private val httpClient = OkHttpClient.Builder()

    fun retrofit():Retrofit {
        return retrofit
    }

    fun <S> createService(serviceClass:Class<S>, authToken:String):S {
        if (!TextUtils.isEmpty(authToken)) {
            httpClient.addInterceptor(Authentication(authToken))
        }
        httpClient.addInterceptor(logging)
        builder.client(httpClient.build())
        retrofit = builder.build()
        return retrofit.create(serviceClass)
    }
}