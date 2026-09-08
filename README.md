# Moblets

Moblets adds custom baby variants of Minecraft mobs while preserving the identity and behavior of the original mobs.

These are more than simply scaled-down models. Baby variants can have their own proportions, movement speeds, damage, accuracy, spawning rules, sounds, and encounter behavior.

## Supported Versions

Minecraft 26.2
- Fabric
- NeoForge
- Forge
- Java 25

Minecraft 26.1.2
- Fabric
- NeoForge
- Forge
- Java 25

Download the JAR that matches both your Minecraft version and mod loader.

## Supported Mobs

### Natural Baby Variants

The following mobs have an 8% chance for a naturally spawned mob to become a baby variant:

- Creeper
- Enderman
- Witch
- Skeleton
- Stray
- Bogged
- Parched
- Wither Skeleton
- Camel Husk

Baby Camel Husk encounters also convert their Husk and Parched passengers into baby variants.

## Special Encounters

### Pillager Outposts

Pillager Outposts contain one or two baby Pillagers.

Raid and patrol Pillagers are left unchanged.

### Snow Golems

Adult Snow Golems maintain a baby Snow Golem companion nearby.

### Iron Golems

Village-created adult Iron Golems can maintain a baby Iron Golem companion nearby.

Player-created Iron Golems are left unchanged.

### Wandering Traders

Normal Wandering Trader caravans receive a baby Wandering Trader companion with two baby Trader Llamas.

The baby trader remains able to trade, follows the caravan's normal despawn timing, and uses a higher-pitched version of the vanilla Wandering Trader voice.

## Baby Behavior

Baby mobs retain the core behavior of their adult counterparts, but individual variants have been tuned to make them feel distinct.

Examples include:

- Smaller physical size with custom juvenile proportions
- Increased movement speed
- Reduced melee or projectile damage where appropriate
- Reduced ranged accuracy
- Smaller Creeper explosions
- Weaker Witch potion attacks
- Custom encounter and companion spawning
- Baby-specific sound adjustments where appropriate

Existing vanilla baby mobs such as Zombie Villagers, Drowned, and Husks are not replaced by Moblets.

## Tamed Companions

Baby Skeletons, Strays, Bogged, Parched, Wither Skeletons, Creepers, Pillagers, and Witches can be tamed. Each variant has its own taming item and chance:

| Moblet | Taming item | Chance |
| --- | --- | ---: |
| Skeleton | String | 25% |
| Stray | Rabbit Hide | 40% |
| Bogged | Red or Brown Mushroom | 25% |
| Parched | Water Potion | 50% |
| Wither Skeleton | Nether Wart | 20% |
| Creeper | Firework Rocket | 25% |
| Pillager | Gold Ingot | 33% |
| Witch | Fermented Spider Eye | 25% |

Owners can switch companions between Follow and Stay modes. Ownership, Stay mode, and Stay anchors persist across world saves. Tamed companions avoid targeting players and other tamed Moblets, recover slowly while out of combat, and can be healed by their owners.

Skeleton-family companions support owner-managed armor and specialized weapons: bows for ranged Skeleton variants and swords for Wither Skeletons. Creepers accept helmets, use controlled hostile-only explosions without terrain damage, and survive their own blasts. Pillagers use crossbows.

Tamed Witches are noncombatant support companions. In Follow mode they flee hostile attackers and throw Strong Healing splash potions at an owner below half health. In Stay mode they wander near their anchor and trade deterministic potion offers with any player. Witch ranks are Hedge Witch, Cauldron Keeper, Hexbinder, Coven Elder, and High Witch.

## Commands

Moblets includes summon commands for testing and administration:

    /moblets summon <mob> baby
    /moblets summon <mob> adult

Supported command names:

    skeleton
    creeper
    enderman
    witch
    stray
    bogged
    parched
    wither_skeleton
    pillager
    snow_golem
    iron_golem
    wandering_trader
    camel_husk

## Requirements

### Minecraft 26.2

Fabric:
- Fabric Loader 0.19.3 or newer
- Fabric API 0.158.0+26.2 or newer

NeoForge:
- NeoForge 26.2.0.75 or newer

Forge:
- Forge 65.1.3 or newer

Java:
- Java 25

### Minecraft 26.1.2

Fabric:
- Fabric Loader 0.19.3 or newer
- Fabric API 0.154.2+26.1.2 or newer

NeoForge:
- NeoForge 26.1.2.100 or newer

Forge:
- Forge 64.1.3 or newer

Java:
- Java 25

For multiplayer, install Moblets on the server and participating clients.

Fabric installations also require Fabric API.

## Installation

Install the supported loader for your Minecraft version, then place the matching Moblets JAR in the instance or server `mods` directory. Fabric installations must also include Fabric API. Use the same Moblets version and loader family on the server and participating clients.

## Configuration

Moblets creates `config/moblets.json` on first launch. The file controls natural spawn chances, whether supported companion types can be tamed, and whether special encounters are enabled. Stop the game or server before editing the file.

## Resource Packs

Moblets uses Minecraft's existing entity types rather than replacing mobs with entirely new entity registrations.

Its custom baby rendering is designed to remain compatible with resource packs and custom entity textures and models as much as possible.

## Development

Moblets uses a shared multi-loader project structure:

    common/
    fabric/
    neoforge/
    forge/

Most gameplay and rendering logic is shared in common, with loader-specific integration handled by the individual loader modules.

Build all supported loaders with:

    ./gradlew clean build

Release JARs are produced in:

    fabric/build/libs/
    neoforge/build/libs/
    forge/build/libs/

The main branch targets Minecraft 26.2.

The mc-26.1.2 branch targets Minecraft 26.1.2.

## Disclaimer

Moblets is an unofficial Minecraft mod and is not affiliated with or endorsed by Mojang Studios or Microsoft.
