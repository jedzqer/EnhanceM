# AGENT.md

This file provides guidance to AI coding agents (Claude Code, Cursor, GitHub Copilot, DSH, and others) when working with code in this repository.

## Project Overview

EnhanceM is a Minecraft Fabric mod (Java 25) that enhances mob behavior and difficulty. It targets Minecraft 26.2 and uses Fabric Loader 0.19.5 with Fabric API 0.160.0+26.2. Version numbers are in `gradle.properties`. Loom 1.17.20 requires Gradle 9.5.0, pinned in `gradle/wrapper/gradle-wrapper.properties`.

## Build Commands

```bash
./gradlew build          # Compile and package mod → build/libs/
./gradlew genSources     # Generate Minecraft source mappings for reference
./gradlew runClient      # Launch a test Minecraft client with the mod
./gradlew runServer      # Launch a test Minecraft server with the mod
```

CI runs on Ubuntu 24.04 with Java 25 via `.github/workflows/build.yml`.

## Source Layout

The mod uses **Fabric's split source sets** (`splitEnvironmentSourceSets()` in `build.gradle`):

- `src/main/java/net/enhancem/` — shared (server + client) code
  - `mixin/` — 31 mixin classes (registered in `src/main/resources/enhancem.mixins.json`)
  - `item/` — custom items (e.g., `RebirthPearl`)
  - `entity/` — custom entities (e.g., `EnderSoldierEntity`)
  - `network/` — network payloads (e.g., `RebirthPearlChannelPayload`)
  - `access/` — mixin accessor interfaces (e.g., `EnhancemEnderBreathMarker`)
  - `util/` — utility classes (e.g., `ExperienceDropContext` using `ThreadLocal`)
- `src/client/java/net/enhancem/client/` — client-only code
  - `mixin/` — 4 client mixin classes (registered in `src/client/resources/enhancem.client.mixins.json`)
  - `renderer/` — entity renderers (e.g., `EnderSoldierRenderer` extends `ZombieRenderer`)
  - `RebirthPearlParticleHandler.java` — client-side particle effect manager
- `src/main/resources/` — assets, data, lang files, mixin config, `fabric.mod.json`
- `src/client/resources/` — client-only mixin config

## Entry Points

- `EnhanceM.java` — `ModInitializer`: registers custom items (Rebirth Pearl, Speed Boots), custom entity (Ender Soldier), Divine Blessing enchantment (via loot table modification), sound events, creative tab entries, and the Rebirth Pearl server-side tick handler
- `EnhanceMClient.java` — `ClientModInitializer`: registers the Rebirth Pearl particle handler, Ender Soldier renderer, and client-side network receiver

## Key Features & Their Mixins

### Items & Enchantments
- **Divine Blessing**: `DivineBlessingMixin` injects into `LivingEntity.checkTotemDeathProtection` — acts as a second-chance totem. On activation, consumes armor durability down to 5%, grants regen/absorption/fire resistance, and plays a custom sound. Enchanted books found in bastion/nether fortress loot (15% chance).
- **Rebirth Pearl**: `RebirthPearl` item starts a 5-second channel (tracked via `CHANNELING` map). `RebirthPearlDamageMixin` cancels the channel on any damage. Server tick handler in `EnhanceM` teleports player to world spawn on completion. Client receives `RebirthPearlChannelPayload` packets for particle effects.
- **Speed Boots**: `SpeedBootsPlayerMixin` injects into `Player.travel` and `Player.tick` — adds a transient 1.7× land-sprint speed modifier when worn, with double hunger exhaustion.

### Ender Dragon & Ender Soldiers
- **EnderDragonHealthMixin**: 3× max health, summons Ender Soldiers at 20% HP thresholds (up to 5 thresholds, max 4 soldiers per wave). Soldiers are spawned on valid ground positions in a ring around the dragon.
- **EnderDragonFireballRateMixin**: `@ModifyConstant` lowers fireball charge threshold from 5→3 ticks; `@Redirect` keeps the dragon strafing for 2 extra fireballs per strafe run.
- **EnderBreathProjectileMixin**: 50% chance for dragon fireballs to be marked as "ender breath" on creation.
- **EnderBreathCloudMixin**: Custom `AreaEffectCloud` behavior — instead of dealing damage, teleports players randomly and angers nearby endermen.
- **EnderSoldierEntity**: Custom entity extending `Zombie` — 30 HP, 9 attack, netherite axe, projectile immunity (teleports away on hit), remembers the last attacker for 5 seconds for target prioritization.

