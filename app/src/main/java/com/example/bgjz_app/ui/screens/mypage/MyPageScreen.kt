package com.example.bgjz_app.ui.screens.mypage

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.bgjz_app.R
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.ui.components.BottomNavBar
import com.example.bgjz_app.ui.components.ProductCard
import com.example.bgjz_app.ui.navigation.Route
import com.example.bgjz_app.ui.theme.BrandDarkGray
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandLightGray
import com.example.bgjz_app.ui.theme.BrandPurple

private enum class StatusFilter(val label: String) {
    ALL("전체"), ON_SALE("판매중"), SOLD("판매완료"), RESERVED("예약중")
}

private enum class SortOption(val label: String) {
    HIGH("고가순"), LOW("저가순"), POPULAR("인기순")
}

@Composable
fun MyPageScreen(
    navController: NavHostController,
    onProfileEdit: () -> Unit = {},
    onProductClick: (Int) -> Unit = {},
    userViewModel: UserViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    var filter by remember { mutableStateOf(StatusFilter.ALL) }
    var sort by remember { mutableStateOf(SortOption.HIGH) }

    val uiState by userViewModel.uiState.collectAsState()
    val productUiState by productViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = Route.MyPage.path, navController = navController)
        },
        containerColor = Color.White
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPurple)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, top = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = onProfileEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "프로필 수정",
                        tint = BrandDarkGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BrandLightGray),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uiState.profile?.avatarUrl,
                        contentDescription = uiState.profile?.nickname,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        error = painterResource(R.drawable.ic_launcher_foreground),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                Column {
                    Text(
                        text = uiState.profile?.nickname ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = uiState.profile?.email ?: "",
                        fontSize = 14.sp,
                        color = BrandGray
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusFilter.entries.forEach { f ->
                    FilterChip(
                        text = f.label,
                        selected = filter == f,
                        onClick = { filter = f }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                SortOption.entries.forEach { s ->
                    Text(
                        text = s.label,
                        fontSize = 13.sp,
                        color = if (sort == s) BrandPurple else BrandGray,
                        fontWeight = if (sort == s) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { sort = s }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val filtered = filterAndSort(productUiState.myProducts, filter, sort)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) BrandPurple else BrandLightGray)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else BrandDarkGray,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun filterAndSort(
    list: List<Product>,
    filter: StatusFilter,
    sort: SortOption
): List<Product> {
    val filtered = when (filter) {
        StatusFilter.ALL -> list
        StatusFilter.ON_SALE -> list.filter { it.status == ProductStatus.ON_SALE }
        StatusFilter.SOLD -> list.filter { it.status == ProductStatus.SOLD }
        StatusFilter.RESERVED -> list.filter { it.status == ProductStatus.RESERVED }
    }
    return when (sort) {
        SortOption.HIGH -> filtered.sortedByDescending { it.price }
        SortOption.LOW -> filtered.sortedBy { it.price }
        SortOption.POPULAR -> filtered
    }
}
