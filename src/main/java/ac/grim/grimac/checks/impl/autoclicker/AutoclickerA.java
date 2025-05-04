package ac.grim.grimac.checks.impl.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "AutoclickerA")
public class AutoclickerA extends Check implements PacketCheck {

    private long lastAttackTime = -1;
    private long lastDelay = -1;
    private float buffer = 0;

    public AutoclickerA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                long now = System.currentTimeMillis();

                if (lastAttackTime != -1) {
                    long delay = now - lastAttackTime;

                    if (delay == lastDelay && delay > 0) {
                        buffer++;
                    } else {
                        buffer = Math.max(0, buffer - 0.4f);
                    }

                    if (buffer > 3) {
                        buffer = 2;
                    }

                    lastDelay = delay;
                }

                lastAttackTime = now;
            }
        }
    }
}
