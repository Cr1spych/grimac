package ac.grim.grimac.checks.impl.combat.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "AutoClicker E", configName = "Autoclicker")
public class AutoClickerE extends Check implements PacketCheck {

    private boolean failed;
    private boolean sent;
    private int count;

    public AutoClickerE(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if(packet == PacketType.Play.Client.ANIMATION) {
            if (!player.isDigging() && !player.isPlacing() && (System.currentTimeMillis() - player.getLastMovePacketTimestamp()) < 110L) {
                if(this.sent) {
                    ++this.count;
                    if(!this.failed) {
                        if(++vl >= 5) {
                            this.flagAndAlert(this.count + "");
                            vl = 0;
                        }

                        this.violations = vl;
                    }
                } else {
                    this.sent = true;
                    this.count = 0;
                }
            }
        } else if(WrapperPlayClientPlayerFlying.isFlying(packet)) {
            this.failed = false;
            this.sent = false;
            this.count = 0;
        }
    }
}
