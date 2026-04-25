package com.example.bgjz_app.data.repository

import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.ProductDetail
import com.example.bgjz_app.data.model.RegisterProductRequest
import com.example.bgjz_app.data.model.UserResult

interface ProductRepository {
    suspend fun getProducts(limit: Int = 20): UserResult<List<Product>>
    suspend fun getProductById(productId: Int): UserResult<ProductDetail>
    suspend fun getMyProducts(): UserResult<List<Product>>
    suspend fun getLikedProducts(): UserResult<List<Product>>
    suspend fun registerProduct(request: RegisterProductRequest): UserResult<Product>
    suspend fun updateProduct(productId: Int, request: RegisterProductRequest): UserResult<Product>
    suspend fun deleteProduct(productId: Int): UserResult<Unit>
    suspend fun updateProductStatus(productId: Int, status: com.example.bgjz_app.data.mock.ProductStatus): UserResult<Unit>
    suspend fun uploadProductImages(productId: Int, imageBytes: List<ByteArray>): UserResult<Unit>
    suspend fun getProductsByUser(userId: String): UserResult<List<Product>>
    suspend fun likeProduct(productId: Int): UserResult<Unit>
    suspend fun unlikeProduct(productId: Int): UserResult<Unit>
    suspend fun searchProducts(query: String): UserResult<List<Product>>
}
