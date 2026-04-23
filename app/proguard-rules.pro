# ─────────────────────────────────────────────
# JNI 브릿지 — 클래스/메서드명 변경 시 .so 연결 깨짐
# ─────────────────────────────────────────────
-keep class com.example.bgjz_app.security.RootDetector {
    private native <methods>;
    public *;
}

# ─────────────────────────────────────────────
# Retrofit + OkHttp
# ─────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# ─────────────────────────────────────────────
# Gson (data class 필드명 보존 — 백엔드 JSON 키와 매핑)
# ─────────────────────────────────────────────
-keepclassmembers class com.example.bgjz_app.data.model.** { *; }

# ─────────────────────────────────────────────
# Kotlin
# ─────────────────────────────────────────────
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# ─────────────────────────────────────────────
# Compose (런타임 리플렉션 사용)
# ─────────────────────────────────────────────
-dontwarn androidx.compose.**
