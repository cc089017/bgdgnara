package com.example.bgjz_app.ui.screens.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.ProductDetail
import com.example.bgjz_app.ui.components.formatPrice
import com.example.bgjz_app.ui.theme.BrandDarkGray
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandLightGray
import com.example.bgjz_app.ui.theme.BrandPurple

@Composable
fun ProductDetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    onSellerClick: (String) -> Unit = {},
    onChatClick: () -> Unit = {},
    viewModel: ProductDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleteSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = BrandPurple,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.product != null -> {
                ProductDetailContent(
                    product = uiState.product!!,
                    onBack = onBack,
                    onLikeClick = { viewModel.toggleLike() },
                    onEdit = { onEdit(productId) },
                    onDelete = { viewModel.deleteProduct() },
                    onStatusChange = { viewModel.updateStatus(it) },
                    onSellerClick = { onSellerClick(uiState.product!!.sellerId) },
                    onChatClick = onChatClick
                )
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error!!,
                    color = BrandGray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: ProductDetail,
    onBack: () -> Unit,
    onLikeClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (ProductStatus) -> Unit,
    onSellerClick: () -> Unit,
    onChatClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("상품 삭제") },
            text = { Text("정말로 이 상품을 삭제하시겠어요?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("삭제", color = androidx.compose.ui.graphics.Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소", color = BrandGray)
                }
            }
        )
    }

    if (showStatusDialog) {
        val options = listOf(
            ProductStatus.ON_SALE to "판매중",
            ProductStatus.RESERVED to "예약중",
            ProductStatus.SOLD to "판매완료"
        )
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("상태 변경") },
            text = {
                Column {
                    options.forEach { (status, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStatusDialog = false; onStatusChange(status) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = product.status == status,
                                onClick = { showStatusDialog = false; onStatusChange(status) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                            )
                            Text(text = label, fontSize = 15.sp, color = BrandDarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("닫기", color = BrandGray)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp + navBarHeight)
        ) {
            // 이미지 캐러셀
            ImageCarousel(
                imageUrls = product.imageUrls,
                fallbackImageRes = product.imageRes,
                status = product.status,
            )

            // 상품 정보 영역
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // 판매자 정보
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSellerClick() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = product.sellerAvatarRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandLightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = product.sellerNickname, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(text = product.sellerRegion, fontSize = 13.sp, color = BrandGray)
                    }
                    Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = BrandGray)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BrandLightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // 상태 뱃지
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (product.status != ProductStatus.ON_SALE) {
                        StatusBadge(
                            text = when (product.status) {
                                ProductStatus.RESERVED -> "예약중"
                                ProductStatus.SOLD -> "판매완료"
                                else -> ""
                            },
                            color = if (product.status == ProductStatus.RESERVED) BrandPurple else BrandDarkGray
                        )
                    }
                    if (product.isLightningPay) {
                        StatusBadge(text = "번개페이", color = BrandPurple)
                    }
                }

                if (product.status != ProductStatus.ON_SALE || product.isLightningPay) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 상품명 & 가격
                Text(text = product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formatPrice(product.price), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                Spacer(modifier = Modifier.height(12.dp))

                // 카테고리 칩
                Box(
                    modifier = Modifier
                        .border(1.dp, BrandLightGray, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = product.category, fontSize = 12.sp, color = BrandGray)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BrandLightGray)
                Spacer(modifier = Modifier.height(16.dp))

                // 상품 설명
                Text(text = product.description, fontSize = 15.sp, color = BrandDarkGray, lineHeight = 24.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // 조회수 · 찜 · 시간
                Text(
                    text = "조회 ${product.viewCount} · 찜 ${product.likeCount} · ${product.timeAgo}",
                    fontSize = 12.sp,
                    color = BrandGray
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = BrandLightGray)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 상단 오버레이 버튼 (뒤로 / 더보기)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = "뒤로", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "더보기", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("수정하기") },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("상태 변경") },
                        onClick = { showMenu = false; showStatusDialog = true }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제", color = androidx.compose.ui.graphics.Color.Red) },
                        onClick = { showMenu = false; showDeleteDialog = true }
                    )
                }
            }
        }

        // 하단 바
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 1.dp, color = BrandLightGray, shape = RoundedCornerShape(0.dp))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onLikeClick, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = if (product.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "찜",
                    tint = if (product.isLiked) Color.Red else BrandDarkGray,
                    modifier = Modifier.size(26.dp)
                )
            }
            OutlinedButton(
                onClick = onChatClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandPurple)
            ) {
                Text(text = "채팅하기", color = BrandPurple, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {},
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                enabled = product.status == ProductStatus.ON_SALE
            ) {
                Text(
                    text = when (product.status) {
                        ProductStatus.ON_SALE -> "바로구매"
                        ProductStatus.RESERVED -> "예약중"
                        ProductStatus.SOLD -> "판매완료"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ImageCarousel(
    imageUrls: List<String>,
    fallbackImageRes: Int,
    status: ProductStatus,
) {
    val pageCount = imageUrls.size.takeIf { it > 0 } ?: 1
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val url = imageUrls.getOrNull(page)
            if (!url.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = fallbackImageRes),
                    error = painterResource(id = fallbackImageRes),
                )
            } else {
                Image(
                    painter = painterResource(id = fallbackImageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (status != ProductStatus.ON_SALE) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (status == ProductStatus.RESERVED) "예약중" else "판매완료",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 페이지 인디케이터
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/$pageCount",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}
