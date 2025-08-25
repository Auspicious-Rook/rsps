package tools

import io.netty.buffer.ByteBufAllocator
import java.nio.file.Files
import java.nio.file.Paths
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.proxy.worlds.World
import net.rsprox.proxy.worlds.WorldList

public fun main() {
    // Define proxy target configuration
    val config: ProxyTargetConfig = ProxyTargetConfig(
        id = 0,
        name = "LocalProxy",
        javConfigUrl = "http://oldschool.runescape.com/jav_config.ws",
        modulus = null,
        varpCount = 0,
        revision = "230",
        runeliteBootstrapCommitHash = null,
        runeliteGamepackUrl = null
    )

    // Define first world
    val localWorld: World = World(
        proxyTargetConfig = config,
        id = 255,
        properties = 0, // Set flags like members, PvP, etc. here
        population = 1337,
        location = 0,
        host = "10.0.0.31", // Your LAN IP or server host
        activity = "Local Dev World"
    )

    // Define second world (optional)
    val exampleWorld: World = World(
        proxyTargetConfig = config,
        id = 301,
        properties = 0,
        population = 999,
        location = 0,
        host = "10.0.0.31",
        activity = "Example World"
    )

    // Create world list and encode it
    val worldList: WorldList = WorldList(listOf(localWorld, exampleWorld))
    val encoded = worldList.encode(ByteBufAllocator.DEFAULT)

    // Read encoded buffer into byte array
    val bytes = ByteArray(encoded.readableBytes())
    encoded.buffer.readBytes(bytes)

    // Write to file
    Files.write(Paths.get("world_list.ws"), bytes)

    println("✅ Successfully wrote world_list.ws with ${worldList.size} world(s).")
}
