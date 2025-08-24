# COOL Tracker

COOL Tracker 是一款專為 [NTU COOL](https://cool.ntu.edu.tw/) 設計的 Android 應用程式，讓使用者能輕鬆追蹤所有作業進度。
<p align="center" width="100%">
  <img width="30%" height="2400" alt="courses" src="https://github.com/user-attachments/assets/446e96c2-0de3-41c3-aeef-429d6294cc51" />
  <img width="30%" height="2400" alt="course-detail" src="https://github.com/user-attachments/assets/340716cd-266d-4352-9299-910ca4791336" />
  <img width="30%" height="2400" alt="assignments" src="https://github.com/user-attachments/assets/3c598a3d-13a6-4928-8fbc-478ba66136e1" />
</p>

## 簡介
### 設計與技術

它借鏡 Google 的應用程式設計原則，並採用最新的 [Material 3 Expressive](https://m3.material.io) 設計語言，打造出現代且活潑的使用者介面。此外，COOL Tracker 遵循 Android 原生設計[^1]規範，並使用 [Jetpack Compose](https://developer.android.com/compose) 原生 UI 框架，確保使用者能獲得道地的 Android 體驗。

### 核心功能

- [x] 作業追蹤： 輕鬆檢視進行中課程的所有作業，並掌握繳交狀態。
- [ ] 作業提醒： 在作業截止前發出提醒，幫助使用者及時完成繳交。
- [ ] 桌面小工具： 提供桌面小工具，讓使用者無需開啟應用程式即可快速查看作業。

### 備註

為了最佳化使用者的網路流量與簡化軟體設計，COOL Tracker 目前只支援進行中的課程資料。

## 運作原理

COOL Tracker 讓使用者在應用程式中，透過內建的瀏覽器登入 NTU COOL。完成登入後，COOL Tracker 會取得使用者的登入憑證（Session Cookie），讓應用程式能夠代替使用者自動從 NTU COOL 伺服器取得最新的作業資料。

## 隱私權與免責聲明

### 隱私權聲明

1. 本應用程式要求使用者透過應用程式內建瀏覽器登入 NTU COOL，以取得用於存取資料的 Session Cookie。
2. 本應用程式會將此 Session Cookie 經加密後儲存於使用者裝置中，不會回傳給開發者或任何第三方。

> Session Cookie 包含高度個人隱私資訊，任何實體若取得特定使用者的 NTU COOL Session，皆可登入該使用者之帳號。使用者應謹慎保管，避免資料外洩。

### 免責聲明

1. 開發者不保證本應用程式所顯示資料之正確性，所有內容僅供參考。使用者應以 NTU COOL 官方平台所顯示之資料為準。
2. 開發者不保證本應用程式之安全性。使用者應自行承擔因網路攻擊或其他方式，導致 Session Cookie 外洩所產生之風險。
3. 本應用程式僅供學術研究與個人學習使用，不得用於任何商業或非法用途。使用者若違反此規定，所產生之一切後果與法律責任，概由使用者自行承擔。

[^1]: 係指 AOSP 或 Google Pixel 系統設計。
