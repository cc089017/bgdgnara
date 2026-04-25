package com.example.bgjz_app.data.remote.api

import com.example.bgjz_app.data.remote.dto.ProductCreateDto
import com.example.bgjz_app.data.remote.dto.ProductDetailResponseDto
import com.example.bgjz_app.data.remote.dto.ProductResponseDto
import com.example.bgjz_app.data.remote.dto.MessageResponseDto
import com.example.bgjz_app.data.remote.dto.ProductStatusUpdateDto
import com.example.bgjz_app.data.remote.dto.ProductUpdateDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
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

    @POST("products")
    suspend fun createProduct(
        @Body body: ProductCreateDto
    ): Response<ProductResponseDto>

    @PATCH("products/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: Int,
        @Body body: ProductUpdateDto
    ): Response<ProductResponseDto>

    @PATCH("products/{product_id}/status")
    suspend fun updateProductStatus(
        @Path("product_id") productId: Int,
        @Body body: ProductStatusUpdateDto
    ): Response<ProductResponseDto>

    @DELETE("products/{product_id}")
    suspend fun deleteProduct(
        @Path("product_id") productId: Int
    ): Response<MessageResponseDto>

    @Multipart
    @POST("products/{product_id}/images")
    suspend fun uploadImages(
        @Path("product_id") productId: Int,
        @Part files: List<MultipartBody.Part>
    ): Response<MessageResponseDto>
}
