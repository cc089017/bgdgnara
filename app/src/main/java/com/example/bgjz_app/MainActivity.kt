package com.example.bgjz_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bgjz_app.data.remote.RetrofitClient
import com.example.bgjz_app.ui.navigation.AppNavigation
import com.example.bgjz_app.ui.theme.Bgjz_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            Bgjz_appTheme {
                AppNavigation()
            }
        }
    }
}
