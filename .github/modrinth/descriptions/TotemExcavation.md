# Totem Excavation

Shape a mining area with two clicks, then work through it with a hammer. Totem Excavation adds seven hammer tiers and a separate hoe interaction for harvesting and replanting mature crops.

## Seven tiers, separate selections

Wood, stone, copper, iron, gold, diamond, and netherite hammers each have their own material properties and selection limits. The selection is saved on the individual hammer stack, so different hammers can keep different work areas.

The outline shows your selected area. Mining still depends on eligible blocks, the tool's capabilities and durability, loot rules, and server permission checks.

## Hammer controls

1. Hold the hammer in your **main hand**.
2. **Sneak + left-click** an eligible block to set Corner A, then another to set Corner B.
3. **Left-click normally inside the completed selection** to begin excavation.

Sneak-left-click the same unfinished corner or a saved corner to clear the selection. With a complete selection, choosing a different eligible block starts a new Corner A.

The hammer does not consume right-click, leaving normal offhand use available. Selection and excavation are checked by the server rather than allowing a client to request arbitrary remote block removal.

## Harvest and replant with a hoe

Right-click a mature supported crop with a hoe to harvest it and replant it. The action uses the crop's normal loot table and Fortune behavior, reserves a planting item, and costs one durability.

Supported crops include wheat, carrots, potatoes, beetroot, Nether Wart, and cocoa. This is a hoe interaction, separate from the hammer's area selection.

## Optional automation

TotemAutomata can use Excavation hammers as Copper Golem tools. Current golem gathering processes one authorized target at a time; it does not run a player's saved hammer area as an automatic area job.

## Getting started and requirements

Craft a hammer for your tool tier, define a small selection, and begin mining inside it. Choose a size within that hammer's limit and keep enough durability for the job.

Current release 0.1.13 requires Minecraft 26.2, Fabric Loader 0.19.3+, Java 25+, Fabric API, and [TotemLibrary / TotemCore](https://modrinth.com/mod/totemlibrary) >=0.7.18 and <0.8.0 on both client and server. TotemAutomata is optional.

[Source and controls](https://github.com/Yunitrish006006/TotemExcavation) · [Report an issue](https://github.com/Yunitrish006006/TotemExcavation/issues)

---

## 繁體中文

用兩個角點劃出工作範圍，再以槌子進行區域挖掘。Totem Excavation 提供七階槌，以及鋤頭收穫後重新種植的互動。

### 七階槌與獨立選區

木、石、銅、鐵、金、鑽石、獄髓槌各有對應材質能力與選區限制。選區保存於每一把槌子本身，因此不同槌子可保留不同工作區。挖掘仍受工具能力、耐久、掉落規則與伺服器權限限制。

### 操作方式

1. 主手拿槌，**蹲下＋左鍵**第一個方塊設定 A 點，再對第二個方塊設定 B 點。
2. 完成選區後，在範圍內**正常左鍵**開始挖掘。
3. 蹲下左鍵已選角點可清除；完成選區後選擇其他方塊則重新設定 A 點。

槌子不占用右鍵，副手的原版使用方式仍可正常運作。

### 鋤頭收割與補種

用鋤頭右鍵成熟作物，可依原版掉落與幸運效果收穫，保留一份種植材料重新種下，並消耗 1 點耐久。支援小麥、胡蘿蔔、馬鈴薯、甜菜根、地獄疙瘩及可可。這是獨立的鋤頭功能，不是槌子的選區操作。

### 選配與安裝

安裝 TotemAutomata 後，銅魁儡可使用這些槌子，但目前仍一次處理一個合法目標，不會自動執行玩家保存的整片槌子選區。

目前 0.1.13 需要 Minecraft 26.2、Fabric Loader 0.19.3+、Java 25+、Fabric API，以及 TotemLibrary／TotemCore >=0.7.18、<0.8.0，客戶端與伺服器皆需安裝。
