# TotemDiscordBridge

Keep a Discord community connected to its Minecraft server. TotemDiscordBridge sends selected chat, events, administrative notices, and server status through a separately deployed HTTPS Worker to your Discord destination.

The bridge is **disabled by default**. You provide the compatible Worker and Discord setup; this mod does not include a hosted service.

## Conversations and activity

Relay Minecraft chat, joins, leaves, deaths, advancements, and supported world events. Optional Totem event integrations can report backpack recovery, public Nexus updates, and supported locked-container break audits.

Multiple configured channels let an operator organize destinations. With a suitable Worker and bot, live presence can show the online player count or an offline status without posting a new channel message for every change.

## Link player accounts

A compatible Discord-side binding flow supplies a one-time verification code. The player then uses `/discordlink verify <code>` in Minecraft and can check or remove their link with /discordlink status and /discordlink unlink.

Ordinary players do not need the client mod for these commands. Account requests use the logged-in player's identity rather than an arbitrary UUID supplied by another player.

## Administration and translated messages

Configure the bridge through config/discord-bridge.json and server commands. Administrators who also install the client mod can use its settings interface. Server-side permission checks control access.

Event localization prefers Traditional Chinese with English fallback and other available resources. The server can download and cache Mojang's official Traditional Chinese language asset for translated Minecraft messages.

## Data sent and message retention

Enabled features send relevant player names and UUIDs, chat and event content, server status, and player counts to the configured Worker and Discord destination. Account linking additionally sends verification requests. These records are not anonymous.

Access and retention depend on your relay and channel settings. Certain join/leave, backpack, and status notifications request deletion after ten minutes; ordinary chat, death, advancement, and audit messages have no automatic deletion request. The Worker must implement the requested deletion behavior.

Inform players what is forwarded and who can read it. Protect the Worker API key and Discord credentials, and avoid placing them in public logs, screenshots, or issue reports.

## Getting started and requirements

1. Install the mod, Fabric API, and [TotemLibrary / TotemCore](https://modrinth.com/mod/totemlibrary) on the server.
2. Deploy a compatible HTTPS Worker and configure its Discord destination and shared key.
3. Fill in the Worker URL and matching key, enable the bridge, and run /discordbridge reload or restart.
4. Add the optional client installation only for administrators who want the settings screen.

Current release 0.1.13 targets Minecraft 26.2, Fabric Loader 0.19.3+, Java 25+, and TotemCore >=0.7.0 and <0.8.0. Other Totem gameplay modules are optional.

[Relay endpoints and configuration](https://github.com/Yunitrish006006/TotemDiscordBridge) · [Report an issue](https://github.com/Yunitrish006006/TotemDiscordBridge/issues)

---

## 繁體中文

把 Minecraft 伺服器活動連到 Discord，透過另外部署的 HTTPS Worker 轉送聊天、玩家事件、管理通知與伺服器狀態。功能**預設關閉**，需要自行準備相容的 Worker 與 Discord 設定，不附代管服務。

### 聊天、事件與即時狀態

支援聊天、加入離開、死亡、進度及其他已支援事件；搭配 Totem 模組可轉送死亡背包回收、公開 Nexus 更新與鎖容器破壞稽核。

可設定多個頻道，並由相容 Worker／Bot 顯示在線人數或離線狀態，不必每次都新增頻道訊息。

### 玩家帳號綁定

由 Discord 端產生一次性綁定碼，再由本人登入 Minecraft 使用 `/discordlink verify <code>` 驗證，並可查詢或解除綁定。普通玩家不必安裝客戶端模組，請求採用目前登入玩家的身分。

### 管理與翻譯

可用設定檔與伺服器指令管理；需要遊戲內設定畫面的管理員才須另外安裝客戶端模組。事件訊息優先使用繁體中文並保留英文回退，伺服器可能向 Mojang 下載、驗證與快取官方繁中語言資產。

### 外部資料與保留方式

啟用後，相關玩家名稱、UUID、聊天、事件、伺服器狀態與人數會送至設定的 Worker 及 Discord；綁定功能另傳送驗證請求，資料不是匿名的。

誰能查看、保留多久取決於服務與頻道設定。部分加入離開、背包與狀態通知要求十分鐘後刪除，普通聊天、死亡、進度與管理稽核不附自動刪除要求，實際刪除須由 Worker 執行。服主應告知玩家轉送範圍，並妥善保管所有金鑰。

### 快速開始與需求

伺服器安裝模組、Fabric API 與 TotemLibrary，部署相容 Worker 並設定 Discord 目的地，填好 URL 與共用金鑰後啟用，再執行 /discordbridge reload 或重啟。一般玩家不需 Bridge 客戶端，管理 GUI 使用者才需要。

目前 0.1.13 需要 Minecraft 26.2、Fabric Loader 0.19.3+、Java 25+，以及 TotemLibrary／TotemCore >=0.7.0、<0.8.0。其他玩法模組為選配。
