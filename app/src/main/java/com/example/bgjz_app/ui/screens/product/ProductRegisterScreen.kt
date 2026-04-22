package com.example.bgjz_app.ui.screens.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bgjz_app.ui.screens.auth.UnderlineField
import com.example.bgjz_app.ui.theme.BrandDarkGray
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandLightGray
import com.example.bgjz_app.ui.theme.BrandPurple

@Composable
fun ProductRegisterScreen(
    onBack: () -> Unit,
    onAutoPriceClick: () -> Unit,
    onRegister: () -> Unit,
    viewModel: ProductRegisterViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var shippingIncluded by remember { mutableStateOf(true) }
    var autoPriceDown by remember { mutableStateOf(false) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val uiState by viewModel.uiState.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val remaining = 10 - imageUris.size
        imageUris = imageUris + uris.take(remaining)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegister()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = "뒤로", tint = Color.Black)
            }
            Text(text = "상품 등록", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            // 이미지 선택 영역
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BrandGray, RoundedCornerShape(12.dp))
                        .background(BrandLightGray)
                        .clickable { if (imageUris.size < 10) imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "이미지 추가", tint = BrandDarkGray, modifier = Modifier.size(28.dp))
                        Text(text = "${imageUris.size}/10", fontSize = 12.sp, color = BrandDarkGray)
                    }
                }
                imageUris.take(4).forEachIndexed { index, uri ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                        )
                        IconButton(
                            onClick = { imageUris = imageUris.toMutableList().also { it.removeAt(index) } },
                            modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            UnderlineField(label = "상품명", value = name, onValueChange = { name = it })
            Spacer(modifier = Modifier.height(20.dp))
            UnderlineField(
                label = "카테고리",
                value = category,
                onValueChange = { category = it },
                placeholder = "카테고리 선택",
                trailingIcon = {
                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = BrandGray)
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            UnderlineField(
                label = "가격",
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() } },
                placeholder = "₩ 가격을 입력하세요"
            )
            Spacer(modifier = Modifier.height(28.dp))

            Text(text = "배송비", fontSize = 14.sp, color = BrandDarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShippingChip(text = "포함", selected = shippingIncluded, onClick = { shippingIncluded = true })
                ShippingChip(text = "별도", selected = !shippingIncluded, onClick = { shippingIncluded = false })
            }
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAutoPriceClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "자동 가격 내림", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text(text = "주기적으로 가격을 자동 인하합니다", fontSize = 12.sp, color = BrandGray)
                }
                Switch(
                    checked = autoPriceDown,
                    onCheckedChange = { autoPriceDown = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandPurple)
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = uiState.error!!, fontSize = 13.sp, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    viewModel.register(
                        name = name,
                        category = category,
                        price = price.toIntOrNull() ?: 0,
                        shippingIncluded = shippingIncluded,
                        autoPriceDown = autoPriceDown,
                        imageUris = imageUris.map { it.toString() }
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "등록하기", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShippingChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) BrandPurple else BrandLightGray)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else BrandDarkGray,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
