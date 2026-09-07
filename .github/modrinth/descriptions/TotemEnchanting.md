# TotemEnchanting

Build an enchanting library where the books themselves matter. TotemEnchanting reads the contents of Chiseled Bookshelves, raises the available enchanting-power ceiling, and gives Bane of Arthropods a wider set of targets.

## Books become enchanting power

- Each normal Book contributes **1 power**.
- An Enchanted Book contributes the **sum of its enchantment levels**. A book with levels III and II contributes 5.
- Filled Chiseled Bookshelves in valid vanilla shelf positions combine up to **64 power**. Keep the gap to the Enchanting Table clear.
- Particles identify contributing shelves and respond to their contents, making a working library easier to read.

## Higher-power enchanting

The displayed level requirement can rise beyond 30. The mod evaluates additional high-power candidates while keeping Minecraft's item and enchantment compatibility rules. It retains the comparable level-30 baseline so a higher-power setup does not simply lose good options because of vanilla power windows.

This improves the possibilities rather than guaranteeing a chosen enchantment. Check the displayed requirement before enchanting; the mod does not add its own enchantment IDs or bypass incompatibilities.

## Expanded Bane of Arthropods

Bane of Arthropods keeps its vanilla targets and additionally affects Skeletons, Strays, Bogged, Wither Skeletons, Skeleton Horses, and Parched. Its bonus damage and Slowness apply to these added targets as well.

## Getting started

1. Craft and place an Enchanting Table.
2. Place Chiseled Bookshelves at valid bookshelf positions and insert Books or Enchanted Books.
3. Use a Totem Manual or plain Book on the table to record the Enchanting chapter.
4. Open the table, compare the available options, and enchant normally.

## Requirements and optional combinations

Current release 0.1.11 targets Minecraft 26.2, Fabric Loader 0.19.3+, Java 25+, Fabric API, and TotemCore >=0.7.18 and <0.8.0. Install the mod and its dependencies on both client and server.

[TotemLibrary](https://modrinth.com/mod/totemlibrary) supplies the TotemCore dependency. Other Totem gameplay modules are optional. TotemVanillaTweaks can provide book-filled Chiseled Bookshelves through its separate survival changes, but those changes are not part of this mod.

[Source and detailed guide](https://github.com/Yunitrish006006/TotemEnchanting) · [Report an issue](https://github.com/Yunitrish006006/TotemEnchanting/issues)

---

## 繁體中文

讓藏書真正影響附魔。TotemEnchanting 會讀取雕紋書櫃內的書本內容，提高可用附魔力上限，並擴充節肢剋星的有效目標。

### 藏書與附魔品質

- 普通書每本提供 **1 點附魔力**。
- 附魔書提供其所有附魔等級的總和，例如 III 與 II 合計 5 點。
- 位於原版有效書櫃位置的雕紋書櫃可累加至 **64 點**，書櫃與附魔台之間須保持暢通。
- 粒子會指出正在提供力量的書櫃，並依書本內容變化。
- 30 級以上可取得額外的品質候選，保留同條件的 30 級基準；仍遵守原版物品與附魔相容規則，不保證指定附魔。

畫面顯示的等級需求可能超過 30，使用前請確認成本。

### 節肢剋星目標擴充

除了原版節肢生物，也對骷髏、流髑、沼骸、凋零骷髏、骷髏馬及 Parched 生效，套用相應的額外傷害與緩速。

### 快速開始

放置附魔台，在有效位置擺上裝有普通書或附魔書的雕紋書櫃。拿手冊或普通書對附魔台使用可記錄教學，再開啟附魔台選擇結果。

目前 0.1.11 需要 Minecraft 26.2、Fabric Loader 0.19.3+、Java 25+、Fabric API，以及 TotemLibrary／TotemCore >=0.7.18、<0.8.0，客戶端與伺服器皆需安裝。其他玩法模組為選配；TotemVanillaTweaks 的書櫃生存調整屬於該模組的功能。
