package com.example.wifiindoorsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    val scrollState = rememberScrollState()
    
    // 追踪各個指南區塊的展開狀態
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
    
    // 初始化展開狀態：默認第一個區塊展開
    LaunchedEffect(Unit) {
        expandedSections["quickstart"] = true
        expandedSections["map"] = false
        expandedSections["reference"] = false
        expandedSections["wifi"] = false
        expandedSections["faq"] = false
    }

    // 動畫進入效果
    val state = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("使用說明導覽") 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visibleState = state,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // 歡迎區塊
                WelcomeSection()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 快速入門指南
                GuideSection(
                    title = "快速入門",
                    icon = Icons.Default.RocketLaunch,
                    contentColor = MaterialTheme.colorScheme.primary,
                    isExpanded = expandedSections["quickstart"] ?: false,
                    onExpandToggle = { expandedSections["quickstart"] = it }
                ) {
                    QuickStartContent()
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 地圖使用指南
                GuideSection(
                    title = "地圖功能指南",
                    icon = Icons.Default.Map,
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    isExpanded = expandedSections["map"] ?: false,
                    onExpandToggle = { expandedSections["map"] = it }
                ) {
                    MapGuideContent()
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 參考點管理指南
                GuideSection(
                    title = "參考點管理指南",
                    icon = Icons.Default.LocationOn,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    isExpanded = expandedSections["reference"] ?: false,
                    onExpandToggle = { expandedSections["reference"] = it }
                ) {
                    ReferencePointGuideContent()
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Wi-Fi 掃描指南
                GuideSection(
                    title = "Wi-Fi 掃描說明",
                    icon = Icons.Default.Wifi,
                    contentColor = Color(0xFF00ACC1), // Cyan
                    isExpanded = expandedSections["wifi"] ?: false,
                    onExpandToggle = { expandedSections["wifi"] = it }
                ) {
                    WifiScanGuideContent()
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 常見問題
                GuideSection(
                    title = "常見問題解答",
                    icon = Icons.Default.QuestionAnswer,
                    contentColor = Color(0xFFFF9800), // Orange
                    isExpanded = expandedSections["faq"] ?: false,
                    onExpandToggle = { expandedSections["faq"] = it }
                ) {
                    FAQContent()
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 最後區塊：提示與聯絡資訊
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "仍需要協助？",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "歡迎至「GitHub Issues」回報問題或提出建議",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 應用程式圖示
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationSearching,
                contentDescription = "應用程式圖示",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Wi-Fi 室內定位系統",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "這個應用程式利用環境中的 Wi-Fi 訊號特徵建立「電子指紋」，實現高精度的室內位置識別",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun GuideSection(
    title: String,
    icon: ImageVector,
    contentColor: Color,
    isExpanded: Boolean,
    onExpandToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 可點擊的標題列
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = contentColor.copy(alpha = 0.1f),
                onClick = { onExpandToggle(!isExpanded) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收合" else "展開",
                        tint = contentColor
                    )
                }
            }
            
            // 可展開的內容區
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun QuickStartContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "開始使用 Wi-Fi 室內定位系統只需幾個簡單步驟：",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        StepItem(
            number = 1,
            title = "授予必要權限",
            description = "首次啟動應用程式時，請允許位置權限，這是掃描 Wi-Fi 網路所必需的"
        )
        
        StepItem(
            number = 2,
            title = "新增&掃描參考點",
            description = "選擇當前位置地圖並放大確認位置 → 將右上角切換成編輯新增模式 → 點擊當前位置選擇區域和掃描次數 → 耐心等待掃描完成"
        )
        
        StepItem(
            number = 3,
            title = "參考點統計與匯出",
            description = "確認資料為最新 → 按下匯出參考點資料 → 確認檔案儲存位置和檔名 → 儲存備份"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "提示：參考點越多，定位精度越高。建議在關鍵位置設置多個參考點。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MapGuideContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "互動式地圖是本系統的核心功能之一，讓您可以直觀地管理參考點：",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureItem(
            icon = Icons.Outlined.AspectRatio,
            title = "多地圖支援",
            description = "點擊頂部的地圖選項卡切換不同樓層或區域地圖"
        )
        
        FeatureItem(
            icon = Icons.Outlined.ZoomIn,
            title = "互動式操作",
            description = "使用捏合手勢進行縮放，拖動手勢移動地圖視角"
        )
        
        FeatureItem(
            icon = Icons.Outlined.Edit,
            title = "編輯模式",
            description = "開啟右上角的「編輯模式」開關，即可透過點擊地圖添加新參考點"
        )
        
        FeatureItem(
            icon = Icons.Outlined.Visibility,
            title = "檢視模式",
            description = "關閉右上角編輯模式時，點擊參考點可查看詳細資訊"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 地圖範例圖片 - 實際開發中可替換為真實截圖
        Image(
            painter = painterResource(id = R.drawable.floor_map),
            contentDescription = "地圖範例",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun ReferencePointGuideContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "參考點是定位系統的基礎，以下是完整的管理流程：",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 新增&掃描參考點區段
        Text(
            text = "新增&掃描參考點",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        StepItem(
            number = 1,
            title = "選擇當前位置地圖並放大地圖確認當前位置",
            description = "在「標記掃描」分頁中選擇對應的樓層地圖，使用縮放手勢放大地圖以準確定位"
        )
        
        StepItem(
            number = 2,
            title = "將右上角檢視模式切換成編輯新增",
            description = "開啟右上角的「編輯新增」開關，此時地圖底部會顯示編輯模式提示"
        )
        
        StepItem(
            number = 3,
            title = "點擊當前位置並選擇區域和掃描次數",
            description = "點擊地圖上的目標位置，選擇區域（只有1、2、3樓需要選擇a、b、c區域）和掃描次數"
        )
        
        StepItem(
            number = 4,
            title = "耐心等待掃描完成",
            description = "系統會顯示掃描進度，請保持在相同位置直到掃描完成，避免移動影響數據準確性"
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 參考點統計與匯出區段
        Text(
            text = "參考點統計與匯出",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        StepItem(
            number = 1,
            title = "確認資料為最新資料",
            description = "在「統計與匯出」分頁中，點擊重新整理按鈕確保顯示的是最新的參考點統計數據"
        )
        
        StepItem(
            number = 2,
            title = "按下匯出參考點資料",
            description = "點擊「匯出參考點資料」按鈕，系統將開啟檔案選擇器"
        )
        
        StepItem(
            number = 3,
            title = "確認檔案儲存位置（預設資料夾為下載）",
            description = "選擇合適的儲存位置，預設會指向下載資料夾，您也可以選擇其他位置"
        )
        
        StepItem(
            number = 4,
            title = "確認檔名（預設檔名為wifi_reference_points_當下日期_時間）",
            description = "檔名會自動包含時間戳記，格式為 wifi_reference_points_yyyyMMdd_HHmmss.json"
        )
        
        StepItem(
            number = 5,
            title = "都確認沒問題後儲存",
            description = "檢查無誤後點擊儲存"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "特別注意：1、2、3樓地圖需要選擇區域（a、b、c），其他樓層地圖已內建區域標識。建議定期匯出資料作為備份。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun WifiScanGuideContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Wi-Fi 掃描功能允許您查看和分析周圍的 Wi-Fi 網路：",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureItem(
            icon = Icons.Outlined.NetworkWifi,
            title = "自動掃描",
            description = "應用程式會定期掃描環境中的 Wi-Fi 訊號"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "注意：頻繁的 Wi-Fi 掃描可能會增加電池消耗。系統預設的掃描間隔已經過優化。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun FAQContent() {
    Column(modifier = Modifier.fillMaxWidth()) {
        FAQItem(
            question = "為什麼需要位置權限？",
            answer = "Android 系統規定，應用程式需要位置權限才能掃描 Wi-Fi 網路，這是系統安全機制的要求，而非應用程式本身的限制。"
        )
        
        FAQItem(
            question = "如何提高定位準確度？",
            answer = "在使用環境中設置更多參考點、增加每個參考點的掃描次數，並確保參考點分佈均勻，都能有效提高定位準確度。"
        )
    }
}

@Composable
fun StepItem(number: Int, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 步驟編號
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 步驟說明
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 功能圖標
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 功能說明
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QuestionMark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = question,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
        
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 28.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.padding(start = 28.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GuideScreenPreview() {
    MaterialTheme {
        GuideScreen()
    }
}
