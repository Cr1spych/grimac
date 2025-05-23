package ac.grim.grimac.checks.impl.killaura;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "KillauraD")
public class KillauraC extends Check implements PacketCheck {
    public KillauraC(GrimPlayer player) {
        super(player);
    }

    private boolean interact;
    private boolean cancelHits;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isCInteract(event)) {
            interact = true;
        }
        if (Packet.isCAttack(event)) {
            if (!interact) {
                fail();
                if (cancelHits) {
                    event.setCancelled(true);
                }
            }
        }
        interact = false;
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.cancelHits = configManager.getBooleanElse("KillauraD.cancelHits", true);
    }
}
