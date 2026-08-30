# TotemCore API v1

The stable root is `dev.totem.core.api.v1`. Public event values are immutable
and expose a contract version. API conventions, lifecycle events, migration
dispatch, identifier/permission primitives, shared social relationship
primitives and version negotiation belong here.

`social` is the canonical Totem-wide friendship contract. Core owns the
server-authoritative friendship and pending-invitation data plus the stable
`TotemFriendshipApi`. Feature modules must query or mutate friendships through
that API instead of mirroring social state. The persisted storage identifier
remains `deadrecall:space_friends` in Core 0.7.x so worlds created by older
Nexus builds retain existing friendships and invitations without a destructive
copy migration. Nexus may provide friend-management UI and use friendships for
teleportation; Locksmith and other modules may consume the same Core relation.

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

`client.world` is the client-only, stateless world-outline primitive. Feature
modules submit block or cuboid outlines from their own render callback using an
immutable colour, line width and explicit `DEPTH_TESTED` or `THROUGH_WALLS`
occlusion mode. Core does not retain positions, register a feature renderer or
own selection, preview, packet, permission, timer or cleanup state.

Gameplay code is prohibited: items, blocks, entities, menus, recipes, client
screens, feature-specific renderers, Discord, Remnant, Automata and Nexus
implementations all belong to their feature repositories. Feature-specific
SavedData also stays outside Core. Cross-module identity/relationship state is
an explicit exception when Core is the canonical owner, as with `social`.
The canonical manual's client-only Mixin and generic overlay seam, plus the
stateless world-outline primitive, are the shared presentation exceptions.

Patch versions retain public signatures and semantics. Minor versions add
compatible APIs. Major versions are required for incompatible changes.
Deprecated APIs remain functional for two lockstep bundle releases and one
published minor Core release, with a replacement and compatibility test.
