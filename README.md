# TTU NEXTDOOR 📡

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-0095D5?logo=kotlin)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpack-compose)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-brightgreen)

## 📋 目錄

- [專案概述](#-專案概述)
- [功能亮點](#-功能亮點)
- [系統需求](#-系統需求)
- [安裝指南](#-安裝指南)
- [使用說明](#-使用說明)
- [技術架構](#-技術架構)
- [定位演算法](#-定位演算法)
- [開發資訊](#-開發資訊)
- [注意事項](#-注意事項)
- [授權條款](#-授權條款)

## 📱 專案概述

TTU NEXTDOOR 是一款專為室內環境設計的高精度定位應用。在 GPS 訊號受限的室內空間中，本系統透過分析周圍 Wi-Fi 網路的訊號特徵建立「電子指紋地圖」，實現精確的室內位置識別。

適用場景：
- 大型商場及展覽館導航
- 醫院、學校等複雜建築物內部定位
- 倉儲物流中心的資產追蹤
- 智慧建築內位置感知服務

## ✨ 功能亮點

### 參考點管理
- **建立參考點**：在室內地圖上標記位置參考點，記錄對應的 Wi-Fi 訊號特徵
- **視覺化參考點**：參考點在地圖上以彩色標記顯示，直觀易讀
- **參考點詳情**：查看每個參考點的座標和收集的 Wi-Fi 訊號資料
- **批次收集**：支援多次掃描收集參考點的 Wi-Fi 訊號，提高精確度
- **追加模式**：可選擇保留舊的 Wi-Fi 資料並追加新的測量值
- **跨裝置共享**：支援匯出參考點資料為 JSON 格式，並在其他裝置上匯入使用

### 地圖功能
- **多地圖支援**：支援多層樓地圖切換，適用於複雜建築物
- **互動式地圖**：支援縮放、平移等手勢操作
- **直接標記**：透過點擊地圖直接建立新參考點
- **座標輸入**：可手動輸入精確座標位置
- **即時顯示**：參考點即時顯示在地圖上，帶有名稱標籤
- **檢視/編輯模式**：提供不同模式以便於查看或編輯參考點

### Wi-Fi 掃描與分析
- **自動掃描**：定期掃描環境中的 Wi-Fi 訊號
- **手動更新**：支援使用者手動觸發掃描更新
- **信號視覺化**：以色彩區分不同強度的 Wi-Fi 訊號
- **詳細資訊**：顯示 SSID、BSSID、訊號強度和頻率
- **數據統計**：提供平均值、最大值、最小值等多種數據分析模式

### 位置估算
- **即時定位**：根據當前 Wi-Fi 環境估算當前位置
- **準確度指示**：顯示位置估算的可信度級別，分為高、中、低三級
- **視覺反饋**：在地圖上清晰標示估算位置，使用不同顏色表示準確度
- **地圖自動選擇**：根據 Wi-Fi 環境自動判斷所在的樓層/地圖

### 資料管理
- **本地儲存**：使用 Room 資料庫安全儲存參考點和 Wi-Fi 資料
- **資料匯出**：將參考點資料匯出為 JSON 格式，方便備份和分析
- **資料匯入**：支援從 JSON 檔案匯入參考點資料，實現跨裝置共享
- **自訂檔案名稱**：使用者可以自訂匯出檔案的名稱
- **靈活儲存位置**：使用系統檔案選擇器選擇匯出檔案的儲存位置

## 🔧 系統需求

- **作業系統**：Android 12.0 (API 31) 或更高版本
- **硬體要求**：支援 Wi-Fi 的 Android 裝置，建議 4GB+ RAM
- **儲存空間**：應用本身佔用約 50MB，資料儲存視參考點數量而定
- **權限需求**：
  - 精確位置權限 (用於 Wi-Fi 掃描)
  - 儲存權限 (用於資料匯出/匯入)
  - 背景掃描權限 (用於持續定位)

## 📥 安裝指南

### 方法一：從 Release 安裝
1. 前往本專案的 [Releases](https://github.com/yourusername/TTU-NEXTDOOR/releases) 頁面
2. 下載最新版本的 APK 檔案
3. 在 Android 裝置上開啟檔案並安裝
4. 允許來自未知來源的應用程式安裝（如果需要）

### 方法二：從原始碼構建
1. 複製專案存儲庫：
   ```
   git clone https://github.com/yourusername/TTU-NEXTDOOR.git
   ```
2. 使用 Android Studio 開啟專案
3. 執行 Gradle 同步
4. 使用 Run 按鈕安裝到已連接的裝置或模擬器上

## 📋 使用說明

### 初次使用
1. 啟動應用程式時，系統會請求必要的位置權限
2. 授予權限後，您將看到主畫面，顯示目前的 Wi-Fi 環境

### 建立參考點
#### 方法 1：使用地圖
1. 切換到「地圖測試」分頁
2. 選擇適合的樓層/區域地圖
3. 開啟「編輯模式」開關
4. 在互動式地圖上，點擊您想要建立參考點的位置
5. 為參考點命名（或使用預設名稱）
6. 點擊「快速新增」完成建立

#### 方法 2：使用室內定位頁面
1. 在「室內定位」分頁點擊右下角的「+」按鈕
2. 輸入參考點名稱和 X、Y 座標值（百分比座標）
3. 設定掃描次數
4. 點擊「儲存參考點」完成建立

### 收集 Wi-Fi 資料
1. 在「室內定位」頁面點擊任一參考點查看詳情
2. 設定掃描次數和是否啟用追加模式
3. 點擊「開始收集 Wi-Fi 訊號」按鈕
4. 靜待收集完成，系統會顯示進度

### 使用互動地圖
1. 在地圖視圖中，您可以：
   - **縮放**：使用捏合手勢放大或縮小地圖
   - **平移**：拖動地圖移動視角
   - **切換地圖**：點擊頂部的地圖選項卡切換不同樓層/區域
   - **查看位置**：目前估算位置會以大圓圈顯示，顏色表示準確度
2. 在「編輯模式」下可直接點擊地圖新增參考點
3. 在「檢視模式」下可查看參考點詳情

### 匯出/匯入資料
- **匯出**：點擊「室內定位」頁面右上角的儲存圖示，設定檔案名稱並選擇儲存位置
- **匯入**：點擊右上角的下載圖示，選擇要匯入的 JSON 檔案

## 🛠️ 技術架構

### 開發技術
- **程式語言**：Kotlin 1.8.0+
- **UI 框架**：Jetpack Compose 與 Material Design 3
- **資料儲存**：Room 資料庫 2.5.0+
- **序列化**：Kotlinx Serialization 1.5.0+
- **非同步處理**：Kotlin Coroutines 與 Flow
- **地圖互動**：自訂 TouchImageView 實現
- **依賴注入**：Hilt/Dagger
- **單元測試**：JUnit, Mockito

### 主要程式結構
```
app/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/wifiindoorsystem/
│   │   │   ├── MainActivity.kt           # 應用入口點
│   │   │   ├── ui/                       # UI 相關程式碼
│   │   │   │   ├── theme/                # 主題設定
│   │   │   │   ├── screens/              # 各畫面實現
│   │   │   │   └── components/           # 共用元件
│   │   │   ├── data/                     # 資料層
│   │   │   │   ├── model/                # 資料模型
│   │   │   │   ├── repository/           # 資料存取
│   │   │   │   └── local/                # 本地數據源
│   │   │   ├── domain/                   # 業務邏輯層
│   │   │   │   ├── usecase/              # 使用案例
│   │   │   │   └── algorithm/            # 定位演算法
│   │   │   └── utils/                    # 通用工具類
│   │   └── res/                          # 資源文件
│   └── test/                             # 測試程式碼
└── build.gradle                          # 構建配置
```

### 核心數據模型
- **ReferencePoint**：參考點資料結構，包含位置與關聯的 Wi-Fi 讀數
- **WifiReading**：Wi-Fi 訊號讀數，含 BSSID、SSID、訊號強度
- **CurrentPosition**：當前位置資料結構，包含座標和準確度指標

## 📈 定位演算法

本系統採用加權 k-NN (k-最近鄰) 演算法進行位置估算，具體實現如下：

1. **信號收集**：掃描當前環境中可見的所有 Wi-Fi AP 的訊號強度 (RSSI)
2. **相似度計算**：
   ```
   distance = √∑(current_RSSI_i - reference_RSSI_i)²
   ```
   其中，僅計算兩者共同可見的 AP，若某 AP 僅出現在一方，則設定預設差值
3. **鄰近點選擇**：選擇距離最小的 K 個參考點 (預設 K=5)
4. **權重分配**：使用反比權重計算：
   ```
   weight_i = (1/distance_i) / ∑(1/distance_j)
   ```
5. **位置估算**：
   ```
   estimated_x = ∑(weight_i * reference_x_i)
   estimated_y = ∑(weight_i * reference_y_i)
   ```
6. **準確度評估**：根據權重分布和最小距離值計算可信度指標

### 演算法優化
- **異常值過濾**：移除訊號強度異常的讀數
- **頻段權重**：不同頻段 (2.4GHz vs 5GHz) 的 AP 賦予不同權重
- **時間衰減**：較舊的讀數影響力逐漸減弱
- **移動平滑**：使用卡爾曼濾波器平滑位置變化

## 💻 開發資訊

### 開發環境設置
1. Android Studio Flamingo 2022.2.1 或更新版本
2. Gradle 8.0+
3. JDK 17

### 建議的 IDE 外掛
- Kotlin Serialization IDE Support
- Compose Multiplatform
- Material Design 3 Theme Builder

### 貢獻指南
1. Fork 專案存儲庫
2. 創建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 開啟 Pull Request

## ⚠️ 注意事項

- 此應用程式需要位置權限才能掃描 Wi-Fi 網路，這是 Android 系統的限制
- Wi-Fi 掃描頻率受 Android 系統限制，在 Android 9+ 上每 30 秒最多掃描 4 次
- 定位精度受環境中 Wi-Fi AP 數量和分布影響
- 大型金屬物體、密集人群可能影響 Wi-Fi 訊號，導致定位誤差增加
- 參考點資料僅儲存在裝置上，建議定期匯出備份
- 耗電量優化：建議在不需要定位時關閉背景掃描

## 📜 授權條款

本專案採用 MIT 授權條款 - 詳情請參閱 [LICENSE](LICENSE) 文件。

---

## 📬 聯絡資訊

如有問題或建議，請透過以下方式聯絡：

- 專案維護者：[SP](mailto:wsp617617@gmail.com)
- 專案問題追蹤：[GitHub Issues](https://github.com/ScaratP/HelpMePlz/issues)

[![Star this repo](https://img.shields.io/github/stars/ScaratP/HelpMePlz.svg?style=social)](https://github.com/ScaratP/HelpMePlz)
