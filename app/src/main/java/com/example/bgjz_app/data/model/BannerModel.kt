package com.example.bgjz_app.data.model

/**
 * 홈 화면 배너 도메인 모델.
 * imageUrl은 절대 URL (Mapper에서 변환).
 */
data class Banner(
    val id: Int,
    val imageUrl: String,
    val linkUrl: String? = null,
    val title: String? = null,
)
