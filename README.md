# Baby Mobs

Baby Mobs adds custom baby variants of Minecraft mobs while preserving the identity and behavior of the original mobs.

These are more than simply scaled-down models. Baby variants can have their own proportions, movement speeds, damage, accuracy, spawning rules, and encounter behavior.

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

### Special Encounters

Some mobs use rules that fit their normal Minecraft spawning behavior instead of the standard 8% chance.

#### Pillager Outposts

Pillager Outposts contain one or two baby Pillagers.

Raid and patrol Pillagers are left unchanged.

#### Snow Golems

Adult Snow Golems maintain a baby Snow Golem companion nearby.

#### Iron Golems

Village-created adult Iron Golems can maintain a baby Iron Golem companion nearby.

Player-created Iron Golems are left unchanged.

#### Wandering Traders

Normal Wandering Trader caravans receive a baby Wandering Trader companion with two baby Trader Llamas.

The baby trader remains able to trade and follows the caravan's normal despawn timing.

## Baby Behavior

Baby mobs retain the core behavior of their adult counterparts, but individual variants have been tuned to make them feel distinct.

Examples include:

- smaller physical size with custom juvenile proportions
- increased movement speed
- reduced melee or projectile damage where appropriate
- reduced ranged accuracy
- smaller Creeper explosions
- weaker Witch potion attacks
- custom encounter and companion spawning

Existing vanilla baby mobs such as Zombie Villagers, Drowned, and Husks are not replaced by Baby Mobs.

## Commands

Baby Mobs includes summon commands for testing and administration:

    /babymobs summon <mob> baby
    /babymobs summon <mob> adult

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

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.158.0+26.2 or newer
- Java 25

For multiplayer, install Baby Mobs and Fabric API on the server and participating clients.

## Resource Packs

Baby Mobs uses Minecraft's existing entity types rather than replacing mobs with entirely new entity registrations.

Its custom baby rendering is designed to remain compatible with resource packs and custom entity textures/models as much as possible.

## Development

Build with:

    ./gradlew clean build

The release JAR is produced in:

    build/libs/

## Disclaimer

Baby Mobs is an unofficial Minecraft mod and is not affiliated with or endorsed by Mojang Studios or Microsoft.
