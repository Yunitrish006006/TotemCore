# TotemLibrary

The shared foundation for the Totem mod family. Install it when a Totem gameplay module lists it as a dependency; it brings the modules together through one manual, shared friendships, and compatible integration services.

**TotemLibrary is the Modrinth project name. The installed mod and dependency ID remain TotemCore / totem-core.** This is a library, not a standalone backpack, teleportation, or automation mod.

## What it provides

- **One growing Totem Manual.** A starter guide is given on first join. Use the manual or a plain Book on a supported module's source block to record its chapter. Acquired chapters stay together, with a contents page and shared two-page layout.
- **Manual updates and recovery.** Recorded chapters can refresh when module content changes. Supported older module guides can be brought into the unified manual.
- **Shared friendships.** Totem modules use the same relationships and pending invitations. Nexus supplies friendship interactions, while Locksmith can consult those relationships without requiring Nexus as a dependency.
- **Optional cross-module connections.** Common events and lifecycle services let Remnant backpacks, Nexus recovery destinations, and DiscordBridge notifications cooperate when their owning modules are installed.
- **Consistent presentation.** Shared Totem game-rule categories, world outlines, and read-only Observer provider contracts help companion modules present their features consistently. The gameplay module decides what is shown and who may access it.
- **Compatibility and migration services.** Versioned APIs and supported legacy identifier conversions help modules evolve while retaining compatible saved content. These services do not make every historical mod version interchangeable.

## Installation

For the current 0.7.19 release: Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, and Fabric API are required. Install the compatible library wherever the dependent module requires it, normally on both client and server.

Check the Dependencies section of your selected gameplay release. Older releases can require an exact older Core version. Do not install two JARs providing the same mod ID.

## For modpack makers and developers

Choose the gameplay modules you want; installing the library does not install them automatically. The [API documentation](https://github.com/Yunitrish006006/TotemCore/blob/master/docs/api-v1.md) describes integration contracts and compatibility rules.

[Source and documentation](https://github.com/Yunitrish006006/TotemCore) · [Report an issue](https://github.com/Yunitrish006006/TotemCore/issues)

---

## 繁體中文

TotemLibrary 是 Totem 系列的共用基礎函式庫，負責讓各玩法模組共用手冊、好友資料與整合介面。Modrinth 頁面名稱是 TotemLibrary，遊戲內模組名稱及依賴識別仍為 TotemCore／totem-core。

### 特色功能

- **一本持續累積的 Totem 手冊：**首次加入取得入門指南；拿手冊或普通書對已安裝模組的來源方塊使用，即可把對應章節加入同一本書，透過目錄與雙頁版面閱讀。
- **章節更新與舊指南整合：**已記錄內容可隨模組更新刷新，支援的舊版指南可整合進共用手冊。
- **全系列共用好友關係：**好友與待接受邀請由 Core 統一保存。Nexus 提供操作介面，Locksmith 可直接利用好友權限，不必把 Nexus 列為必要模組。
- **選配模組互相合作：**提供死亡背包、死亡傳送點、公開事件及通知等共用介面，讓相關模組在同時安裝時合作。
- **一致的共用呈現：**提供 Totem 世界規則分類、世界框線與唯讀 Observer 介面契約；實際畫面與權限仍由功能模組負責。
- **版本相容與遷移：**支援明確的 API 版本及部分舊識別資料遷移，但不代表所有歷史版本可任意混用。

### 安裝與定位

目前 0.7.19 需要 Minecraft 26.2、Fabric Loader 0.19.3 以上、Java 25 以上及 Fabric API。依所選玩法模組的需求安裝，通常客戶端與伺服器都需要。

這不是獨立的背包、傳送或自動化模組，也不會自動安裝其他玩法。請依各版本 Dependencies 選擇相容的 Core；舊版可能要求指定版本，並避免重複安裝相同模組 ID。
