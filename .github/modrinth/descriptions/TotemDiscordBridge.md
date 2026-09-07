# TotemDiscordBridge

Connect Minecraft server activity to Discord through a separately deployed HTTPS relay Worker. The bridge can forward chat, player events, administrative notices, and server status so a community can follow activity outside the game.

The bridge is disabled by default. A server operator must provide and configure a compatible Worker and Discord destination before enabling it. This mod does not include hosted relay service or Discord credentials.

## Features

- Forward chat, joins, leaves, deaths, advancements, and supported server events.
- Send server online/offline status and player-count presence updates.
- Relay supported public events from installed Totem modules, including backpack recovery and container-lock audit events.
- Let players link and unlink their Minecraft and Discord accounts through the configured relay.
- Configure the server through commands and a configuration file, or use the optional administrator client interface.

## Setup

1. Install the mod, Fabric API, and [TotemLibrary (the TotemCore dependency)](https://modrinth.com/mod/totemlibrary) on the server.
2. Deploy a compatible HTTPS Worker and configure its Discord destination and shared API key. The repository README describes the required endpoints.
3. Set the Worker URL and matching key in `config/discord-bridge.json`, enable the bridge, then run `/discordbridge reload` or restart the server.

Players do not need the client mod for ordinary play or account-link commands. Administrators who want the in-game settings screen must also install the client mod and its dependencies.

## Information sent outside Minecraft

Enabled features send player names and UUIDs, chat and event content, server status, and player counts to the configured Worker and Discord destination. Account linking also sends the requesting player's identity and verification request. These records are not anonymous. Access depends on your Worker and Discord configuration; server owners should tell players what is relayed and who can read it.

The bridge requests deletion after ten minutes for certain join/leave, backpack, and status notifications. Ordinary chat, death, advancement, and audit messages have no automatic deletion request. Actual retention and deletion depend on the Worker and Discord destination. Protect relay credentials and do not include them in public configuration files or issue reports.

The server may also download and cache the official Minecraft Traditional Chinese language asset from Mojang for translated event messages.

## Compatibility

Minecraft 26.2, Fabric Loader 0.19.3 or newer, Java 25 or newer, Fabric API, and TotemCore >=0.7.0 and <0.8.0 are required. Other Totem gameplay modules are optional.

[Configuration guide and source](https://github.com/Yunitrish006006/TotemDiscordBridge) · [Report an issue](https://github.com/Yunitrish006006/TotemDiscordBridge/issues)
