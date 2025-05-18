package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "InventoryE")
public class InventoryE extends Check implements PacketCheck {
    public InventoryE(GrimPlayer player) {
        super(player);
    }

    private boolean cancelClicks;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isCClickWindow(event)) {
            if (player.packetStateData.isSlowedByUsingItem() && player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot()) {
                fail();
                if (cancelClicks) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.cancelClicks = configManager.getBooleanElse("InventoryE.cancelClicks", true);
    }
}
