package com.example.wifiindoorsystem

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    // 獲取資料庫實例
    val context = LocalContext.current
    val database = remember { ReferencePointDatabase.getInstance(context) }
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 統計數據狀態
    var referencePoints by remember { mutableStateOf<List<ReferencePoint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var lastUpdated by remember { mutableStateOf<Date?>(null) }
    
    // 分組統計數據
    val stats = remember(referencePoints) {
        referencePoints.groupBy { it.imageId }
            .map { (imageId, points) ->
                val mapImage = ReferencePointDatabase.availableMapImages.find { it.id == imageId }
                StatsItem(
                    mapName = mapImage?.name ?: "未知地圖",
                    mapId = imageId,
                    pointCount = points.size,
                    totalScans = points.sumOf { it.scanCount },
                    totalSignals = points.sumOf { it.wifiReadings.size }
                )
            }
            .sortedByDescending { it.pointCount }
    }
    
    // 總計數據
    val totalPoints = referencePoints.size
    val totalScans = referencePoints.sumOf { it.scanCount }
    val totalSignals = referencePoints.sumOf { it.wifiReadings.size }
    
    // 匯出檔案選擇器
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // 取得JSON資料
                    val jsonData = database.exportAllPointsToJson()
                    
                    // 寫入到使用者選擇的位置
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonData.toByteArray())
                    }
                    
                    scope.launch {
                        snackbarHostState.showSnackbar("已成功匯出參考點資料")
                    }
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("匯出失敗: ${e.localizedMessage}")
                    }
                }
            }
        }
    }
    
    // 載入參考點資料
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            referencePoints = database.referencePoints
            lastUpdated = Date()
        } catch (e: Exception) {
            // 處理錯誤
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("參考點統計與匯出") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // 重新整理按鈕
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                referencePoints = database.referencePoints
                                lastUpdated = Date()
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重新載入",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 總計統計卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "參考點統計總覽",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (lastUpdated != null) {
                            Text(
                                text = "更新: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(lastUpdated)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 大數字顯示總參考點數
                    Text(
                        text = "$totalPoints",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "個參考點",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 其他統計資訊
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            title = "總掃描次數",
                            value = totalScans.toString(),
                            icon = Icons.Default.Refresh
                        )
                        
                        StatItem(
                            title = "Wi-Fi 訊號數",
                            value = totalSignals.toString(),
                            icon = Icons.Default.Wifi
                        )
                        
                        StatItem(
                            title = "地圖數量",
                            value = stats.size.toString(),
                            icon = Icons.Default.Map
                        )
                    }
                }
            }
            
            // 匯出按鈕 - 移除了匯入按鈕，讓匯出按鈕佔據整個寬度
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/json"
                            putExtra(Intent.EXTRA_TITLE, "wifi_reference_points_${
                                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            }.json")
                        }
                        exportFileLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("匯出參考點資料")
                }
            }
            
            // 各地圖的參考點統計
            Text(
                text = "各地圖參考點分佈",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (stats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "尚未新增任何參考點",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(stats) { stat ->
                        MapStatItem(
                            stat = stat,
                            totalPoints = totalPoints
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun MapStatItem(
    stat: StatsItem,
    totalPoints: Int
) {
    val progress = if (totalPoints > 0) stat.pointCount.toFloat() / totalPoints else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnimation")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.mapName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${stat.pointCount} 點 (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 進度條
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 掃描次數與訊號數
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "掃描次數: ${stat.totalScans}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "Wi-Fi 訊號數: ${stat.totalSignals}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
// 統計數據類
data class StatsItem(
    val mapName: String,
    val mapId: Int,
    val pointCount: Int,
    val totalScans: Int,
    val totalSignals: Int
)