### Zombie Variants
- **Tool generation** (`ZombieToolMixin`): Random tool on spawn — 25% copper, 25% iron, 30% gold, 15% diamond, 5% netherite. Swords have 25% chance of Fire Aspect II or Sharpness III.
- **Sword Zombie** (`ZombieSwordStateMixin`, `ZombieSwordMeleeAttackMixin`): Dual-phase combo attack animation (20 ticks total: 10+10). Client side: `ZombieRenderStateMixin`, `AbstractZombieRendererMixin`, `AbstractZombieModelMixin` handle the visual animation. Access interfaces: `SwordZombieAccess`, `SwordZombieRenderStateAccess`. Constants in `SwordZombieAttackAnimation`.
- **Axe Zombie** (`ZombieAxeMeleeAttackMixin`): Axe-wielding zombies strafe and jump away during cooldown.
- **Shield Zombie** (`ZombieShieldMixin`, `ZombieShieldAttackGoalMixin`, `ZombieShieldMeleeAttackMixin`): Shield-wielding zombies with custom attack AI.

### Other Mobs
- **Skeleton** (`FleeWhenCloseMixin`): Flees when player is near, chases when player is out of bow range.
- **Spider** (`SpiderWebMixin`): 50% cobweb spawn on hit.
- **Creeper** (`CreeperSpeedMixin`): 50% double-speed variant; slow variant applies blindness/nausea/slowness II on explosion.
- **Drowned** (`DrownedSpeedMixin`, `DrownedHitEffectMixin`, `DrownedLandSlownessMixin`): Faster swimming, -5 oxygen on hit, slower on land.
- **Horse** (`HorseSpeedMixin`): 2× speed.
- **Piglin** (`PiglinWeaponEnchantMixin`, `PiglinCrossbowHitEffectMixin`, `PiglinBruteHitEffectMixin`): Enchanted weapons, crossbow knockback + slowness, brute hit slowness + forced crouch.
- **Zombified Piglin** (`ZombifiedPiglinWeaponEnchantMixin`, `ZombifiedPiglinMeleeAttackMixin`): Enchanted weapons, hit-and-run melee AI.

### General
- **ExperienceMultiplierMixin**: 3× XP from all mob kills.
- **PlayerDeathExperienceMixin** + `ExperienceDropContext`: Player death XP uses `ThreadLocal` context flag to skip the 3× multiplier.
- **DeathDropPersistenceMixin**: Player death drops never despawn.
- **DownwardMeleeAttackMixin**: Extended downward attack range.
- **EnhanceMMixin**: Placeholder mixin.

## Mixin Conventions

- All injected methods use the `enhancem$` prefix (e.g., `enhancem$spawnCobwebAfterSuccessfulAttack`)
- Private constants and fields use `@Unique`
- `@Inject` to add code at `HEAD`/`TAIL`/`RETURN`; `@Redirect` to replace method calls; `@ModifyArg` to replace arguments; `@ModifyConstant` to change constants
- Access parent/peer class fields with `@Shadow` + `@Final`
- Mixin configs: `src/main/resources/enhancem.mixins.json` and `src/client/resources/enhancem.client.mixins.json` — both require annotations (`"overwrites": { "requireAnnotations": true }`), compatibility level `JAVA_25`

## Accessor Pattern

Mixins that implement custom interfaces use the accessor pattern rather than `@Invoker`:
- `EnhancemEnderBreathMarker` — marks `DragonFireball`/`AreaEffectCloud` instances as custom ender breath
- `SwordZombieAccess` / `SwordZombieRenderStateAccess` — share sword combo state between server logic and client rendering
- `ShieldZombieAccess` — expose shield zombie flag

## Network Layer

Uses Fabric Networking API with custom payloads. `RebirthPearlChannelPayload` is a record with a `boolean channeling` field, serialized via `StreamCodec.composite(ByteBufCodecs.BOOL, ...)`. Registered in `EnhanceM.onInitialize()` (server→client) and `EnhanceMClient.onInitializeClient()` (client receiver).

## Data-Driven Resources

- **Enchantment**: `data/enhancem/enchantment/divine_blessing.json`
- **Items**: JSON models in `assets/enhancem/models/item/` and `assets/enhancem/items/`
- **Recipe**: `data/enhancem/recipe/rebirth_pearl.json`
- **Loot injection**: Divine Blessing books and Speed Boots are injected into vanilla loot tables via `LootTableEvents.MODIFY` in `EnhanceM.java` (not through JSON loot table files)
- **Sounds**: `assets/enhancem/sounds.json` (Divine Blessing activation sound)
- **Lang**: `assets/enhancem/lang/zh_cn.json` (primary, Chinese) and `en_us.json`
