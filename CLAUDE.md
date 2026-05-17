# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EnhanceM is a Minecraft Fabric mod (Java 25) that enhances mob behavior and difficulty. It targets Minecraft 26.1.2 and uses Fabric Loader 0.19.2 with Fabric API 0.147.0+26.1.2.

## Build Commands

```bash
./gradlew build          # Compile and package mod → build/libs/
./gradlew genSources     # Generate Minecraft source mappings for reference
```

CI runs on Ubuntu 24.04 with Java 25 via `.github/workflows/build.yml`.

## Architecture

The mod uses **Fabric mixins** to inject behavior into Minecraft entity classes without modifying original code.

**Entry points:**
- `src/main/java/net/enhancem/EnhanceM.java` — server-side `ModInitializer`
- `src/client/java/net/enhancem/client/EnhanceMClient.java` — client-side `ClientModInitializer`

**Utility classes** (`src/main/java/net/enhancem/`):
- `MobWeaponEnchantments.java` — applies Fire Aspect / Knockback enchantments to mob weapons via Minecraft's registry
- `EndermanBlackHoleManager.java` — record-based manager tracking active black hole effects; runs per-tick to pull entities, deal damage, and teleport players
- `ShieldZombieAccess.java` — mixin accessor interface for shield-related zombie data

**Mixins** (`src/main/java/net/enhancem/mixin/`) — 23 classes covering:
- Zombie: speed (0.35), tool generation, shield attacks
- Spider: 50% cobweb spawn on hit
- Creeper: dual speed variants with status effects
- Drowned: speed, oxygen drain, land slowness
- Piglin/Zombified Piglin: weapon enchantments, crossbow knockback
- Enderman: death black hole via `EndermanBlackHoleManager`
- Horse: 2× speed multiplier
- Skeleton: flee when player is close
- General: 3× XP multiplier, death drop persistence, extended downward melee range

## Mixin Conventions

- All injected methods use the `enhancem$` prefix (e.g., `enhancem$spawnCobwebAfterSuccessfulAttack`)
- Private constants use `@Unique`
- Use `@Inject` to add code at call sites; use `@Redirect` to replace method calls
- Access parent class fields with `@Shadow` + `@Final`
- Mixin registry: `src/main/resources/enhancem.mixins.json` (compatibility level: JAVA_25)
