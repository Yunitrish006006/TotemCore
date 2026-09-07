# Modrinth description evidence

These bilingual descriptions were rebuilt through the TotemWorkspace resolve/orchestrate/context workflow, bounded module workers, and independent review. The authenticated inspection from GitHub Actions run 34137978755 is the pre-update metadata baseline. Published-version requirements take precedence over older dependency labels in the workspace graph.

Only project summaries and bodies are changed. Project names, slugs, disclosures, version dependencies, files, and moderation status are preserved.

| Module | Release | Feature evidence |
| --- | --- | --- |
| TotemCore / TotemLibrary | 0.7.18 | src/main/resources/assets/totem-core/lang/en_us.json; api/v1/manual/TotemManualPlayerHelper.java; onboarding initialization; Friendship API; docs/api-v1.md |
| TotemAlchemy | 0.1.49 | Module .github/modrinth/description-sources.md; brewing, research, mixture timing and data-pack recipes |
| TotemAutomata | 0.1.24 | README; wrench interaction and gathering controller; furnace routing; container safety bridges; LLM request construction |
| TotemDiscordBridge | 0.1.13 | README; bridge configuration, account binding commands, event subscriptions and localization |
| TotemEnchanting | 0.1.11 | README; en_us manual; data/minecraft/tags/entity_type/sensitive_to_bane_of_arthropods.json |
| TotemExcavation | 0.1.13 | README; hammer selection/excavation and mature-crop harvest implementation |
| TotemLocksmith | 0.1.10 | README; LockAccessPolicy.java; registry/LocksmithGameRules.java; en_us management manual |
| TotemNexus | 0.3.16 | README; space/NexusDistributedSpawnAuthority.java; structure scanning, travel and death-node services |
| TotemRemnant | 0.2.21 | README; BackpackUpgradeType and upgrade recipes; echo/EchoShardCrystallization.java; en_us and zh_tw item/manual text |
| TotemVanillaTweaks / Totemvt | 0.1.25 | README; inventory sorting, bookshelf/concrete/furnace behavior and Observer command/provider implementation |
| TotemVillagers | 0.1.36 | README; physical economy, woodcutter, builder and guard production; village generation and rollback mode |

Paths without a source-root prefix refer to the owning module's Java package or resources. Latest published versions were checked against the inspection; the existing SHA512-verified published-JAR dependency audit was reused for the ten gameplay modules.

Review corrections: Owners and Managers bypass Locksmith's physical-key requirement; guards consume Carved Pumpkins; Remnant's Traditional Chinese item name is 完美防消失模組; commands with angle-bracket arguments use Markdown code spans. Optional integrations are not advertised as hard requirements. Automata hammer area jobs are not advertised as shipped.

## Validation

- All 11 expected identities, old summaries and body SHA256 hashes match the authenticated baseline.
- Every manifest record changes only description/body, and each summary is below 256 characters.
- Every body contains English, Traditional Chinese, installation requirements and its source link.
- git diff --check passes in all three edited repositories.
- TotemWorkspace post-edit impact and test-plan were run. Runtime recommendations are conditional: this task changes documentation and metadata only, not game behavior, shared APIs, UI or artwork.
- Final publication requires authenticated readback of all 11 bodies and summaries, with unrelated project metadata and disclosures unchanged.
