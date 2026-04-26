package com.example.bgjz_app.ui.screens.admin

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.bgjz_app.R
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.data.model.UserProfile
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.accessDenied -> AccessDeniedView()
                uiState.isLoading && uiState.users.isEmpty() && uiState.banners.isEmpty()
                        && uiState.products.isEmpty() -> LoadingView()
                else -> Column(modifier = Modifier.fillMaxSize()) {
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
                        0 -> UserManagementTab(uiState, viewModel)
                        1 -> ProductManagementTab(uiState, viewModel)
                        2 -> BannerManagementTab(uiState, viewModel)
                    }
                }
            }
        }
    }
}

// ─── 권한 없음 / 로딩 ───────────────────────────────────────

@Composable
private fun AccessDeniedView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = BrandGray,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("접근 권한이 없습니다", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "관리자 권한이 있는 계정으로 로그인해주세요.",
            fontSize = 13.sp,
            color = BrandGray
        )
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandPurple)
    }
}

// ─── 유저 관리 탭 ───────────────────────────────────────────

@Composable
private fun UserManagementTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    val users = remember(uiState.userQuery, uiState.users) { viewModel.filteredUsers() }
    var pendingToggle by remember { mutableStateOf<UserProfile?>(null) }

    pendingToggle?.let { target ->
        val grant = !target.isAdmin
        AlertDialog(
            onDismissRequest = { pendingToggle = null },
            title = { Text(if (grant) "관리자 권한 부여" else "관리자 권한 해제", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${target.nickname} (${target.id})\n${if (grant) "이 사용자를 관리자로 지정합니다." else "이 사용자의 관리자 권한을 해제합니다."}"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleUserAdmin(target.id)
                    pendingToggle = null
                }) {
                    Text(if (grant) "권한 부여" else "권한 해제", color = BrandPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingToggle = null }) { Text("취소") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.userQuery,
            onValueChange = viewModel::onUserQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("닉네임 · 아이디 · 지역 검색", color = BrandGray, fontSize = 14.sp) },
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
                    user = user,
                    isMe = user.id == uiState.currentUserId,
                    onToggleClick = { pendingToggle = user }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun UserCard(user: UserProfile, isMe: Boolean, onToggleClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            if (!user.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = BrandGray)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.nickname, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (user.isAdmin) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "관리자",
                        fontSize = 11.sp,
                        color = BrandPurple,
                        modifier = Modifier
                            .background(BrandPurple.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (isMe) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "본인",
                        fontSize = 11.sp,
                        color = BrandGray,
                        modifier = Modifier
                            .background(BrandLightGray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${user.id}  ·  ${user.region.orEmpty()}",
                fontSize = 13.sp,
                color = BrandGray
            )
        }
        OutlinedButton(
            onClick = onToggleClick,
            shape = RoundedCornerShape(8.dp),
            enabled = !isMe,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (user.isAdmin) Color.Red else BrandPurple
            ),
            border = BorderStroke(1.dp, if (isMe) Color(0xFFE0E0E0) else if (user.isAdmin) Color.Red else BrandPurple),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                if (user.isAdmin) "권한 해제" else "관리자 지정",
                fontSize = 13.sp
            )
        }
    }
}

// ─── 상품 관리 탭 ───────────────────────────────────────────

@Composable
private fun ProductManagementTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    val products = remember(uiState.products, uiState.productStatusFilter) {
        viewModel.filteredProducts()
    }
    var productToDelete by remember { mutableStateOf<Int?>(null) }

    productToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("상품 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("이 상품을 삭제하시겠습니까?\n복구할 수 없습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(id)
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
            if (!product.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = product.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(product.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
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
                    "판매자: ${product.sellerId}",
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
private fun BannerManagementTab(uiState: AdminUiState, viewModel: AdminViewModel) {
    var imageUrl by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var bannerToDelete by remember { mutableStateOf<Banner?>(null) }

    bannerToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { bannerToDelete = null },
            title = { Text("배너 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("'${target.title ?: "(제목 없음)"}' 배너를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBanner(target.id)
                    bannerToDelete = null
                }) { Text("삭제", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { bannerToDelete = null }) { Text("취소") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("현재 배너 (${uiState.banners.size}개)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        if (uiState.banners.isEmpty()) {
            item {
                Text(
                    "등록된 배너가 없습니다",
                    fontSize = 13.sp,
                    color = BrandGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        items(uiState.banners, key = { it.id }) { banner ->
            BannerRow(banner = banner, onDelete = { bannerToDelete = banner })
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text("새 배너 등록", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "이미지는 외부 URL(CDN/이미지 호스팅)을 입력해주세요. 백엔드는 URL만 저장합니다.",
                fontSize = 12.sp,
                color = BrandGray
            )
        }
        item {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("이미지 URL *") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple)
            )
        }
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("타이틀 (선택)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple)
            )
        }
        item {
            OutlinedTextField(
                value = linkUrl,
                onValueChange = { linkUrl = it },
                label = { Text("이동할 링크 URL (선택)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple)
            )
        }
        if (imageUrl.isNotBlank()) {
            item {
                Text("미리보기", fontSize = 13.sp, color = BrandGray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandLightGray)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "미리보기",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.createBanner(
                        imageUrl = imageUrl,
                        title = title,
                        linkUrl = linkUrl,
                    )
                    imageUrl = ""
                    title = ""
                    linkUrl = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                enabled = imageUrl.isNotBlank()
            ) {
                Text("배너 등록", color = Color.White)
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun BannerRow(banner: Banner, onDelete: () -> Unit) {
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
                .background(Color.White)
        ) {
            AsyncImage(
                model = banner.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_background)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                banner.title ?: "(제목 없음)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!banner.linkUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    banner.linkUrl,
                    fontSize = 11.sp,
                    color = BrandGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = Color(0xFFE53935))
        }
    }
}
