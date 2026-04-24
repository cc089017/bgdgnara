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

    // ──────────────────────────────────────────────────────────────
    // 프로덕션 URL 전환용 SSL 핀닝 레퍼런스 (타 브랜치에서 가져온 설정)
    // 현재는 localhost(adb reverse) 테스트 모드라 비활성.
    // https://bgdgnara.duckdns.org 로 붙일 때:
    //   1) 아래 import / const / certificatePinner 블록 활성화
    //   2) BuildConfig.BASE_URL 을 https://bgdgnara.duckdns.org 로 교체
    //   3) init() 안 OkHttpClient.Builder() 에 .certificatePinner(certificatePinner) 추가
    // 시스템 레벨 핀닝은 이미 res/xml/network_security_config.xml 에 있음.
    // ──────────────────────────────────────────────────────────────
    // import okhttp3.CertificatePinner
    //
    // private const val PROD_BASE_URL = "https://bgdgnara.duckdns.org"
    // private const val PROD_HOST = "bgdgnara.duckdns.org"
    // private const val PROD_CERT_PIN = "sha256/vdM+wN7oF7AmctWqr2WmxnmRzf9sk86ko7pbm0Ids/g="
    //
    // private val certificatePinner = CertificatePinner.Builder()
    //     .add(PROD_HOST, PROD_CERT_PIN)
    //     .build()
}
