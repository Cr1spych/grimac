package ac.grim.grimac.checks.impl.autoclicker;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "AutoclickerA")
public class AutoclickerA extends Check implements PacketCheck {

    private long lastAttackTime = -1, lastDelay = -1;

    private double buffer, maxBuffer;
    private double increaseAmount;
    private double decreaseAmount;
    private boolean cancelHits;

    public AutoclickerA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isAttack(event)) {
            long now = System.currentTimeMillis();

            if (lastAttackTime != -1) {
                long delay = now - lastAttackTime;

                if (delay == lastDelay && delay > 300) {
                    buffer += increaseAmount;
                } else {
                    buffer = Math.max(0, buffer - decreaseAmount);
                }

                if (buffer > maxBuffer) {
                    fail();
                    buffer = maxBuffer - 1.0;
                    if (cancelHits) {
                        event.setCancelled(true);
                    }
                }

                lastDelay = delay;
            }

            lastAttackTime = now;
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("AutoclickerA.maxBuffer", 3.0);
        this.increaseAmount = configManager.getDoubleElse("AutoclickerA.increaseBuffer", 1.5);
        this.decreaseAmount = configManager.getDoubleElse("AutoclickerA.decreaseBuffer", 1.0);
        this.cancelHits = configManager.getBooleanElse("AutoclickerA.cancelHits", true);
    }
}
