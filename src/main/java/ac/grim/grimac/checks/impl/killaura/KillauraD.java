package ac.grim.grimac.checks.impl.killaura;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@CheckData(name = "KillauraD")
public class KillauraD extends Check implements PacketCheck {
    public KillauraD(GrimPlayer player) {
        super(player);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        if (!player.bukkitPlayer.canSee(target)) {
            flagAndAlert();
        }
    }
}
