package me.cookie.data

import me.cookie.RespawnHandler
import me.cookie.cookiecore.data.Values
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

class Corpses(plugin: RespawnHandler) {
    init {
        pluginInstance = plugin
    }
}

private lateinit var pluginInstance: RespawnHandler
fun saveCorpse(corpse: Corpse) {
    val cause: String = escapeStringForSql(corpse.cause)
    val inventory: String = escapeStringForSql(corpse.inventoryBase64)

    pluginInstance.corpses.insertIntoTable(
        "corpses",
        listOf(
            "id", "UUID", "x", "y", "z", "world", "cause", "inventory", "timestamp", "claimed", "items",
            "expired", "claimedByUUID"
        ),
        Values(
            corpse.id,
            corpse.uuid,
            corpse.x,
            corpse.y,
            corpse.z,
            corpse.world,
            //corpse.cause,
            cause,
            //corpse.inventoryBase64,
            inventory,
            corpse.timestamp,
            corpse.claimed,
            corpse.items,
            corpse.expired,
            corpse.claimedByUUID
        )
    )
    //pluginInstance.corpses.updateColumnsWhere(
    //    "corpses",
    //    listOf("")
    //)
}

fun escapeStringForSql(input: String): String {
    return input.replace("'", "''")
}
fun unescapeStringForSql(input: String): String {
    return input.replace("''", "'")
}

fun getNextCorpseId(uuid: UUID): Int {
    val connection: Connection = getCorpsesConnection()
    val result = connection.prepareStatement("SELECT id FROM corpses WHERE UUID = '$uuid' ORDER BY id DESC LIMIT 1")
        .executeQuery()
    if (result.next()) {
        return result.getInt("id") + 1
    }
    return 1
}

fun getCorpsesConnection(): Connection {
    val connectionField = pluginInstance.corpses.javaClass.getDeclaredField("connection")
    connectionField.isAccessible = true
    return connectionField.get(pluginInstance.corpses) as Connection
}

fun getCorpse(uuid: UUID, id: Int): Corpse {
    val connection: Connection = getCorpsesConnection()
    val result = connection.prepareStatement("SELECT * FROM corpses WHERE UUID = '$uuid' AND id = '$id'").executeQuery()
    if (result.next()) {
        return buildCorpseFromResult(result)
    }
    return null!!
}

fun setCorpseClaimed(uuid: UUID, id: Int, claimed: Boolean, claimedByWho: UUID) {
    //val connection: Connection = getCorpsesConnection()
    //connection.prepareStatement("UPDATE corpses SET claimed = '$claimed' WHERE UUID = '$uuid' AND id = '$id'").executeUpdate()
    pluginInstance.corpses.updateColumnsWhere(
        "corpses",
        listOf("claimed", "claimedByUUID"),
        "UUID = '$uuid' AND id = '$id'",
        Values(claimed, claimedByWho.toString())
    )
}

fun setCorpseExpired(uuid: UUID, id: Int) {
    //val connection: Connection = getCorpsesConnection()
    //connection.prepareStatement("UPDATE corpses SET expired = 'true' WHERE UUID = '$uuid' AND id = '$id'").executeUpdate()
    pluginInstance.corpses.updateColumnsWhere(
        "corpses",
        listOf("expired"),
        "UUID = '$uuid' AND id = '$id'",
        Values(true)
    )
}

fun buildCorpseFromResult(result: ResultSet): Corpse {
    return Corpse(
        result.getInt("id"),
        result.getString("UUID"),
        result.getInt("x"), result.getInt("y"),
        result.getInt("z"), result.getString("world"),
        //result.getString("cause"), result.getString("inventory"),
        unescapeStringForSql(result.getString("cause")),
        unescapeStringForSql(result.getString("inventory")),
        result.getLong("timestamp"), result.getBoolean("claimed"),
        result.getInt("items"),
        result.getBoolean("expired"),
        result.getString("claimedByUUID"),
    )
}

data class Corpse(
    val id: Int,
    val uuid: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val world: String,
    val cause: String,
    val inventoryBase64: String,
    val timestamp: Long,
    val claimed: Boolean,
    val items: Int,
    val expired: Boolean,
    val claimedByUUID: String
)
