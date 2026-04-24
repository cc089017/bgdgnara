#include <jni.h>
#include <cstring>
#include <cctype>
#include <sys/stat.h>
#include <android/log.h>
#include <sys/system_properties.h>

#define TAG "RootDetectionLog"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

static const char* SU_BINARY_PATHS[] = {
    "/system/bin/su",
    "/system/xbin/su",
    "/system/sbin/su",
    "/system/sd/xbin/su",
    "/system/bin/failsafe/su",
    "/su/bin/su",
    "/su/xbin/su",
    "/su/bin/daemonsu",
    "/sbin/su",
    "/vendor/bin/su",
    "/data/local/su",
    "/data/local/bin/su",
    "/data/local/xbin/su"
};

static const char* ROOT_PATHS[] = {
    "/su/etc/",
    "/data/adb/magisk",
    "/data/adb/modules",
    "/data/adb/magisk.db",
    "/sbin/.magisk",
    "/dev/com.google.android.gms.setup"
};

static bool pathExists(const char* path) {
    struct stat st;
    return stat(path, &st) == 0;
}

// 대소문자 무시 부분 문자열 검색
static bool containsIgnoreCase(const char* haystack, const char* needle) {
    char h[PROP_VALUE_MAX], n[PROP_VALUE_MAX];
    int i;
    for (i = 0; haystack[i] && i < PROP_VALUE_MAX - 1; i++)
        h[i] = (char)tolower((unsigned char)haystack[i]);
    h[i] = '\0';
    for (i = 0; needle[i] && i < PROP_VALUE_MAX - 1; i++)
        n[i] = (char)tolower((unsigned char)needle[i]);
    n[i] = '\0';
    return strstr(h, n) != nullptr;
}

extern "C" {

// [AB] Build.prop 속성 기반 검사
JNIEXPORT jboolean JNICALL
Java_com_example_bgjz_1app_security_RootDetector_checkBuildAttributesNative(JNIEnv *env, jobject thiz) {
    bool isFound = false;
    char value[PROP_VALUE_MAX];

    LOGD("--- [AB] Build.prop 속성 검사 시작 ---");

    __system_property_get("ro.build.tags", value);
    if (strstr(value, "test-keys") != nullptr) {
        LOGE("[!] 탐지됨 (ro.build.tags): %s", value);
        isFound = true;
    } else {
        LOGD("[ ] 정상 (ro.build.tags): %s", value);
    }

    __system_property_get("ro.build.fingerprint", value);
    const char* fpKeywords[] = {"test-keys", "generic", "unknown", "vbox"};
    bool fpDetected = false;
    for (const char* key : fpKeywords) {
        if (containsIgnoreCase(value, key)) {
            LOGE("[!] 탐지됨 (ro.build.fingerprint): %s (키워드 '%s' 포함)", value, key);
            isFound = true;
            fpDetected = true;
            break;
        }
    }
    if (!fpDetected) LOGD("[ ] 정상 (ro.build.fingerprint): %s", value);

    __system_property_get("ro.secure", value);
    if (strcmp(value, "0") == 0) {
        LOGE("[!] 탐지됨 (ro.secure): 0 (보안 모드 비활성화)");
        isFound = true;
    } else {
        LOGD("[ ] 정상 (ro.secure): %s", value);
    }

    __system_property_get("ro.adb.secure", value);
    if (strcmp(value, "0") == 0) {
        LOGE("[!] 탐지됨 (ro.adb.secure): 0 (ADB 보안 미설정)");
        isFound = true;
    } else {
        LOGD("[ ] 정상 (ro.adb.secure): %s", value);
    }

    LOGD("--- [AB] 검사 종료 (최종 결과: %s) ---", isFound ? "true" : "false");
    return (jboolean)isFound;
}

// [PB] 파일 경로 존재 여부 검사
JNIEXPORT jboolean JNICALL
Java_com_example_bgjz_1app_security_RootDetector_checkPathsNative(JNIEnv *env, jobject thiz) {
    bool isFound = false;

    LOGD("--- [PB] 경로 검사 시작 ---");

    LOGD(">> [1] su 바이너리 경로 검사 중...");
    for (const char* path : SU_BINARY_PATHS) {
        if (pathExists(path)) {
            LOGE("[!] 탐지됨 (su 바이너리): %s", path);
            isFound = true;
        } else {
            LOGD("[ ] 없음 (su 바이너리): %s", path);
        }
    }

    LOGD(">> [2] 루트 파일 경로 검사 중...");
    for (const char* path : ROOT_PATHS) {
        if (pathExists(path)) {
            LOGE("[!] 탐지됨 (경로): %s", path);
            isFound = true;
        } else {
            LOGD("[ ] 없음 (경로): %s", path);
        }
    }

    LOGD("--- [PB] 검사 종료 (최종 결과: %s) ---", isFound ? "true" : "false");
    return (jboolean)isFound;
}

} // extern "C"
