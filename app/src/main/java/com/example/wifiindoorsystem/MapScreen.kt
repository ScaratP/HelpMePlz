package com.example.wifiindoorsystem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.toArgb
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.ortiz.touchview.OnTouchImageViewListener
import com.ortiz.touchview.TouchImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlin.random.Random

// 自定義的 TouchImageView，新增了一些輔助方法和手勢處理
class MyCustomImageView(context: Context, attrs: AttributeSet? = null) : TouchImageView(context, attrs) {
    // 提供公開的方法來使用protected方法
    fun useTransformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        return transformCoordTouchToBitmap(x, y, clipToBitmap)
    }

    fun useTransformCoordBitmapToTouch(x: Float, y: Float): PointF {
        return transformCoordBitmapToTouch(x, y)
    }

    // 新增標誌位，用於判斷是否正在進行手勢操作
    var isGestureInProgress = false
    
    // 可重用的 Paint 對象，避免反覆創建
    private val pointPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 30f
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    
    private val borderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = android.graphics.Color.WHITE
    }
    
    // 繪製參考點的方法移動到自定義視圖
    fun drawReferencePointsOnCanvas(canvas: Canvas, points: List<ReferencePoint>) {
        if (drawable == null) return
        
        // 取得圖片原始尺寸
        val bitmapWidth = drawable.intrinsicWidth.toFloat()
        val bitmapHeight = drawable.intrinsicHeight.toFloat()
        
        // 如果正在進行手勢操作，只繪製簡化版的參考點
        val shouldDrawSimplified = isGestureInProgress
        
        // 繪製每個參考點
        points.forEach { point ->
            // 計算參考點在圖片上的絕對位置
            val pointXOnBitmap = (point.x / 100f * bitmapWidth)
            val pointYOnBitmap = (point.y / 100f * bitmapHeight)
            
            // 將點從圖片座標轉換為螢幕座標
            val bitmapPoint = PointF(pointXOnBitmap.toFloat(), pointYOnBitmap.toFloat())
            val mappedPoint = useTransformCoordBitmapToTouch(bitmapPoint.x, bitmapPoint.y)
            
            // 設置點的顏色
            pointPaint.color = getPointColor(point).toArgb()
            
            // 繪製外圓
            canvas.drawCircle(mappedPoint.x, mappedPoint.y, if (shouldDrawSimplified) 15f else 30f, pointPaint)
            
            // 如果不是簡化模式，繪製完整的參考點
            if (!shouldDrawSimplified) {
                // 繪製邊框
                canvas.drawCircle(mappedPoint.x, mappedPoint.y, 30f, borderPaint)
                
                // 繪製標籤文字
                canvas.drawText(
                    point.name.take(1), 
                    mappedPoint.x,
                    mappedPoint.y + textPaint.textSize / 3,
                    textPaint
                )
                
                // 繪製名稱標籤
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.color = getPointColor(point).toArgb()
                canvas.drawText(
                    point.name,
                    mappedPoint.x + 40f,
                    mappedPoint.y + 10f,
                    textPaint
                )
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.color = android.graphics.Color.WHITE
            }
        }
    }
    
    // 為參考點生成一致的顏色
    private fun getPointColor(point: ReferencePoint): Color {
        val hash = point.id.hashCode()
        return Color(
            red = ((hash and 0xFF0000) shr 16) / 255f,
            green = ((hash and 0x00FF00) shr 8) / 255f,
            blue = (hash and 0x0000FF) / 255f,
            alpha = 1f
        )
    }

    // 新增：存放要繪製的參考點
    var overlayPoints: List<ReferencePoint> = emptyList()

    // 修改：存放目前的圖片 ID 和正確的地圖圖片
    var currentImageId: Int = R.drawable.floor_map
    var currentMapImage: MapImage? = null
    
    // 修改繪製方法，刪除繪製當前位置的調用
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 繪製最新的參考點
        drawReferencePointsOnCanvas(canvas, overlayPoints)
    }
}

