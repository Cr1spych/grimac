package ac.grim.grimac.checks.impl.combat.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(name = "AutoClicker C", configName = "Autoclicker")
public class AutoClickerC extends Check implements PacketCheck {

    private boolean sent = false;

    public AutoClickerC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);

            switch (digging.getAction()) {
                case START_DIGGING -> this.sent = true;
                case CANCELLED_DIGGING -> {
                    if (this.sent) {
                        if (++vl > 10 && shouldModifyPackets() && this.flagAndAlert(vl + "") && vl >= 20) {
                            event.setCancelled(true);
                        }
                    } else {
                        vl = 0;
                    }
                    this.violations = vl;
                }
            }

        } else if (packet == PacketType.Play.Client.ANIMATION) {
            this.sent = false;
        }
    }
}
