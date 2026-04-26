package com.example.bgjz_app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bgjz_app.R
import com.example.bgjz_app.data.mock.MockData
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.ui.components.BottomNavBar
import com.example.bgjz_app.ui.components.ProductCard
import com.example.bgjz_app.ui.navigation.Route
import com.example.bgjz_app.ui.theme.BrandDarkGray
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandLightGray
import com.example.bgjz_app.ui.theme.BrandPurple
import kotlinx.coroutines.delay

private data class ShortcutItem(val label: String, val icon: ImageVector, val route: String?)

@Composable
fun HomeScreen(
    navController: NavHostController,
    onProductClick: (Int) -> Unit = {},
    onAdminClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadProducts()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = Route.Home.path, navController = navController)
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            HomeTopBar(onAdminClick = onAdminClick)
            BannerCarousel(banners = uiState.banners)
            Spacer(modifier = Modifier.height(20.dp))
            ShortcutRow(navController = navController)
            Spacer(modifier = Modifier.height(24.dp))
            RecommendationHeader(nickname = MockData.currentUser.nickname)
            Spacer(modifier = Modifier.height(12.dp))
            ProductGrid(
                products = uiState.products,
                onProductClick = onProductClick,
                onLikeClick = { viewModel.toggleLike(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HomeTopBar(onAdminClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Menu, contentDescription = "메뉴", tint = Color.Black)
        }
        Text(
            text = "번개당근나라",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPurple,
            modifier = Modifier.clickable { onAdminClick() }
        )
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Notifications, contentDescription = "알림", tint = Color.Black)
        }
    }
}

@Composable
private fun BannerCarousel(banners: List<Banner>) {
    if (banners.isEmpty()) {
        PlaceholderBanner()
        return
    }
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(banners.size) {
        if (banners.size <= 1) return@LaunchedEffect
        while (true) {
            delay(3500)
            val next = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(BrandLightGray)
            ) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (!banner.title.isNullOrBlank()) {
                    Text(
                        text = banner.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { i ->
                val active = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (active) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (active) Color.White else Color.White.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun PlaceholderBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(BrandLightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "기본 배너",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "번개당근나라",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
        )
    }
}

@Composable
private fun ShortcutRow(navController: NavHostController) {
    val items = listOf(
        ShortcutItem("찜", Icons.Filled.Favorite, Route.Wishlist.path),
        ShortcutItem("최근본상품", Icons.Filled.AccessTime, null),
        ShortcutItem("우리동네", Icons.Filled.LocationOn, null),
        ShortcutItem("번개나눔", Icons.Filled.ShoppingBag, null),
        ShortcutItem("스크랩", Icons.Filled.Bookmark, null)
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { item.route?.let { navController.navigate(it) } }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(BrandLightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = item.icon, contentDescription = item.label, tint = BrandPurple, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.label, fontSize = 12.sp, color = BrandDarkGray)
            }
        }
    }
}

@Composable
private fun RecommendationHeader(nickname: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "취향 저격! ${nickname}님, 이건 어때요?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {}) {
            Text(text = "더보기", fontSize = 13.sp, color = BrandGray)
            Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = BrandGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ProductGrid(products: List<Product>, onProductClick: (Int) -> Unit, onLikeClick: (Product) -> Unit) {
    val rows = products.chunked(2)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ProductCard(
                        product = rowItems[0],
                        onClick = { onProductClick(rowItems[0].id) },
                        onLikeClick = { onLikeClick(rowItems[0]) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (rowItems.size > 1) {
                        ProductCard(
                            product = rowItems[1],
                            onClick = { onProductClick(rowItems[1].id) },
                            onLikeClick = { onLikeClick(rowItems[1]) }
                        )
                    }
                }
            }
        }
    }
}
