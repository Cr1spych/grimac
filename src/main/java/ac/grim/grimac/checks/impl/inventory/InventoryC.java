package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "InventoryC")
public class InventoryC extends Check implements PacketCheck {
    public InventoryC(GrimPlayer player) {
        super(player);
    }

    private long lastClickWindowTime = -1;
    private double buffer, maxBuffer;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isClickWindow(event)) {
            long now = System.currentTimeMillis();


            if (lastClickWindowTime > 0) {
                long delay = now - lastClickWindowTime;

                if (delay < 3) {
                    buffer++;
                } else {
                    buffer = 0;
                }

                if (buffer > maxBuffer) {
                    flagAndAlert();
                    buffer = 0;
                }
            }

            lastClickWindowTime = now;
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("InventoryC.buffer", 2.0);
    }
}
