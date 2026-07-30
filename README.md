# TotemCore

TotemCore 是 Totem 系列功能模組的共用 API 基礎。它只提供跨模組契約、
生命週期介面與 API 版本協商，不註冊物品、方塊、GUI、Mixin 或 SavedData。

目前候選版本為 **0.2.0**，API root 為 `dev.totem.core.api.v1`。

## 誰需要安裝

- 一般玩家不會單獨從 TotemCore 得到玩法；它必須搭配至少一個 Totem
  功能模組。
- 使用 DeadRecall 2.4.4 整合 JAR 時不需另外安裝，整合包已內含
  TotemCore。
- 使用獨立模組時，把 `totem-core-0.2.0.jar` 與功能模組一起放進
  Client／Server 的 `mods/`。
- 所有目前候選功能模組都精確要求 `totem-core =0.2.0`，不要以其他
  版本替換。

## 相容需求

| 項目 | 需求 |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3+ |
| Java | 25+ |
| Fabric API | Core 本身不要求；功能模組通常要求 |

## 提供的 API

| API | 用途 |
| --- | --- |
| `TotemEvent` | 不可變、帶契約版本的跨功能事件 marker |
| `ApiVersion` | 驗證同 major、足夠 minor 的 API 相容性 |
| `DeathBackpackNodeLifecycle` | Remnant 與 Nexus 間的選配死亡節點生命週期 |

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

## API 版本政策

- Patch 版本保留公開 signature 與語意。
- Minor 版本只加入向後相容 API。
- 不相容變更必須升 major。
- Deprecated API 至少保留兩個 lockstep bundle release 與一個已發佈
  Core minor release，並提供替代方式與相容測試。

完整契約見 [TotemCore API v1](docs/api-v1.md)。

## 開發與建置

需要 Java 25：

```bash
./gradlew build
```

輸出位於 `build/libs/`。功能 repository 應把通過測試的精確 Core 版本
寫入 `fabric.mod.json`，並避免直接依賴其他功能模組。
