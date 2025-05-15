package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "InventoryB")
public class InventoryB extends Check implements PacketCheck {
    public InventoryB(GrimPlayer player) {
        super(player);
    }

    private boolean cancelClicks;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isClickWindow(event)) {
            if (player.isSprinting) {
                fail();
                if (cancelClicks) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.cancelClicks = configManager.getBooleanElse("InventoryB.cancelClicks", true);
    }
}
