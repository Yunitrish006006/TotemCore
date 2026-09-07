# Totemvt — TotemVanillaTweaks

A collection of practical inventory tools, survival-rule changes, and an administrator Observer View. Use it to streamline storage and support server oversight, while understanding the world changes it enables.

## Inventory sorting

Middle-click the hovered side of an inventory or container screen to sort it. The key can be rebound in Controls.

Matching item variants are combined and ordered consistently. Sorting the player's main inventory leaves the hotbar, armor, offhand, and other equipment slots alone. Container operations still follow server-validated slot and menu rules.

## Small changes to survival

- **Simpler Lecterns:** craft one from four wooden slabs and a Book.
- **Bookshelf overhaul:** the ordinary Bookshelf crafting recipe is removed. Ordinary Bookshelf items in survival inventories convert into three Books each; creative players are exempt from that inventory conversion.
- **Useful generated shelves:** supported ordinary and empty Chiseled Bookshelves in generated structures become book-filled Chiseled Bookshelves.
- **Concrete in water:** dropped Concrete Powder items harden when they actually contact water, retaining stack quantity. Rain or merely being near water is not enough.
- **Automatic furnace experience:** Hoppers taking results from furnaces, smokers, or blast furnaces release the corresponding recipe experience nearby.

The bookshelf rules change existing survival behavior. Read them before adding the mod to an established world.

## Administrator Observer View

Authorized administrators in Spectator mode can use `/observeui <player>` to observe a player's perspective, HUD, inventory, and supported interface state. Use /observeui stop or Escape to stop.

This is **read-only**: observation does not grant control of the target's menus or inventory. Compatible clients share structured game state and render supported screens locally; the feature does not record or stream screenshots or video.

Supported vanilla screens use the corresponding Minecraft interfaces. Compatible Totem screens are provided by their owning modules, including backpacks, golem management, Nexus, locks, and the Woodcutter. Unsupported screens are identified rather than presented as fully supported.

Unsent chat and commands, private draft fields, and credentials are excluded. Both clients need compatible installations, and the server controls observation permissions. Server owners should tell players what authorized administrators can observe.

## Requirements and combinations

Current release 0.1.25 requires Minecraft 26.2, Fabric Loader 0.19.3+, Java 25+, Fabric API, and [TotemLibrary / TotemCore](https://modrinth.com/mod/totemlibrary) >=0.7.18 and <0.8.0 on both client and server.

Other Totem gameplay modules are optional. TotemEnchanting can make use of filled Chiseled Bookshelves; it supplies the enchanting changes itself.

[Detailed guide and source](https://github.com/Yunitrish006006/TotemVanillaTweaks) · [Report an issue](https://github.com/Yunitrish006006/TotemVanillaTweaks/issues)

---

## 繁體中文

Totemvt 是 TotemVanillaTweaks 的 Modrinth 名稱，集合物品欄整理、生存規則調整與管理員 Observer View。

### 容器整理

把游標移到物品欄或容器的一側，按滑鼠中鍵即可整理，按鍵可在設定中修改。相同物品與資料變體會合併並穩定排序；整理玩家背包時，不移動快捷列、盔甲、副手或其他裝備欄。

### 生存調整

- 講台改用四個木製半磚與一本書製作。
- 移除普通書櫃配方；生存玩家攜帶的普通書櫃每個轉成三本書，創造玩家不受該轉換影響。
- 結構生成中支援的普通書櫃與空雕紋書櫃，會成為裝有書的雕紋書櫃。
- 掉落的混凝土粉末接觸水時硬化並保留數量；僅靠近水或下雨不算。
- 漏斗從熔爐、煙燻爐或高爐取出成品時，在附近釋放對應經驗。

書櫃規則會改變既有生存玩法，加入舊世界前請先了解。

### 管理員唯讀觀察

有權限的管理員在旁觀模式使用 `/observeui <player>`，可觀察玩家視角、HUD、物品欄及支援的介面；用 /observeui stop 或 Escape 結束。

觀察者不能代替目標操作物品或按鈕。系統傳送結構化遊戲狀態，在觀察端重建支援畫面，不錄製或串流截圖、影片。原版畫面使用相符的 Minecraft 介面，Totem 畫面由背包、銅魁儡、Nexus、鎖具、木工台等所屬模組提供；不支援的畫面會明確標示。

未送出的聊天、指令、私人草稿與憑證不包含在觀察內容。雙方客戶端須相容，權限由伺服器控制；服主應告知玩家可被管理員觀察的遊戲資訊。

目前 0.1.25 需要 Minecraft 26.2、Fabric Loader 0.19.3+、Java 25+、Fabric API，以及 TotemLibrary／TotemCore >=0.7.18、<0.8.0，雙端皆需安裝。其他玩法模組為選配。