// 圖片選項卡組件
@Composable
fun MapImageTabItem(
    mapImage: MapImage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        border = if (!isSelected)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = mapImage.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    currentMapImage: MapImage? = null,
    currentPosition: CurrentPosition? = null // 添加參數
) {
    // 圖片尺寸狀態
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    
    // TouchImageView 參考
    val touchImageViewRef = remember { mutableStateOf<TouchImageView?>(null) }
    
    // 保存自定義 TouchImageView 引用
    val customImageViewRef = remember { mutableStateOf<MyCustomImageView?>(null) }
    
    // 當前選擇的圖片ID
    var currentImageId by remember { mutableStateOf(ReferencePointDatabase.availableMapImages.first().id) }
    
    // 參考點列表 - 修改為保存所有參考點，然後按當前圖片ID過濾
    var allReferencePoints by remember { mutableStateOf(listOf<ReferencePoint>()) }
    val filteredReferencePoints = remember(allReferencePoints, currentImageId) {
        // 過濾出當前圖片的參考點
        allReferencePoints.filter { it.imageId == currentImageId }
    }
    
    // 輸入狀態
    var xInput by remember { mutableStateOf("") }
    var yInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    
    // 對話框狀態
    var showAddDialog by remember { mutableStateOf(false) }
    var tapPosition by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    
    // 新增: 區域選擇狀態
    var selectedArea by remember { mutableStateOf("a") }
    
    // 新增: 掃描次數選擇
    var scanCount by remember { mutableStateOf(3) }
    
    // 新增: 是否正在掃描
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0) }
    var scanTotal by remember { mutableStateOf(0) }
    
    // 鍵盤控制
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 資料庫實例
    val context = LocalContext.current
    val database = remember { ReferencePointDatabase.getInstance(context) }
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }

    // 新增效能監控
    var lastFrameTime by remember { mutableStateOf(0L) }

    // 添加模式切換狀態 - true 表示編輯模式，false 表示檢視模式
    var isEditMode by remember { mutableStateOf(false) }

    // 控制參考點列表的展開/收起狀態
    var isListExpanded by remember { mutableStateOf(true) }

    // 添加刷新狀態
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("參考點標記掃描") },
                    colors = TopAppBarDefaults.topAppBarColors( // Explicitly set TopAppBar colors
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        // 添加刷新按鈕
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isRefreshing = true
                                    try {
                                        // 重新載入參考點資料
                                        allReferencePoints = database.referencePoints
                                        snackbarHostState.showSnackbar("已重新載入參考點資料")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("載入失敗: ${e.localizedMessage}")
                                    } finally {
                                        isRefreshing = false
                                    }
                                }
                            }
                        ) {
                            if (isRefreshing) {
                                // 刷新動畫
                                val infiniteTransition = rememberInfiniteTransition(label = "refreshAnimation")
                                val rotation by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "rotationAnimation"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "正在刷新",
                                    modifier = Modifier.rotate(rotation),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "刷新參考點",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        
                        // 添加模式切換開關
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isEditMode) "編輯新增" else "檢視模式",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isEditMode) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    LocalContentColor.current // Inherit from TopAppBar's actionIconContentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isEditMode,
                                onCheckedChange = { isEditMode = it },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (isEditMode)
                                            Icons.Default.Edit
                                        else
                                            Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isEditMode) { // Tint for Icon inside Switch thumb
                                            MaterialTheme.colorScheme.onError // Contrasts with error thumb
                                        } else {
                                            // Icon color for unchecked state, contrasts with uncheckedThumbColor
                                            MaterialTheme.colorScheme.primary 
                                        }
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.error,
                                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline, // Example color for unchecked thumb
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant // Example color for unchecked track
                                )
                            )
                        }
                    }
                )
                
                // 圖片分類按鈕橫向滾動列表
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // 顯示所有可用地圖圖片供選擇
                        ReferencePointDatabase.availableMapImages.forEachIndexed { index, mapImage ->
                            MapImageTabItem( // Corrected typo from MapImageTabltem
                                mapImage = mapImage,
                                isSelected = currentImageId == mapImage.id,
                                onClick = {
                                    currentImageId = mapImage.id
                                    customImageViewRef.value?.setImageResource(mapImage.id)
                                }
                            )
                            
                            // 添加分隔空間（最後一個項目除外）
                            if (index < ReferencePointDatabase.availableMapImages.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                HorizontalDivider()
                }
            }
        }
    ) { paddingValues ->
        // 修改為Column，但不使用整個頁面的verticalScroll
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 地圖部分 - 固定不動
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Changed from Color.LightGray
            ) {
                AndroidView(
                    factory = { ctx ->
                        // 使用自定義的 MyCustomImageView 而非基本的 TouchImageView
                        MyCustomImageView(ctx).apply {
                            // 開啟硬體加速，提高繪圖效能
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            
                            customImageViewRef.value = this
                            touchImageViewRef.value = this
                            setImageResource(currentImageId) // 使用當前選擇的圖片ID
                            
                            // 如果有目前位置，自動選擇匹配的地圖
                            currentPosition?.let { pos ->
                                // 尋找正確的地圖（如果有）
                                currentMapImage?.let { mapImg ->
                                    if (currentImageId != mapImg.id) {
                                        currentImageId = mapImg.id
                                        setImageResource(mapImg.id)
                                    }
                                }
                            }
                            
                            // 設置最大/最小縮放級別
                            maxZoom = 4f
                            minZoom = 0.8f
                            
                            // 優化觸控事件處理
                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        isGestureInProgress = true
                                        // 不立即重繪，而是在下一個動畫幀重繪
                                        postInvalidateOnAnimation()
                                        false
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        // 在編輯模式下，快速響應點擊以添加參考點
                                        if (isEditMode) {
                                            // 使用自定義方法進行座標轉換
                                            val mappedPoint = useTransformCoordTouchToBitmap(event.x, event.y, true)
                                            
                                            // 確保點擊的點在圖片範圍內
                                            val bitmapWidth = drawable.intrinsicWidth
                                            val bitmapHeight = drawable.intrinsicHeight
                                            if (mappedPoint.x >= 0 && mappedPoint.x <= bitmapWidth &&
                                                mappedPoint.y >= 0 && mappedPoint.y <= bitmapHeight) {
                                                
                                                // 計算百分比座標
                                                val percentX = mappedPoint.x / bitmapWidth * 100f
                                                val percentY = mappedPoint.y / bitmapHeight * 100f
                                                
                                                // 儲存點擊位置並顯示對話框
                                                tapPosition = Pair(percentX, percentY)
                                                showAddDialog = true
                                                v.performClick()
                                                return@setOnTouchListener true
                                            }
                                        } else {
                                            // 檢視模式：需要符合點擊時間閾值才視為點擊
                                            val currentTime = System.currentTimeMillis()
                                            if (currentTime - lastFrameTime < 300 && event.pointerCount == 1) {
                                                // 使用自定義方法進行座標轉換
                                                val mappedPoint = useTransformCoordTouchToBitmap(event.x, event.y, true)
                                                
                                                // 確保點擊的點在圖片範圍內
                                                val bitmapWidth = drawable.intrinsicWidth
                                                val bitmapHeight = drawable.intrinsicHeight
                                                if (mappedPoint.x >= 0 && mappedPoint.x <= bitmapWidth &&
                                                    mappedPoint.y >= 0 && mappedPoint.y <= bitmapHeight) {
                                                    
                                                    // 計算百分比座標
                                                    val percentX = mappedPoint.x / bitmapWidth * 100f
                                                    val percentY = mappedPoint.y / bitmapHeight * 100f
                                                    
                                                    // 儲存點擊位置並顯示對話框
                                                    tapPosition = Pair(percentX, percentY)
                                                    showAddDialog = true
                                                    v.performClick()
                                                    return@setOnTouchListener true
                                                }
                                            }
                                        }
                                        
                                        // 標記手勢結束，但不立即重繪
                                        isGestureInProgress = false
                                        // 延遲重繪，確保UI流暢性
                                        postDelayed({ postInvalidateOnAnimation() }, 100)
                                        lastFrameTime = System.currentTimeMillis()
                                        false
                                    }
                                    MotionEvent.ACTION_CANCEL -> {
                                        isGestureInProgress = false
                                        postDelayed({ postInvalidateOnAnimation() }, 100)
                                        false
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        // 移動時保持手勢標記，但不頻繁重繪
                                        if (!isGestureInProgress) {
                                            isGestureInProgress = true
                                            postInvalidateOnAnimation()
                                        }
                                        false
                                    }
                                    else -> false
                                }
                            }
                            
                            // 重新實現自定義覆蓋層，使用更高效的方式
                            post {
                                imageSize = IntSize(drawable.intrinsicWidth, drawable.intrinsicHeight)
                                
                                // 建立高效率渲染的覆蓋層
                                val overlayView = object : View(context) {
                                    init {
                                        // 開啟硬體加速
                                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                    }
                                    
                                    override fun onDraw(canvas: Canvas) {
                                        super.onDraw(canvas)
                                        // 使用過濾後的參考點列表
                                        (touchImageViewRef.value as? MyCustomImageView)?.drawReferencePointsOnCanvas(
                                            canvas, 
                                            filteredReferencePoints  // 使用過濾後的參考點列表
                                        )
                                    }
                                }
                                
                                // 使用 OnTouchImageViewListener 更有效地處理縮放和平移事件
                                setOnTouchImageViewListener(object : OnTouchImageViewListener {
                                    private var lastCallTime = 0L
                                    
                                    override fun onMove() {
                                        // 限制過於頻繁的刷新
                                        val currentTime = System.currentTimeMillis()
                                        if (currentTime - lastCallTime > 16) { // 約60fps
                                            overlayView.postInvalidateOnAnimation() // 使用動畫框架的重繪方法
                                            lastCallTime = currentTime
                                        }
                                    }
                                })
                                
                                // 將覆蓋 View 加入到 TouchImageView 的父容器
                                (parent as? android.view.ViewGroup)?.addView(overlayView, 
                                    android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )
                            }
                        }
                    },
                    update = { view ->
                        // 確保圖片資源與當前選擇匹配
                        if ((view as? MyCustomImageView)?.drawable?.constantState?.newDrawable()?.constantState !=
                            context.getDrawable(currentImageId)?.constantState) {
                            view.setImageResource(currentImageId)
                        }
                        // 傳入最新的參考點並重繪
                        (view as? MyCustomImageView)?.let { mv ->
                            mv.overlayPoints = filteredReferencePoints
                            // 設置當前圖片 ID 和地圖資訊
                            mv.currentImageId = currentImageId
                            mv.currentMapImage = currentMapImage
                            mv.invalidate()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 編輯模式指示器
                if (isEditMode) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "點擊地圖直接新增參考點",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // 參考點列表部分 - 可收起，可獨立滑動
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .weight(1f), // 使用weight佔據剩餘空間
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 列表標題與展開/收起按鈕
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isListExpanded = !isListExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "參考點列表 (${filteredReferencePoints.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // 展開/收起箭頭圖示
                        Icon(
                            imageVector = if (isListExpanded) 
                                Icons.Default.KeyboardArrowUp 
                            else 
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isListExpanded) "收起" else "展開",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    HorizontalDivider()
                    
                    // 使用AnimatedVisibility控制內容顯示/隱藏
                    AnimatedVisibility(
                        visible = isListExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        // 獨立滾動的列表區域
                        if (filteredReferencePoints.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "尚無參考點，請點擊圖片或使用新增按鈕來添加",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Changed from Color.Gray
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // 使用LazyColumn而不是forEach，提供獨立滾動能力
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 400.dp) // 限制最大高度
                            ) {
                                items(filteredReferencePoints) { point ->
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(getPointColor(point)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (point.id.isNotEmpty()) point.id.takeLast(1) else "?",
                                                    color = Color.White
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Column {
                                                Text(
                                                    text = point.name,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Row {
                                                    Text(
                                                        text = "X: ${String.format(Locale.US, "%.2f", point.x)}%, Y: ${String.format(Locale.US, "%.2f", point.y)}%",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "• ${point.scanCount} 次掃描",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.weight(1f))
                                            
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        database.deleteReferencePoint(point.id)
                                                        allReferencePoints = database.referencePoints
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(android.R.drawable.ic_menu_delete),
                                                    contentDescription = "刪除",
                                                    tint = MaterialTheme.colorScheme.error // Changed from Color.Red
                                                )
                                            }
                                        }
                                    }
                                }
                                // 底部間隔
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 新增參考點對話框
        if (showAddDialog) {
            // 獲取當前地圖名稱
            val currentMapName = remember(currentImageId) {
                val mapImage = ReferencePointDatabase.availableMapImages.find { it.id == currentImageId }
                mapImage?.name?.let {
                    // 從資源名稱中提取實際的圖片名稱，如 se1, sea4 等
                    val resourceName = context.resources.getResourceEntryName(currentImageId)
                    resourceName
                } ?: "unknown"
            }
            
            // 判斷是否需要選擇區域
            val needAreaSelection = remember(currentMapName) {
                // se1, se2, se3 需要選擇區域，其他已有區域標識
                currentMapName == "se1" || currentMapName == "se2" || currentMapName == "se3"
            }
            
            // 修正 AlertDialog，確保使用 Material3 的 API 格式
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    tapPosition = null
                    xInput = ""
                    yInput = ""
                    nameInput = ""
                    selectedArea = "a"
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                title = { 
                    Text(
                        text = if (isEditMode) "快速新增參考點" else "新增參考點",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column {
                        if (tapPosition != null) {
                            Text(
                                text = "已選擇位置: X=${String.format(Locale.US, "%.2f", tapPosition!!.first)}%, Y=${String.format(Locale.US, "%.2f", tapPosition!!.second)}%",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        if (tapPosition == null) {
                            OutlinedTextField(
                                value = xInput,
                                onValueChange = { xInput = it },
                                label = { Text("X 座標 (0.0 - 100.0)%") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = yInput,
                                onValueChange = { yInput = it },
                                label = { Text("Y 座標 (0.0 - 100.0)%") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // 區域選擇（只有 se1, se2, se3 地圖需要）
                        if (needAreaSelection) {
                            Text(
                                text = "請選擇區域：",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("a", "b", "c").forEach { area ->
                                    Button(
                                        onClick = { selectedArea = area },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedArea == area) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = area.uppercase(),
                                            color = if (selectedArea == area)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // 掃描次數選擇
                        Text(
                            text = "WiFi 掃描次數：",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(3, 4, 5).forEach { count ->
                                Button(
                                    onClick = { scanCount = count },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (scanCount == count) 
                                            MaterialTheme.colorScheme.secondary 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "$count 次",
                                        color = if (scanCount == count)
                                            MaterialTheme.colorScheme.onSecondary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 生成默認名稱
                        val defaultName = if (needAreaSelection) {
                            "${currentMapName.take(2)}${selectedArea}${currentMapName.drop(2)}" 
                        } else {
                            currentMapName
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val x = if (tapPosition != null) 
                                tapPosition!!.first.toDouble()
                            else 
                                xInput.toDoubleOrNull() ?: 0.0
                                
                            val y = if (tapPosition != null) 
                                tapPosition!!.second.toDouble()
                            else 
                                yInput.toDoubleOrNull() ?: 0.0
                        
                            // 生成參考點名稱，如果用戶沒有輸入則使用默認名稱
                            val defaultName = if (needAreaSelection) {
                                "${currentMapName.take(2)}${selectedArea}${currentMapName.drop(2)}" 
                            } else {
                                currentMapName
                            }
                            val name = nameInput.ifEmpty { defaultName }
                        
                            // 範圍檢查
                            if (x in 0.0..100.0 && y in 0.0..100.0) {
                                // 創建新的參考點，包含當前圖片ID和掃描次數
                                val newPoint = ReferencePoint.createSimplePoint(
                                    name = name,
                                    x = x,
                                    y = y,
                                    imageId = currentImageId,
                                    scanCount = 0 // 先設為0，掃描後會更新
                                )
                                
                                scope.launch {
                                    try {
                                        // 先添加參考點
                                        database.addReferencePoint(newPoint)
                                        allReferencePoints = database.referencePoints
                                        showAddDialog = false
                                        
                                        // 顯示掃描進度的Snackbar
                                        isScanning = true
                                        scanTotal = scanCount
                                        scanProgress = 0
                                        
                                        // 準備收集WiFi數據
                                        val batchId = java.util.UUID.randomUUID().toString()
                                        val wifiReadings = mutableListOf<WifiReading>()
                                        
                                        // 循環掃描Wi-Fi
                                        repeat(scanCount) { index ->
                                            scanProgress = index + 1
                                            
                                            // 通知用戶正在掃描
                                            snackbarHostState.showSnackbar(
                                                message = "正在收集Wi-Fi數據 (${scanProgress}/${scanTotal})...",
                                                duration = SnackbarDuration.Short
                                            )
                                            
                                            // 確保有位置權限
                                            val locationPermissionGranted = ContextCompat.checkSelfPermission(
                                                context, 
                                                android.Manifest.permission.ACCESS_FINE_LOCATION
                                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                            
                                            if (locationPermissionGranted) {
                                                try {
                                                    // 開始掃描
                                                    @Suppress("DEPRECATION")
                                                    wifiManager.startScan()
                                                    delay(300) // 等待掃描結果
                                                    
                                                    // 獲取掃描結果
                                                    @Suppress("DEPRECATION")
                                                    val scanResults = wifiManager.scanResults
                                                    
                                                    // 轉換為WifiReading並添加到列表
                                                    val scanTime = System.currentTimeMillis()
                                                    val currentScanBatchId = "$batchId-scan-${index + 1}"
                                                    
                                                    scanResults.forEach { scan ->
                                                        wifiReadings.add(
                                                            WifiReading(
                                                                bssid = scan.BSSID,
                                                                ssid = if (scan.SSID.isNullOrEmpty()) "未知網路" else scan.SSID,
                                                                level = scan.level,
                                                                frequency = scan.frequency,
                                                                batchId = currentScanBatchId,
                                                                scanTime = scanTime
                                                            )
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("MapScreen", "Wi-Fi掃描錯誤", e)
                                                    Toast.makeText(
                                                        context,
                                                        "Wi-Fi掃描錯誤: ${e.localizedMessage}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "需要位置權限才能掃描Wi-Fi",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            
                                            delay(300)  // 掃描間隔
                                        }
                                        
                                        // 更新參考點，添加WiFi讀數
                                        if (wifiReadings.isNotEmpty()) {
                                            val updatedPoint = newPoint.copy(
                                                wifiReadings = wifiReadings,
                                                scanCount = scanCount
                                            )
                                            
                                            database.addReferencePoint(updatedPoint)
                                            allReferencePoints = database.referencePoints
                                            
                                            snackbarHostState.showSnackbar(
                                                "已新增參考點: $name 並收集了 ${wifiReadings.size} 個Wi-Fi訊號",
                                                duration = SnackbarDuration.Long
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                "已新增參考點: $name，但未收集到Wi-Fi訊號",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("新增參考點失敗: ${e.localizedMessage}")
                                    } finally {
                                        isScanning = false
                                        tapPosition = null
                                        xInput = ""
                                        yInput = ""
                                        nameInput = ""
                                        selectedArea = "a"
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("座標需在 0.0 到 100.0 之間")
                            }
                        }
                    }
                ) {
                    Text(if (isEditMode) "快速新增" else "新增")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        tapPosition = null
                        xInput = ""
                        yInput = ""
                        nameInput = ""
                        selectedArea = "a"
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 顯示掃描進度指示器
    if (isScanning) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "正在掃描Wi-Fi...",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "進度: $scanProgress/$scanTotal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { scanProgress.toFloat() / scanTotal },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    // 載入參考點資料 - 修改為使用實際的篩選邏輯
    LaunchedEffect(Unit) {
        try {
            // 載入所有參考點
            allReferencePoints = database.referencePoints
        } catch (e: Exception) {
            Log.d("MapScreen", "參考點錯誤")
        }
    }
    
    // 監聽圖片ID變化，確保重新渲染
    LaunchedEffect(currentImageId) {
        // 觸發重新渲染
        customImageViewRef.value?.invalidate()
    }
}

}

// 為參考點生成顏色的函數
fun getPointColor(point: ReferencePoint): Color {
    // 使用 ID 的 hashCode 生成唯一但一致的顏色
    val hash = point.id.hashCode()
    return Color(
        red = ((hash and 0xFF0000) shr 16) / 255f,
        green = ((hash and 0x00FF00) shr 8) / 255f,
        blue = (hash and 0x0000FF) / 255f,
        alpha = 1f
    )
}
