package com.example.bgjz_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bgjz_app.ui.navigation.Route
import com.example.bgjz_app.ui.theme.BrandDarkGray
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandPurple

enum class NavTab(val label: String, val route: String, val icon: ImageVector) {
    HOME("홈", Route.Home.path, Icons.Filled.Home),
    SEARCH("검색", Route.Search.path, Icons.Filled.Search),
    REGISTER("등록", Route.ProductRegister.path, Icons.Filled.Add),
    CHAT("채팅", Route.ChatList.path, Icons.Filled.Chat),
    MY("마이", Route.MyPage.path, Icons.Filled.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    navController: NavHostController
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavTab.entries.forEach { tab ->
                val selected = currentRoute == tab.route
                if (tab == NavTab.REGISTER) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(BrandPurple, CircleShape)
                            .clickable { navController.navigate(tab.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clickable {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) BrandPurple else BrandGray,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) BrandPurple else BrandDarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}
