# TotemCore API v1

The stable root is `dev.totem.core.api.v1`.  Public event values are immutable
and expose a contract version.  Only API conventions, lifecycle events,
migration dispatch, identifier/permission primitives and version negotiation
belong here.

Gameplay code is prohibited: items, blocks, entities, menus, recipes, client
screens, renderers, Mixins, feature SavedData, Discord, Remnant, Automata and
Nexus implementations all belong to their feature repositories.

Patch versions retain public signatures and semantics. Minor versions add
compatible APIs. Major versions are required for incompatible changes.
Deprecated APIs remain functional for two lockstep bundle releases and one
published minor Core release, with a replacement and compatibility test.
