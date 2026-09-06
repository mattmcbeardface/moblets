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
