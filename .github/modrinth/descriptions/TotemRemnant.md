# TotemRemnant

Carry more on survival trips with upgradeable backpacks, and recover eligible dropped inventory through death backpacks. The mod also adds rules that prevent unsafe nesting of portable containers.

## Backpacks

- Four tiers provide 9, 18, 27, or 36 base slots, with upgrade bays.
- Upgrades add features such as crafting, material compression, matching-item pickup, extra capacity, Ender Chest access, and protection from specific hazards.
- A one-use soulbound upgrade keeps its backpack and contents through death, consuming the upgrade.
- Normal backpacks can be dyed. Right-click one to open it, or use the inventory-side storage panel while carrying it.

Use a Book on a Smithing Table to obtain the Remnant chapter of the Totem Manual. It explains backpack creation, smithing upgrades, and the available upgrade modules.

## Death recovery and limits

When enabled and `keepInventory` is off, eligible inventory contents are placed in a death backpack. It preserves ownership, resists ordinary damage and despawning, and has a visible locator beam. Capacity is limited: overflow stacks still drop normally. Portable-container rules can also leave containers as separate drops.

Administrators can configure death-backpack generation, owner-only pickup, and the additional nesting restriction through game rules. Existing invalid nested contents can be removed; new invalid insertions are rejected.

TotemNexus can provide an optional death destination. Trinkets Updated can provide optional accessory-inventory integration. Neither is required for ordinary backpacks and recovery.

## Installation

Install this mod, Fabric API, and [TotemLibrary (the TotemCore dependency)](https://modrinth.com/mod/totemlibrary) on both client and server. Current releases require Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, and TotemCore >=0.7.18 and <0.8.0. Choose compatible versions for any optional integrations.

[Recipes, upgrades, and source](https://github.com/Yunitrish006006/TotemRemnant) · [Report an issue](https://github.com/Yunitrish006006/TotemRemnant/issues)
