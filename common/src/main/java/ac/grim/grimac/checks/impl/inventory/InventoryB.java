package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

@CheckData(name = "InventoryB")
public class InventoryB extends Check implements PacketCheck {
    public InventoryB(GrimPlayer player) {
        super(player);
    }

    private long lastClickWindowTime = -1;
    private int clickDelay;
    private double buffer, maxBuffer;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isCClickWindow(event)) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            if (wrapper.getWindowClickType() == WrapperPlayClientClickWindow.WindowClickType.QUICK_MOVE) {
                long now = System.currentTimeMillis();

                if (lastClickWindowTime > 0) {
                    long delay = now - lastClickWindowTime;

                    if (delay < clickDelay) {
                        buffer++;
                    } else {
                        buffer = 0;
                    }

                    if (buffer > maxBuffer) {
                        buffer = 0;
                        flagAndAlert();
                    }
                }

                lastClickWindowTime = now;
            }
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("InventoryB.buffer", 2.0);
        this.clickDelay = configManager.getIntElse("InventoryB.clickDelay", 20);
    }
}
