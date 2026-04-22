package com.example.bgjz_app.ui.screens.mypage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bgjz_app.R
import com.example.bgjz_app.ui.screens.auth.UnderlineField
import com.example.bgjz_app.ui.theme.BrandGray
import com.example.bgjz_app.ui.theme.BrandLightGray
import com.example.bgjz_app.ui.theme.BrandPurple

@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.profile

    var nickname by remember(profile?.nickname) { mutableStateOf(profile?.nickname ?: "") }
    var region by remember(profile?.region) { mutableStateOf(profile?.region ?: "") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }
    var currentPwVisible by remember { mutableStateOf(false) }
    var newPwVisible by remember { mutableStateOf(false) }
    var newPwConfirmVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            localAvatarUri = uri
            viewModel.uploadAvatar(uri.toString())
        }
    }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            viewModel.clearStatus()
            onBack()
        }
    }

    LaunchedEffect(uiState.isPasswordChangeSuccess) {
        if (uiState.isPasswordChangeSuccess) {
            viewModel.clearStatus()
            currentPassword = ""
            newPassword = ""
            newPasswordConfirm = ""
            passwordError = null
        }
    }

    LaunchedEffect(uiState.isDeleteSuccess) {
        if (uiState.isDeleteSuccess) {
            viewModel.clearStatus()
            onDeleteSuccess()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("회원 탈퇴", fontWeight = FontWeight.Bold) },
            text = { Text("정말 탈퇴하시겠어요?\n탈퇴 시 모든 데이터가 삭제됩니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("탈퇴", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소", color = BrandGray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "뒤로",
                    tint = Color.Black
                )
            }
            Text(
                text = "프로필 수정",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 아바타
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(BrandLightGray)
                    .border(2.dp, BrandLightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = localAvatarUri ?: uiState.profile?.avatarUrl,
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(BrandPurple)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "사진 변경",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 프로필 정보
        UnderlineField(label = "닉네임", value = nickname, onValueChange = { nickname = it })
        Spacer(modifier = Modifier.height(24.dp))
        UnderlineField(
            label = "지역",
            value = region,
            onValueChange = { region = it },
            placeholder = "예) 서울 강남구"
        )

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.error!!,
                fontSize = 13.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.updateProfile(nickname, region) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("저장", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        HorizontalDivider(color = BrandLightGray)
        Spacer(modifier = Modifier.height(32.dp))

        // 비밀번호 변경
        Text(
            text = "비밀번호 변경",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        UnderlineField(
            label = "현재 비밀번호",
            value = currentPassword,
            onValueChange = { currentPassword = it },
            visualTransformation = if (currentPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { currentPwVisible = !currentPwVisible }) {
                    Icon(
                        imageVector = if (currentPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = BrandGray
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        UnderlineField(
            label = "새 비밀번호",
            value = newPassword,
            onValueChange = { newPassword = it },
            visualTransformation = if (newPwVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { newPwVisible = !newPwVisible }) {
                    Icon(
                        imageVector = if (newPwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = BrandGray
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        UnderlineField(
            label = "새 비밀번호 확인",
            value = newPasswordConfirm,
            onValueChange = { newPasswordConfirm = it },
            visualTransformation = if (newPwConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { newPwConfirmVisible = !newPwConfirmVisible }) {
                    Icon(
                        imageVector = if (newPwConfirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = BrandGray
                    )
                }
            }
        )

        if (passwordError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = passwordError!!,
                fontSize = 13.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (newPassword != newPasswordConfirm) {
                    passwordError = "새 비밀번호가 일치하지 않습니다"
                } else {
                    passwordError = null
                    viewModel.changePassword(currentPassword, newPassword)
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Text("비밀번호 변경", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
        HorizontalDivider(color = BrandLightGray)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "회원 탈퇴",
            fontSize = 14.sp,
            color = Color.Red.copy(alpha = 0.7f),
            modifier = Modifier
                .clickable { showDeleteDialog = true }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
