# TotemAutomata

Turn Copper Golems into sorting and gathering helpers for your survival base. Configure each golem with a Copper Wrench, a home Copper Chest, fuel, tools, and rules for the items or blocks it should handle.

## Jobs and controls

- Sorting moves items from one source Copper Chest to destinations you choose. Matching stacks are merged, and items that cannot be delivered are returned to the source when possible.
- Gathering collects selected block types inside a two-corner work area, using real tools and fuel. Golems return their collected materials to their home chest.
- Manual filters let you choose accepted and rejected items without an external service.
- Optional integrations respect TotemRemnant container rules and TotemLocksmith access permissions. TotemExcavation hammers can be used as tools; a golem still works on one authorized target at a time.

To start, right-click a Copper Golem with a Copper Wrench, then right-click a Copper Chest to set its home. Choose a job in the golem screen, add fuel, and configure destinations or a gathering area before starting it. Gathering also needs a suitable tool. The project README includes the wrench recipe and full controls.

## Optional generative AI

You can configure an OpenAI-compatible endpoint to help classify items and gathering targets. This feature is optional: manual rules work without it. When enabled, requests send your configured prompts and relevant item/block names, registry identifiers, tags, expected drops, and tool information to the endpoint you select. Its operator controls processing and retention; an external provider may charge for requests. Do not put private information in prompts.

You supply the endpoint, model, and API key. Keep the key private. Decisions still pass the server's work-area, storage, and permission checks.

## Installation

Install this mod, Fabric API, and [TotemLibrary (the TotemCore dependency)](https://modrinth.com/mod/totemlibrary) on both client and server. Current releases target Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, and TotemCore >=0.7.18 and <0.8.0. Use matching release versions on both sides. Other Totem gameplay modules are optional.

[Detailed guide and source](https://github.com/Yunitrish006006/TotemAutomata) · [Report an issue](https://github.com/Yunitrish006006/TotemAutomata/issues)
