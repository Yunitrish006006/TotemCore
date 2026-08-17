# TotemCore API v1

The stable root is `dev.totem.core.api.v1`.  Public event values are immutable
and expose a contract version.  Only API conventions, lifecycle events,
migration dispatch, identifier/permission primitives and version negotiation
belong here.

`death.DeathBackpackNodeLifecycle` is an optional v1 contract: a Remnant-like
module may publish a backpack binding while a Nexus-like module supplies the
adapter. Core never supplies either gameplay implementation.

`death.DeathRetainedItemPolicy` is another optional v1 contract. A feature
module may authorize one Server-owned ItemStack for death retention, while the
death-owning module remains solely responsible for transactional extraction,
persistence and exactly-once respawn restoration. Authorization must not
itself mutate or copy the item.

`manual` is the shared vanilla written-book contract. Feature modules register
immutable localized sections in `TotemManualRegistry`, then explicitly
activate `TotemManualLifecycle` and delegate their own acquisition interaction
to `TotemManualPlayerHelper`. Core owns the canonical marker and deterministic
assembler, including persistent chapter subsets, server-authoritative splitting
and union-based consolidation. Core does not register an item, block, screen or
acquisition source.

`client.manual` is the client-only presentation extension. Core owns the
canonical manual's two-page `BookViewScreen` layout and navigation. Feature
modules may register optional `TotemManualPageOverlay` implementations for
page-specific diagrams. The shared layout exposes a Split action for a held
multi-section manual; ordinary written books retain vanilla rendering.

Gameplay code is prohibited: items, blocks, entities, menus, recipes, client
screens, feature-specific renderers, feature SavedData, Discord, Remnant,
Automata and Nexus implementations all belong to their feature repositories.
The canonical manual's client-only Mixin and generic overlay seam are the sole
shared presentation exception.

Patch versions retain public signatures and semantics. Minor versions add
compatible APIs. Major versions are required for incompatible changes.
Deprecated APIs remain functional for two lockstep bundle releases and one
published minor Core release, with a replacement and compatibility test.
