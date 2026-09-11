# TotemCore

TotemCore 是 Totem 系列功能模組的共用 API 基礎。它提供跨模組契約、
生命週期介面、API 版本協商與共用好友關係；不註冊物品、方塊或功能模組專屬
SavedData。Core 也提供原版
遊戲規則畫面共用的 Totem 分類；客戶端會替正式 Totem 手冊提供共用雙頁
版面，也提供不保存功能狀態的世界框線 API，但不取代一般原版書本。

目前版本為 **0.7.20**，API root 為 `dev.totem.core.api.v1`。

## 誰需要安裝

- 一般玩家不會單獨從 TotemCore 得到玩法；它必須搭配至少一個 Totem
  功能模組。
- 使用獨立模組時，把 `totem-core-0.7.20.jar` 與相容的功能模組一起放進
  Client／Server 的 `mods/`。
- 0.7.x 功能模組改用驗證過的 Core minor 範圍；目前目標為
  `totem-core >=0.7.0 <0.8.0`，不再因 Core patch release 強迫全系列重發。

## 相容需求

| 項目 | 需求 |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Java | 25+ |
| Fabric API | 必須安裝 |

## 提供的 API

| API | 用途 |
| --- | --- |
| `TotemEvent` | 不可變、帶契約版本的跨功能事件 marker |
| `TotemEventBus` | 程序內、型別安全的跨功能 publish／subscribe seam |
| `DeathBackpackCreatedEvent` | Remnant 成功建立死亡背包後發布 |
| `DeathBackpackRecoveredEvent` | Remnant 或 Nexus 完成死亡背包回收後發布 |
| `SpaceUnitPublicUpdateEvent` | Nexus 公開 Space Unit 狀態更新 |
| `AdminAuditEvent` | 功能模組完成管理操作後的安全稽核摘要 |
| `ApiVersion` | 驗證同 major、足夠 minor 的 API 相容性 |
| `TotemFriendshipApi` | 全系列唯一好友／邀請資料來源；查詢、邀請、接受與解除關係 |
| `DeathBackpackNodeLifecycle` | Remnant 與 Nexus 間的選配死亡節點生命週期 |
| `DeathRetainedItemPolicy` | 功能模組授權一件物品由死亡模組安全保留 |
| `LegacyItemMigrationRegistry` | DeadRecall 註冊舊 ID 對應，功能模組以 canonical Item 接受並轉換舊堆疊 |
| `LegacyNbtMigrationRegistry` | 功能模組以 allow-list 註冊 raw-NBT 舊 ID 改寫；Core 在 registry codec 前執行，下一次儲存只會寫 canonical ID |
| `gamerule.TotemGameRuleCategories` | 提供穩定的 `totem:rules` 原版遊戲規則分類，讓各功能模組自行登記規則 |
| `manual.*` | 功能模組登記本地化章節，組裝、刷新、拆分及重新整合原版 Totem 手冊 |
| `client.manual.*` | 共用雙頁手冊版面，以及功能模組可選的頁面圖示覆蓋註冊表 |
| `client.world.TotemWorldOutlines` | 無狀態方塊／長方體框線、任意兩點實線與預先計算線框的提交，支援明確的深度遮擋或穿牆模式 |
| `client.world.VoxelUnionOutline` | 將方塊集合轉為可快取的最外圍線框；合併連續線段並移除共享邊、平面格線與封閉空腔 |

## 共用玩家目錄

Core 0.7.20 的 `player.TotemPlayerDirectoryApi` 統一保存曾進入此世界的玩家 UUID 與最後使用名稱，提供離線查找、名稱／UUID 搜尋、分頁及即時在線狀態。Nexus 的管理權限選擇使用此 API；其他模組也應使用同一資料來源。

資料以 `totem:known_players` 隨世界保存。既有世界會匯入 `playerdata/*.dat` 對應的 UUID，名稱取自本機快取；無名稱時顯示 UUID，等玩家登入後更新。查詢不連線 Mojang，也不憑名稱生成離線 UUID。同名結果須以 UUID 選擇。

API 必須在伺服器執行緒使用。功能模組仍負責查詢者授權、請求頻率及自己的權限資料。

## 共用好友系統

Core 0.7.0 起由 `TotemFriendshipApi` 擁有整個 Totem 系列的好友關係與待處理
邀請。Nexus 只負責好友 UI、Space Unit 與傳送用途；Locksmith 的 Friends
存取模式也直接查詢 Core，不再要求 Nexus 必須安裝。其他功能模組若需要
好友權限，應使用同一個 API，不要另存一份關係資料。

新世界會寫入 `totem:space_friends`。為保留舊世界，首次讀取時 Core 只會讀取
歷史 `deadrecall:space_friends`、複製到 canonical 儲存識別並寫入新的資料；原
legacy 檔案不會被改寫。因此從舊 Nexus 升級時，已存在的 friendships 與 pending
invites 會由 Core 直接接手，不需要玩家重新加好友。

## 選配死亡背包整合

`DeathBackpackNodeLifecycle` 的責任分工：

1. Remnant 建立死亡背包前呼叫 `create` 建立選配節點。
2. 背包 ItemEntity 生成後以 `bind` 保存反向 UUID。
3. transaction 失敗時呼叫 `rollback`。
4. 背包清空時以 `recover` 停用節點。

```java
DeathBackpackNodeLifecycle.current().ifPresent(lifecycle -> {
    UUID nodeId = lifecycle.create(player, level, position);
    lifecycle.bind(level, nodeId, backpackEntity.getUUID());
});
```

功能模組可以在不存在 adapter 時正常運作；不要把另一個功能模組改成
必要依賴。

事件發布者只依賴 Core 契約，不直接呼叫 Discord 或其他消費者。
`TotemEventBus` 會隔離單一 subscriber 的失敗；沒有 subscriber 時發布
是安全的 no-op。Discord Bridge 可獨立訂閱上述事件，因此 standalone
組合不需要額外的整合接線。

## 0.7.20 發布重點

- 新增可快取的 `VoxelUnionOutline`，把方塊集合轉成精確的最外圍線框。
- 移除相鄰方塊的共享邊、同平面格線與封閉空腔線，並合併連續線段。
- 保留不規則與分離元件的實際形狀，且以固定上限拒絕病態的大範圍推導。

## 0.7.0 發布重點

- 將好友與 pending invitation 的唯一資料來源從 TotemNexus 搬到 TotemCore。
- 新增 `dev.totem.core.api.v1.social.TotemFriendshipApi`。
- 新世界使用 `totem:space_friends`；舊世界的 `deadrecall:space_friends` 會在
  首次讀取時單向複製到 canonical 資料。
- 正式區分 feature-specific SavedData 與 Core-owned cross-module identity / relationship primitives。
- 下游功能模組改用 Core minor 相容範圍，CI 仍固定在已驗證的 Core commit。

## API 版本政策

- Patch 版本保留公開 signature 與語意。
- Minor 版本只加入向後相容 API。
- 不相容變更必須升 major。
- Deprecated API 至少保留兩個 bundle release 與一個已發佈 Core minor
  release，並提供替代方式與相容測試。

完整契約見 [TotemCore API v1](docs/api-v1.md)。

## 開發與建置

需要 Java 25：

```bash
./gradlew build
```

輸出位於 `build/libs/`。功能 repository 的 `fabric.mod.json` 應使用經驗證的
Core minor 相容範圍；CI 則固定在明確 Core commit，避免未驗證的 Core 變更
偷偷進入 release build。功能模組仍應避免直接依賴其他功能模組。
