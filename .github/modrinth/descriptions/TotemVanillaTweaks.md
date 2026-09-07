# Totemvt / TotemVanillaTweaks

A collection of survival adjustments, inventory conveniences, and an administrator Observer View for Minecraft servers.

## Gameplay changes

- Sort the hovered player or container inventory with the middle mouse button. The key can be changed in Controls; player sorting leaves the hotbar and equipment alone.
- Craft a Lectern with four wooden slabs and one Book.
- Ordinary Bookshelf items carried in survival become books, and the ordinary Bookshelf crafting recipe is removed. Supported generated shelves become book-filled Chiseled Bookshelves.
- Dropped Concrete Powder items harden into concrete when they contact water.
- Hoppers extracting furnace, smoker, or blast-furnace output release the associated recipe experience nearby.

The bookshelf conversion changes survival behavior, so consider it before adding the mod to an existing world.

## Administrator Observer View

Authorized administrators in Spectator mode can use `/observeui <player>` to view a player's perspective and supported inventory or interface state. Use `/observeui stop` or Escape to stop observing.

The observation is read-only. It relays supported game state for local rendering rather than recording or streaming screenshots or video. Unsent chat, commands, and private input fields are excluded. Some modded screens require a compatible provider from their owning mod; unsupported screens are identified as unsupported. Both the observed player and observer need compatible client installations, and the server controls access.

Server owners should tell players that authorized administrators can observe supported game and inventory state.

## Installation

Install this mod, Fabric API, and [TotemLibrary (the TotemCore dependency)](https://modrinth.com/mod/totemlibrary) on both client and server. Current releases require Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, and TotemCore >=0.7.18 and <0.8.0. Other Totem gameplay modules are optional.

[Full feature guide and source](https://github.com/Yunitrish006006/TotemVanillaTweaks) · [Report an issue](https://github.com/Yunitrish006006/TotemVanillaTweaks/issues)
