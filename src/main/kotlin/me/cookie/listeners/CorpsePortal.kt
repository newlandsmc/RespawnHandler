import me.cookie.RespawnHandler
import me.cookie.traits.CorpseTrait
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPortalEvent
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC

class CorpsePortal(private val plugin: RespawnHandler): Listener {
    @EventHandler
    fun onPortal(event: EntityPortalEvent) {
        if (event.entity.hasMetadata("NPC")) {
            val npc: NPC = CitizensAPI.getNPCRegistry().getNPC(event.entity)
            if (npc.hasTrait(CorpseTrait::class.java)) {
                event.isCancelled = true
            }
        }
    }
}
