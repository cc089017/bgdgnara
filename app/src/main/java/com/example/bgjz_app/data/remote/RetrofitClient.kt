package com.example.bgjz_app.data.remote

import android.content.Context
import com.example.bgjz_app.BuildConfig
import com.example.bgjz_app.data.remote.api.AuthApi
import com.example.bgjz_app.data.remote.api.ProductApi
import com.example.bgjz_app.data.remote.api.UserApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 싱글톤. ApplicationContext를 한번 주입받아 초기화.
 * 사용 예: RetrofitClient.init(applicationContext); RetrofitClient.authApi
 */
object RetrofitClient {

    lateinit var tokenStorage: TokenStorage
        private set

    private lateinit var retrofit: Retrofit

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val productApi: ProductApi by lazy { retrofit.create(ProductApi::class.java) }

    val baseUrl: String get() = BuildConfig.BASE_URL

    fun init(context: Context) {
        if (::retrofit.isInitialized) return
        tokenStorage = TokenStorage(context.applicationContext)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
