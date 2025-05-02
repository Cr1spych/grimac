package ac.grim.grimac.checks.impl.combat.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public class AutoClickerL extends Check implements PacketCheck {
    private int movements;
    private int failed;
    private int passed;
    private int stage;

    public AutoClickerL(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if(System.currentTimeMillis() - this.player.lastMovePacketTimestamp < 110L) {
            if (packet == PacketType.Play.Client.ANIMATION) {
                if (this.stage == 0 || this.stage == 1) {
                    ++this.stage;
                }
                else {
                    this.stage = 1;
                }
            } else if(WrapperPlayClientPlayerFlying.isFlying(packet)) {
                if (this.stage == 2) {
                    ++this.stage;
                }
                else {
                    this.stage = 0;
                }
                ++this.movements;
            } else if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
                WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
                if(digging.getAction() == DiggingAction.CANCELLED_DIGGING) {
                if (this.stage == 3) {
                    ++this.failed;
                }
                else {
                    ++this.passed;
                }
                if (this.movements >= 200 && this.failed + this.passed > 60) {
                    double rat = (this.passed == 0) ? -1.0 : ((double) this.failed / this.passed);
                    if (rat > 2.5) {
                        if ((vl += 1.0 + (rat - 2.0) * 0.75) >= 4.0) {
                            this.flagAndAlert(String.format("RAT %.2f. VL %.2f.", rat, vl));
                        }
                    }
                    else {
                        vl -= 2.0;
                    }
                    this.violations = vl;
                    this.movements = 0;
                    this.passed = 0;
                    this.failed = 0;
                }
            }
        } else {
             this.stage = 0;
            }
        }
    }
}
