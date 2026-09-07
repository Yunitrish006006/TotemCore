# Totem Villagers

Make village trading depend on a physical economy. Villagers keep goods, materials, tools, food, and emeralds; completed work supplies the items they can sell through Minecraft's normal trade screen.

## A working village

- Villagers sell merchandise they actually have. Sold-out goods disappear instead of receiving free restocks.
- Player trades supply real materials and use the villager's available emeralds.
- Workers use loaded workstations and suitable recipes to produce goods. Librarians can make enchanted books and equipment, and Cartographers can produce explorer maps.
- Specialist miners, lumberjacks, builders, and guards support gathering, construction, and defense with configured work areas and posts.
- Villagers need food and working supplies. Hunger, unavailable materials, or unloaded work areas can pause production.

## Before you install

Work-backed trading is enabled by default and changes the availability of trades. Existing vanilla sell stock is not converted into free merchandise. This is a village-economy overhaul, so back up an existing world before adopting it.

Keep workers, their workstations, and resource areas loaded, and supply the village through normal trading. Players cannot directly open the villagers' protected work inventories. Optional TotemRemnant integration adds backpack smithing.

## Administration

Use `/totemvillagers mode status` to inspect the world setting. Administrators can configure roles, work zones, builder sites, and guard posts using `/totemvillagers` commands. `/totemvillagers mode vanilla_rollback` restores vanilla trading behavior while retaining the mod's saved work state for a later return. The README describes specialist setup and the economy's resource requirements.

## Installation

Install this mod, Fabric API, and [TotemLibrary (the TotemCore dependency)](https://modrinth.com/mod/totemlibrary) on both client and server. Current releases require Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, and TotemCore >=0.7.18 and <0.8.0. Other Totem gameplay modules are optional.

[Economy and setup guide](https://github.com/Yunitrish006006/TotemVillagers) · [Report an issue](https://github.com/Yunitrish006006/TotemVillagers/issues)
