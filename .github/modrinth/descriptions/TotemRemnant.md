# TotemRemnant

Build a backpack for the way you travel, keep useful storage beside your inventory, and recover eligible items after death. TotemRemnant combines modular backpacks, Echo Shard crystallization, and configurable portable-container safety.

## Four backpack tiers

Normal backpacks have **9, 18, 27, or 36 base slots** and **1, 2, 3, or 4 upgrade bays**. Make and upgrade them at a Smithing Table; the in-game manual shows the live recipes.

Right-click a backpack to open it. Carried backpacks also expose real, usable slots beside the normal inventory screen. When carrying several, hover a backpack to choose the displayed one.

Dye normal backpacks with vanilla dyes, mix colors through repeated dyeing, and wash them in a water-filled cauldron. Contents and custom data stay with the backpack. The Netherite tier has built-in fire protection.

## Choose your upgrades

- **Crafting:** an integrated 3×3 crafting area.
- **Metal compaction:** compress supported metals using the world's current recipes.
- **Matching pickup:** collect matching item variants already stored in the backpack.
- **Capacity:** add rows of nine slots; removable only when the affected space is empty.
- **Ender access:** open your own Ender Chest storage.
- **Single-use soulbound retention:** keep the backpack and its contents through death, consuming the installed charge.
- **Protection modules:** protect against specific dropped-item hazards.
- **Perfect Preservation:** combine blast/cactus, fire, despawn, and void protection into one bay. The individual protection modules remain usable.

Upgrades have real capacity and slot limits. Extra storage rows can be scrolled without shrinking the entire interface.

## Echo Shard crystallization

Use an Amethyst Shard on a Sculk block to obtain an Echo Shard. The consumed Sculk becomes Deepslate and does not also release its experience. Sensors, Shriekers, Catalysts, and Sculk Veins are not valid targets.

## Death recovery

With death backpacks enabled and keepInventory off, eligible contents are captured in a red death backpack. It retains ownership, has a locator beam, resists ordinary damage and despawning, and is rescued from below the world.

A death backpack holds up to 54 stacks; overflow drops normally. Portable containers can remain separate under nesting rules, and Curse of Vanishing still takes priority. Successful pickup into the inventory completes the matching Nexus recovery node. Full inventories do not count; foreign pickup is rejected when the owner-only rule is enabled; emptying and closing an already carried backpack remains supported.

World rules control generation, owner-only recovery restrictions, and the additional nesting policy. Existing invalid nested contents can be removed without silently deleting them.

## Optional integrations and installation

TotemNexus 0.3.19 can follow the moving backpack for close death destinations and support retention of one eligible travel item. Trinkets Updated provides supported accessory-inventory capture. These integrations are optional.

Use a Book or Totem Manual on a Smithing Table to record the guide. Current release 0.2.24 requires Minecraft 26.2, Fabric Loader 0.19.3+, Java 25+, Fabric API, and [TotemLibrary / TotemCore](https://modrinth.com/mod/totemlibrary) >=0.7.19 and <0.8.0 on both client and server.

[Recipes and detailed guide](https://github.com/Yunitrish006006/TotemRemnant) · [Report an issue](https://github.com/Yunitrish006006/TotemRemnant/issues)

---

## 繁體中文

依照旅行需求打造背包，直接在物品欄旁操作收納，並在死亡後回收符合條件的物品。TotemRemnant 結合模組化背包、回聲碎片結晶與可調整的容器巢狀安全規則。

### 四階背包與實用收納

四階背包分別提供 **9／18／27／36 格**基礎容量，以及 **1／2／3／4 個**擴充槽。於鍛造台製作與升級，手冊顯示目前世界配方。

可右鍵開啟，也能使用原版物品欄旁的真實背包格位。攜帶多個背包時，把游標移到背包上即可切換。一般背包可用原版染料混色，再用水煉藥鍋洗色；內容與資料會保留，獄髓階另自帶防火。

### 自由搭配擴充

提供內嵌 3×3 合成、金屬壓縮、同類物品收納、每次增加九格容量、自己的終界箱存取，以及消耗一次充能的死亡靈魂綁定。

個別防護模組可抵抗特定掉落危害；**完美防消失模組（Perfect Preservation）**能把爆炸／仙人掌、火焰、自然消失與虛空防護合併到一個擴充槽。原本的個別模組仍可使用。容量模組拆除前必須清空受影響格位，超出畫面的大背包可以捲動儲存列。

### 回聲結晶與死亡背包

拿紫水晶碎片對 Sculk 方塊使用，可取得回聲碎片；Sculk 轉為深板岩，不再額外釋放經驗。感測器、尖嘯體、觸媒與菌脈不適用。

開啟死亡背包且 keepInventory 關閉時，符合條件的物品會收進紅色死亡背包，保留歸屬並顯示定位光柱，抵抗一般損傷、自然消失與掉入虛空。容量最多 54 組，超量仍正常掉落；容器可能依巢狀規則分開掉落，消失詛咒仍優先。成功拾取至物品欄後完成對應 Nexus 節點回收；物品欄已滿不算成功，啟用主人限定規則時會拒絕他人拾取，已攜帶背包仍支援取空並關閉的回收流程。

管理員可調整死亡背包生成、主人限制與額外巢狀規則。既有不合法巢狀內容可取出，不會被直接刪除。

### 整合與需求

選配 Nexus 0.3.19 提供追蹤移動背包的近距離死亡回收點及一件有效傳送介面的死亡保留；Trinkets Updated 提供支援的飾品物品欄擷取。拿書或手冊對鍛造台使用可取得教學。

目前 0.2.24 需要 Minecraft 26.2、Fabric Loader 0.19.3+、Java 25+、Fabric API，以及 TotemLibrary／TotemCore >=0.7.19、<0.8.0，雙端皆需安裝。
