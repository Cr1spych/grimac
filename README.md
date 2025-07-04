# GrimAC

GrimAC is an open source Minecraft anticheat designed for 1.21 and supports 1.8-1.21. It is free while in beta. It will eventually become paid and/or will include offering additional subscription based paid checks. Geyser players are fully exempt.

This project is considered feature complete for the 2.0 (open-source) branch of this project. If you would like a bugfix or enhancement and cannot sponsor the work, pull requests are welcome.

## Downloads
- [Modrinth](https://modrinth.com/plugin/grimac)
- [Hangar](https://hangar.papermc.io/GrimAnticheat/GrimAnticheat)
- [SpigotMC](https://www.spigotmc.org/resources/grim-anticheat.99923/)
- *For bleeding edge builds use GitHub artifacts*: [Bukkit](https://nightly.link/GrimAnticheat/Grim/workflows/gradle-publish/2.0/grimac-bukkit.zip), [Fabric](https://nightly.link/GrimAnticheat/Grim/workflows/gradle-publish/2.0/grimac-fabric.zip)

## Installation notes
> [!WARNING]
> Java 17 is now required. More information [here](https://github.com/GrimAnticheat/Grim/wiki/Updating-to-Java-17).
- Paper, Spigot, Folia, and Fabric are currently supported.
- If you use Geyser, place Floodgate on the backend server so grim can exempt bedrock players. Grim cannot access the Floodgate API if it is on the proxy.
- If you use ViaVersion, it should be on the backend server as movement is highly dependent on client version.

## Support & wiki information
- Support & discussion: [Discord](https://discord.com/invite/kqQAhTmkUF)
- Report issues: [Issues](https://github.com/GrimAnticheat/Grim/issues/new/choose)
- Wiki & examples: [Wiki](https://github.com/GrimAnticheat/Grim/wiki)

## Developer API
Grim's API allows you to integrate Grim into your own plugins.
- API repository: [GrimAPI](https://github.com/GrimAnticheat/GrimAPI)
- Wiki info: [Wiki](https://github.com/GrimAnticheat/GrimAPI)

## How to compile

1. `git clone https://github.com/GrimAnticheat/Grim.git`
2. `cd Grim`
3. `./gradlew build`
4. The final jars will compile into the `<platform>/build/libs` folders

## Grim supremacy

What makes Grim stand out against other anticheats?

### Movement Simulation Engine

* We have a 1:1 replication of the player's possible movements
    * This covers everything from basic walking, swimming, knockback, cobwebs, to bubble columns
    * It even covers riding entities from boats to pigs to striders
* Built upon covering edge cases to confirm accuracy
* 1.13+ clients on 1.13+ servers, 1.12- clients on 1.13+ servers, 1.13+ clients on 1.12- servers, and 1.12- clients on 1.12- servers are all supported regardless of the large technical changes between these versions.
* The order of collisions depends on the client version and is correct
* Accounts for minor bounding box differences between versions, for example:
    * Single glass panes will be a + shape for 1.7-1.8 players and * for 1.9+ players
    * 1.13+ clients on 1.8 servers see the + glass pane hitbox due to ViaVersion
    * Many other blocks have this extreme attention to detail.
    * Waterlogged blocks do not exist for 1.12 or below players
    * Blocks that do not exist in the client's version use ViaVersion's replacement block
    * Block data that cannot be translated to previous versions is replaced correctly
    * All vanilla collision boxes have been implemented

### Fully asynchronous and multithreaded design

* All movement checks and the overwhelming majority of listeners run on the netty thread
* The anticheat can scale to many hundreds of players, if not more
* Thread safety is carefully thought out
* The next core allows for this design

### Full world replication

* The anticheat keeps a replica of the world for each player
* The replica is created by listening to chunk data packets, block places, and block changes
* On all versions, chunks are compressed to 16-64 kb per chunk using palettes
* Using this cache, the anticheat can safely access the world state
* Per player, the cache allows for multithreaded design
* Sending players fake blocks with packets is safe and does not lead to falses
* The world is recreated for each player to allow lag compensation
* Client sided blocks cause no issues with packet based blocks. Block glitching does not false the anticheat.

### Latency compensation

* World changes are queued until they reach the player
* This means breaking blocks under a player does not false the anticheat
* Everything from flying status to movement speed will be latency compensated

### Inventory compensation

* The player's inventory is tracked to prevent ghost blocks at high latency, and other errors

### Secure by design, not obscurity

* All systems are designed to be highly secure and mathematically impossible to bypass
* For example, the prediction engine knows all possible movements and cannot be bypassed
