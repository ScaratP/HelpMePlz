package com.example.wifiindoorsystem

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.SignalWifi0Bar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * 全局權限包裝器，用於檢查位置權限
 * 如果權限未授予，顯示權限請求畫面
 * 如果權限已授予，顯示子內容
 */
@Composable
fun LocationPermissionWrapper(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // 記錄權限請求結果
    var permissionRequested by remember { mutableStateOf(false) }

    // 建立權限請求器
    val requestLocationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // 檢查是否所有需要的權限都獲得了
            val allGranted = permissions.entries.all { it.value }
            permissionRequested = true
            // 如果權限仍未授予，可以在這裡處理額外邏輯
        }
    )

    // 檢查精確位置權限
    val fineLocationPermissionGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    // 檢查粗略位置權限
    val coarseLocationPermissionGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    // 權限已授予，顯示正常內容
    if (fineLocationPermissionGranted && coarseLocationPermissionGranted) {
        content()
    } else {
        // 權限未授予，顯示權限請求畫面
        GlobalPermissionRequiredScreen(
            onRequestPermission = {
                // 同時請求精確和粗略位置權限
                requestLocationPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            // 如果已經請求過權限但用戶拒絕，顯示設置按鈕
            showSettings = permissionRequested
        )
    }
}

/**
 * 全局權限請求畫面
 * 替代各個畫面中重複的 PermissionRequiredScreen
 */
@Composable
fun GlobalPermissionRequiredScreen(
    onRequestPermission: () -> Unit,
    showSettings: Boolean = false
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "需要位置權限",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Wi-Fi定位系統需要位置權限才能掃描周圍的Wi-Fi網路，請授予位置權限以繼續使用應用程式。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // 主要權限請求按鈕
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("授予權限")
                }
                
                // 如果已經請求過但被拒絕，顯示前往設定的按鈕
                if (showSettings) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            // 前往應用程式設定頁面
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("前往設定")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "如果無法授予權限，請前往設定手動啟用位置權限",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
