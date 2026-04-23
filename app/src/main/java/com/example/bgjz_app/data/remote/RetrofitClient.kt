package com.example.bgjz_app.data.remote

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://bgdgnara.duckdns.org"
    private const val HOST = "bgdgnara.duckdns.org"
    private const val CERT_PIN = "sha256/vdM+wN7oF7AmctWqr2WmxnmRzf9sk86ko7pbm0Ids/g="

    private val certificatePinner = CertificatePinner.Builder()
        .add(HOST, CERT_PIN)
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
