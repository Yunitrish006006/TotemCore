# Totem Locksmith

Control who can open and automate your storage. One Padlock can protect a supported chest, barrel, double chest, or a storage network connected by fixed Hoppers.

## Locks and permissions

The first locked container is the network root. Connected supported containers share its lock; if a connector breaks, only the part still connected to the root remains locked.

Owners can manage friends, members, blocked players, managers, physical keys, and separate automation permissions. Friendship comes from the shared TotemCore system. Optional TotemAutomata integration checks golem access, and TotemDiscordBridge can relay supported audit events.

Locks control access, not block destruction. Other players can still break containers when normal Minecraft rules permit it. Do not treat this mod as a complete land-claim or anti-grief system.

## Getting started

Craft a Padlock and use it on a Chest, Trapped Chest, or Barrel. Use a Book or Totem Manual on a Chest to obtain the Locksmith chapter. Sneak-use a locked root container with an empty hand to inspect its policy, or use `/locksmith` to manage it.

## Installation

Install this mod, Fabric API, and [TotemLibrary (the TotemCore dependency)](https://modrinth.com/mod/totemlibrary) on both client and server. Current releases require Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, and TotemCore >=0.7.18 and <0.8.0. Other Totem gameplay modules are optional.

[Permissions guide and source](https://github.com/Yunitrish006006/TotemLocksmith) · [Report an issue](https://github.com/Yunitrish006006/TotemLocksmith/issues)
