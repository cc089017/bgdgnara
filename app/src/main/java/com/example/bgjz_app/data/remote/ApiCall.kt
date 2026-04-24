package com.example.bgjz_app.data.remote

import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

/**
 * Retrofit 호출을 감싸서 성공/실패 공통 처리.
 * 성공: [Result.success]
 * 실패: FastAPI 기본 에러 포맷 `{"detail": "메시지"}` 파싱, 없으면 HTTP status 메시지 사용
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ApiResult.Success(body)
            else ApiResult.Error("응답 본문이 비어있습니다", response.code())
        } else {
            val detail = parseErrorDetail(response.errorBody()?.string())
                ?: "요청 실패 (${response.code()})"
            ApiResult.Error(detail, response.code())
        }
    } catch (e: IOException) {
        ApiResult.Error("네트워크 연결을 확인해주세요")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "알 수 없는 오류")
    }
}

private fun parseErrorDetail(errorBody: String?): String? {
    if (errorBody.isNullOrBlank()) return null
    return try {
        JsonParser.parseString(errorBody).asJsonObject
            .get("detail")?.asString
    } catch (_: Exception) {
        null
    }
}
