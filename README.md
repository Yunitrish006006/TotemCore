# TotemCore

TotemCore is the library-only foundation for Totem feature mods.  It owns
versioned public contracts, event/lifecycle types, migration helpers,
identifier/permission primitives, and API-version negotiation.  It never owns
gameplay registry content, client UI, Mixins, or feature SavedData.

The API contract and deprecation policy are in
[`docs/api-v1.md`](docs/api-v1.md).  Version `0.1.0-SNAPSHOT` is the first
development artifact; consumers must pin an exact tested version.
