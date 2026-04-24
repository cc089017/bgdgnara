package com.example.bgjz_app.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bgjz_app.data.mock.MockData
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.mock.Seller
import com.example.bgjz_app.ui.components.formatPrice
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandLightGray
import com.example.bgjz_app.ui.theme.BrandPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("유저 관리", "상품 관리", "배너 관리")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("관리자", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandPurple
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 14.sp) }
                    )
                }
            }
            when (selectedTab) {
                0 -> UserManagementTab(uiState = uiState, viewModel = viewModel)
                1 -> ProductManagementTab(uiState = uiState, viewModel = viewModel)
                2 -> BannerUploadTab(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

// ─── 유저 관리 탭 ───────────────────────────────────────────

@Composable
private fun UserManagementTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    val users = remember(uiState.userQuery) { viewModel.filteredUsers() }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.userQuery,
            onValueChange = viewModel::onUserQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("닉네임 · 지역 검색", color = BrandGray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = BrandGray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandPurple,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        Text(
            "전체 ${users.size}명",
            fontSize = 13.sp,
            color = BrandGray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(users, key = { it.id }) { user ->
                UserCard(
                    seller = user,
                    isBanned = user.id in uiState.bannedUserIds,
                    onToggleBan = { viewModel.toggleBan(user.id) }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun UserCard(seller: Seller, isBanned: Boolean, onToggleBan: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isBanned) Color(0xFFFFF5F5) else Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrandLightGray),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(seller.avatarRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(seller.nickname, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (isBanned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "정지됨",
                        fontSize = 11.sp,
                        color = Color.Red,
                        modifier = Modifier
                            .background(Color(0xFFFFE0E0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${seller.region}  ·  매너점수 ${seller.mannerScore}",
                fontSize = 13.sp,
                color = BrandGray
            )
        }
        OutlinedButton(
            onClick = onToggleBan,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isBanned) BrandPurple else Color.Red
            ),
            border = BorderStroke(1.dp, if (isBanned) BrandPurple else Color.Red),
            modifier = Modifier.height(36.dp)
        ) {
            Text(if (isBanned) "정지 해제" else "정지", fontSize = 13.sp)
        }
    }
}

// ─── 상품 관리 탭 ───────────────────────────────────────────

@Composable
private fun ProductManagementTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    val products = remember(
        uiState.productQuery,
        uiState.productStatusFilter,
        uiState.deletedProductIds
    ) { viewModel.filteredProducts() }

    var productToDelete by remember { mutableStateOf<Int?>(null) }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("상품 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("이 상품을 삭제하시겠습니까?\n복구할 수 없습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(productToDelete!!)
                    productToDelete = null
                }) { Text("삭제", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("취소") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.productQuery,
            onValueChange = viewModel::onProductQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("상품명 검색", color = BrandGray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = BrandGray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandPurple,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        val filters = listOf(
            null to "전체",
            ProductStatus.ON_SALE to "판매중",
            ProductStatus.RESERVED to "예약중",
            ProductStatus.SOLD to "판매완료"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { (status, label) ->
                FilterChip(
                    selected = uiState.productStatusFilter == status,
                    onClick = { viewModel.setProductStatusFilter(status) },
                    label = { Text(label, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "전체 ${products.size}개",
            fontSize = 13.sp,
            color = BrandGray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(products, key = { it.id }) { product ->
                ProductRow(product = product, onDeleteClick = { productToDelete = product.id })
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, onDeleteClick: () -> Unit) {
    val seller = MockData.sellers.find { it.id == product.sellerId }
    val (statusLabel, statusColor) = when (product.status) {
        ProductStatus.ON_SALE -> "판매중" to Color(0xFF4CAF50)
        ProductStatus.RESERVED -> "예약중" to Color(0xFFFF9800)
        ProductStatus.SOLD -> "판매완료" to BrandGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrandLightGray)
        ) {
            Image(
                painter = painterResource(product.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(formatPrice(product.price), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    statusLabel,
                    fontSize = 11.sp,
                    color = statusColor,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    "판매자: ${seller?.nickname ?: "-"}",
                    fontSize = 12.sp,
                    color = BrandGray
                )
            }
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = Color(0xFFE53935))
        }
    }
}

// ─── 배너 관리 탭 ───────────────────────────────────────────

@Composable
private fun BannerUploadTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> if (uris.isNotEmpty()) viewModel.addBannerUris(uris) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("현재 배너", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(MockData.banners, key = { it.id }) { banner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandLightGray, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Image(
                        painter = painterResource(banner.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(banner.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text("새 배너 추가", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple),
                border = BorderStroke(1.dp, BrandPurple)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("갤러리에서 이미지 선택")
            }
        }
        if (uiState.pendingBannerUris.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "선택된 이미지 ${uiState.pendingBannerUris.size}장",
                    fontSize = 13.sp,
                    color = BrandGray
                )
            }
            items(uiState.pendingBannerUris, key = { it.toString() }) { uri ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        uri.lastPathSegment ?: "이미지",
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = BrandGray
                    )
                    IconButton(onClick = { viewModel.removeBannerUri(uri) }) {
                        Icon(Icons.Filled.Close, contentDescription = "제거", tint = BrandGray)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { viewModel.uploadBanners() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                    enabled = !uiState.uploadDone
                ) {
                    Text(
                        if (uiState.uploadDone) "업로드 완료" else "업로드 (${uiState.pendingBannerUris.size}장)",
                        color = Color.White
                    )
                }
                if (uiState.uploadDone) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "✓ 업로드 완료 — 백엔드 연결 시 POST /admin/upload 호출",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
