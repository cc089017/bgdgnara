package com.example.bgjz_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bgjz_app.ui.screens.auth.LoginScreen
import com.example.bgjz_app.ui.screens.auth.SignUpScreen
import com.example.bgjz_app.ui.screens.home.HomeScreen
import com.example.bgjz_app.ui.screens.mypage.MyPageScreen
import com.example.bgjz_app.ui.screens.mypage.ProfileEditScreen
import com.example.bgjz_app.ui.screens.onboarding.OnboardingScreen
import com.example.bgjz_app.ui.screens.product.AutoPriceScreen
import com.example.bgjz_app.ui.screens.product.ProductDetailScreen
import com.example.bgjz_app.ui.screens.product.ProductEditScreen
import com.example.bgjz_app.ui.screens.product.ProductRegisterScreen
import com.example.bgjz_app.ui.screens.userprofile.UserProfileScreen
import com.example.bgjz_app.ui.screens.splash.SplashScreen
import com.example.bgjz_app.ui.screens.wishlist.WishlistScreen
import com.example.bgjz_app.ui.screens.search.SearchScreen
import com.example.bgjz_app.ui.screens.admin.AdminScreen
import com.example.bgjz_app.ui.screens.chat.ChatListScreen
import com.example.bgjz_app.ui.screens.chat.ChatRoomScreen

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object SignUp : Route("signup")
    data object Home : Route("home")
    data object Wishlist : Route("wishlist")
    data object MyPage : Route("mypage")
    data object ProductRegister : Route("product_register")
    data object AutoPrice : Route("auto_price")
    data object ProfileEdit : Route("profile_edit")
    data object ProductDetail : Route("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    data object ProductEdit : Route("product_edit/{productId}") {
        fun createRoute(productId: Int) = "product_edit/$productId"
    }
    data object UserProfile : Route("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    data object Search : Route("search")
    data object Admin : Route("admin")
    data object ChatList : Route("chat_list")
    data object ChatRoom : Route("chat_room/{chatRoomId}") {
        fun createRoute(chatRoomId: Int) = "chat_room/$chatRoomId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Splash.path) {
        composable(Route.Splash.path) {
            SplashScreen(onTimeout = {
                navController.navigate(Route.Onboarding.path) {
                    popUpTo(Route.Splash.path) { inclusive = true }
                }
            })
        }
        composable(Route.Onboarding.path) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Onboarding.path) { inclusive = true }
                    }
                },
                onOtherWay = { navController.navigate(Route.SignUp.path) }
            )
        }
        composable(Route.Login.path) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onOtherWay = { navController.navigate(Route.SignUp.path) }
            )
        }
        composable(Route.SignUp.path) {
            SignUpScreen(
                onBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.SignUp.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Home.path) {
            HomeScreen(
                navController = navController,
                onProductClick = { productId ->
                    navController.navigate(Route.ProductDetail.createRoute(productId))
                },
                onAdminClick = { navController.navigate(Route.Admin.path) }
            )
        }
        composable(Route.Wishlist.path) {
            WishlistScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Route.ProductDetail.createRoute(productId))
                }
            )
        }
        composable(Route.MyPage.path) {
            MyPageScreen(
                navController = navController,
                onProfileEdit = { navController.navigate(Route.ProfileEdit.path) },
                onProductClick = { productId ->
                    navController.navigate(Route.ProductDetail.createRoute(productId))
                }
            )
        }
        composable(Route.ProfileEdit.path) {
            ProfileEditScreen(
                onBack = { navController.popBackStack() },
                onDeleteSuccess = {
                    navController.navigate(Route.Onboarding.path) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.ProductRegister.path) {
            ProductRegisterScreen(
                onBack = { navController.popBackStack() },
                onAutoPriceClick = { navController.navigate(Route.AutoPrice.path) },
                onRegister = { navController.popBackStack() }
            )
        }
        composable(Route.AutoPrice.path) {
            AutoPriceScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.Admin.path) {
            AdminScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.ChatList.path) {
            ChatListScreen(
                navController = navController,
                onChatRoomClick = { roomId ->
                    navController.navigate(Route.ChatRoom.createRoute(roomId))
                }
            )
        }
        composable(
            route = Route.ChatRoom.path,
            arguments = listOf(navArgument("chatRoomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getInt("chatRoomId") ?: return@composable
            ChatRoomScreen(
                chatRoomId = chatRoomId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Search.path) {
            SearchScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Route.ProductDetail.createRoute(productId))
                }
            )
        }
        composable(
            route = Route.ProductDetail.path,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Route.ProductEdit.createRoute(id)) },
                onDeleteSuccess = { navController.popBackStack() },
                onSellerClick = { sellerId -> navController.navigate(Route.UserProfile.createRoute(sellerId)) },
                onChatClick = {
                    // 백엔드 연결 시: POST /chats {productId} → 서버가 chatRoomId 반환
                    val roomId = com.example.bgjz_app.data.mock.MockData.chatRooms
                        .find { it.productId == productId }?.id
                        ?: com.example.bgjz_app.data.mock.MockData.chatRooms.firstOrNull()?.id
                        ?: 1
                    navController.navigate(Route.ChatRoom.createRoute(roomId))
                }
            )
        }
        composable(
            route = Route.ProductEdit.path,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            ProductEditScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.UserProfile.path,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            UserProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate(Route.ProductDetail.createRoute(productId)) }
            )
        }
    }
}
