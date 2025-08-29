# COOL Tracker

![GitHub License](https://img.shields.io/github/license/NatsuCamellia/cool-tracker)
[![GitHub Release](https://img.shields.io/github/v/release/NatsuCamellia/cool-tracker)](https://github.com/NatsuCamellia/cool-tracker/releases)
![Static Badge](https://img.shields.io/badge/Platform-Android_10+-green)

COOL Tracker 是一款專為 [NTU COOL](https://cool.ntu.edu.tw/) 設計的 Android 應用程式，讓使用者能輕鬆追蹤所有作業進度。
<p align="center" width="100%">
  <img width="30%" src="https://github.com/user-attachments/assets/f99254db-5a46-4e36-ba08-d2a4d2180b12" />
  <img width="30%" src="https://github.com/user-attachments/assets/4d6bc867-fb63-4a3f-8629-ab6bef7bf915" />
  <img width="30%" src="https://github.com/user-attachments/assets/aa9f26a6-7ea0-40d2-a8c7-566bbc366557" />
  <img width="30%" src="https://github.com/user-attachments/assets/70486d9b-f7e6-4e3f-9b7d-78083071dbb1" />
</p>

## 下載

[![GitHub Release](https://img.shields.io/github/v/release/NatsuCamellia/cool-tracker)](https://github.com/NatsuCamellia/cool-tracker/releases)
![Static Badge](https://img.shields.io/badge/Platform-Android_10+-green)

請至 [Releases](https://github.com/NatsuCamellia/cool-tracker/releases) 下載最新版本的 `app-release.apk`。

## 簡介
### 設計與技術

它借鏡 Google 的應用程式設計原則，並採用最新的 [Material 3 Expressive](https://m3.material.io) 設計語言，打造出現代且活潑的使用者介面。此外，COOL Tracker 遵循 Android 原生設計[^1]規範，並使用 [Jetpack Compose](https://developer.android.com/compose) 原生 UI 框架，確保使用者能獲得道地的 Android 體驗。

### 核心功能

- [x] 作業追蹤： 輕鬆檢視進行中課程的所有作業，並掌握繳交狀態。
- [ ] 作業提醒： 在作業截止前發出提醒，幫助使用者及時完成繳交。
- [x] 桌面小工具： 提供桌面小工具，讓使用者無需開啟應用程式即可快速查看作業。

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
3. 本專案為獨立開發，並未與 NTU COOL 官方有任何關聯。此專案的所有內容與功能皆為開發者的貢獻，不代表 NTU COOL 官方立場。
4. 本應用程式僅供學術研究與個人學習使用，不得用於任何商業或非法用途。使用者若違反此規定，所產生之一切後果與法律責任，概由使用者自行承擔。

## 貢獻

歡迎並鼓勵大家為這個專案貢獻心力，你可以透過 [Issues](https://github.com/NatsuCamellia/cool-tracker/issues) 回報錯誤或提供功能建議，或是你也可以參考以下說明貢獻程式碼。

### 提交 Issues

- 標題：請用簡潔明瞭的方式概括你的問題或建議
- 描述：可以的話請提供以下資訊
  - 預期程式行為
  - 實際程式行為
  - 重現錯誤步驟
  - 補充資訊：裝置與系統版本以及應用程式版本等。

### 貢獻程式碼

為了確保專案的一致性與品質，所有程式碼貢獻都應遵守以下規範：

- 程式碼風格：請盡量使用 Android Developers 官方推薦的寫法，包括但不限於 Kotlin Coroutines、Android Jetpack 函式庫、以及 MVVM 等架構模式。
- 使用者介面 (UI)：UI 貢獻必須嚴格遵守 Android 原生風格，使用 Jetpack Compose 並與 Material 3 Expressive 設計指南保持一致。在設計和實作新的功能時，應優先使用 Material 3 Expressive 提供的元件，並確保其視覺效果與應用程式整體風格和諧。

閱讀並同意以上規範後，便可開始貢獻程式碼：

- 分叉專案：在你的 GitHub 帳號下分叉（fork）此專案。
- 提交變更：撰寫清晰且有意義的提交訊息。
- 合併請求：完成變更後，向此專案發起一個合併請求 (Pull Request)。請在請求中詳細說明你所做的變更、目的以及任何相關的錯誤或問題。

[^1]: 係指 AOSP 或 Google Pixel 系統設計。
