# Totem Locksmith

Protect access to a storage network with one physical Padlock. Totem Locksmith connects chest and barrel permissions with keys, shared friendships, and controls for automation.

## One lock for connected storage

A Padlock can protect a Chest, Trapped Chest, Barrel, double chest, or supported storage linked by fixed Hoppers. The first locked container becomes the network root.

Connected members share that lock. If a middle Hopper is broken, the part disconnected from the root loses its lock protection; the root component remains protected. Transfers within one locked network can continue, while boundary transfers follow its automation policy.

## Keys, roles, and access

- Choose access policies such as private, friends, members, or public.
- Assign managers and users, or block a player.
- Issue physical Bound Keys from Key Blanks, revoke individual keys, or rotate the set.
- A physical key must be held in the main hand or offhand to grant its access. Blocked players remain denied even with a key.
- The default physical-key world rule makes access depend on keys for players who are neither owners nor managers. Disabling that rule allows more convenient direct policy-based access.

The owner can remove a lock through its management screen and recover the Padlock. A Bound Key can be recycled into a Key Blank.

## Storage access is not a land claim

Locks do not make containers indestructible. Other players can still break them when ordinary Minecraft rules permit it. Supported non-owner break events can be sent through optional TotemDiscordBridge; notifications do not prevent the break.

Shared friendships come from TotemLibrary / TotemCore. TotemNexus is not required for Locksmith's dependency. Optional TotemAutomata integration checks the operator's access before a golem moves items.

## Getting started

1. Craft a Padlock from the live recipe and use it on a supported chest or barrel.
2. Sneak-use an empty hand on a locked container or member Hopper to open management when authorized.
3. Configure members and access, then use the Keys tab with a Key Blank in either hand to issue keys.
4. Use a Book or Totem Manual on a Chest to record the Locksmith guide.

The in-game guide shows the current Padlock and Key Blank recipes. Commands remain available as an administrator fallback.

## Requirements

Current release 0.1.10 requires Minecraft 26.2, Fabric Loader 0.19.3+, Java 25+, Fabric API, and [TotemLibrary / TotemCore](https://modrinth.com/mod/totemlibrary) >=0.7.18 and <0.8.0 on both client and server.

[Permissions guide and source](https://github.com/Yunitrish006006/TotemLocksmith) · [Report an issue](https://github.com/Yunitrish006006/TotemLocksmith/issues)

---

## 繁體中文

用一個實體掛鎖管理整個儲存網路的存取，結合鑰匙、好友、成員角色與自動化權限。

### 一鎖連動整個網路

支援箱子、陷阱箱、木桶、雙箱，以及固定漏斗連接的相容儲存容器。第一個上鎖容器是根節點，連接成員共用同一把鎖；中間漏斗被拆除後，只有仍連到根節點的部分維持鎖定。

網路內部的漏斗搬運可繼續運作，邊界輸入與輸出則依設定判定。

### 實體鑰匙與角色

可設定私人、好友、成員或公開存取，分配管理員與使用者，或封鎖特定玩家。由鑰匙胚發出綁定鑰匙後，可單獨撤銷或整批輪替。鑰匙須拿在主手或副手；被封鎖的玩家即使有鑰匙也不會取得權限。

世界預設要求既非擁有者、也非管理員的玩家使用實體鑰匙；關閉相應規則後，可改用較便利的直接權限模式。擁有者能透過管理介面移除鎖並取回掛鎖，綁定鑰匙也可回收成鑰匙胚。

### 使用與重要限制

把掛鎖用在相容箱子或木桶上，再以空手蹲下使用已鎖容器或成員漏斗開啟管理。於鑰匙分頁拿著鑰匙胚即可發鑰匙；用書或手冊對箱子使用可記錄教學與目前配方。

此模組管理的是**存取權限，不是領地防拆**。原版規則允許時，其他玩家仍能拆除容器。選配 DiscordBridge 可傳送相關通知，Automata 可檢查銅魁儡操作權限；好友資料由 Core 共用，不要求安裝 Nexus。

目前 0.1.10 需要 Minecraft 26.2、Fabric Loader 0.19.3+、Java 25+、Fabric API，以及 TotemLibrary／TotemCore >=0.7.18、<0.8.0，雙端皆需安裝。
