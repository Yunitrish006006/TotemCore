# TotemCore instructions

## Module-owned Observer UI

- TotemCore owns only the versioned, client-safe Observer provider contract,
  bounded semantic envelope, read-only marker and remote cursor geometry.
- Every player-facing Totem `Screen`/`Menu` is rendered by its owning module in
  a read-only Observer mode; TotemVanillaTweaks must not draw a lookalike.
- Observer transport is permanently framebuffer-free and never carries pixels,
  screenshots or video.
- Contracts must enforce bounded state, defensive copies, version/family
  compatibility, privacy and input/packet suppression.
- Changes require unit tests, Client GameTest visual evidence, dedicated
  three-JVM E2E and Production Runtime validation.
- Provider capture/create and all handle methods are client-thread-only;
  GameTests must invoke them through client-thread context helpers.
