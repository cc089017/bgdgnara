package com.example.bgjz_app.security

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object RootDetector {

    private const val TAG = "RootDetectionLog"

    private val ROOT_PACKAGES = arrayOf("com.topjohnwu.magisk")

    init {
        System.loadLibrary("root_detector")
    }

    private external fun checkBuildAttributesNative(): Boolean
    private external fun checkPathsNative(): Boolean

    fun checkBuildAttributes(): Boolean = checkBuildAttributesNative()

    fun checkPaths(): Boolean = checkPathsNative()

    fun checkPackages(context: Context): Boolean {
        Log.d(TAG, ">> [3] 패키지 설치 여부 검사 중...")
        for (pkg in ROOT_PACKAGES) {
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                Log.e(TAG, "[!] 탐지됨 (패키지): $pkg")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                Log.d(TAG, "[ ] 없음 (패키지): $pkg")
            }
        }
        return false
    }

    fun checkAll(context: Context): Boolean {
        val ab = checkBuildAttributesNative()
        val pb = checkPathsNative()
        val pkg = checkPackages(context)
        val result = ab || pb || pkg
        Log.d(TAG, "=== [RootDetector] 전체 검사 결과: ${if (result) "루팅 탐지됨" else "정상"} ===")
        return result
    }
}
