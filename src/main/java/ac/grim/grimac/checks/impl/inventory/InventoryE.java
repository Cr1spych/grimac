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

    private boolean hasInventoryOpen;
    private boolean cancelHits;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isClickWindow(event)) {
            hasInventoryOpen = true;
        }

        if (Packet.isCloseWindow(event)) {
            hasInventoryOpen = false;
        }

        if ((player.packetStateData.isSlowedByUsingItem() && player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot()) && hasInventoryOpen) {
            fail();
            if (cancelHits) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.cancelHits = configManager.getBooleanElse("InventoryE.cancelHits", true);
    }
}
