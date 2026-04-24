package com.example.bgjz_app.data.remote.api

import com.example.bgjz_app.data.remote.dto.ProductDetailResponseDto
import com.example.bgjz_app.data.remote.dto.ProductResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20,
    ): Response<List<ProductResponseDto>>

    @GET("products/me")
    suspend fun getMyProducts(): Response<List<ProductResponseDto>>

    @GET("products/{product_id}")
    suspend fun getProductDetail(
        @Path("product_id") productId: Int
    ): Response<ProductDetailResponseDto>
}
