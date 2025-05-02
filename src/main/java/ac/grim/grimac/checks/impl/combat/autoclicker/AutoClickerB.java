package ac.grim.grimac.checks.impl.combat.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "AutoClicker B", configName = "Autoclicker")
public class AutoClickerB extends Check implements PacketCheck {

    public AutoClickerB(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if(packet == PacketType.Play.Client.ANIMATION && !player.isDigging() && !player.isPlacing() && System.currentTimeMillis() - player.lastMovePacketTimestamp < 100L) {

            if(player.flyingCount < 10) {
                if(player.release) {
                    player.release = false;
                    player.flyingCount = 0;
                    return;
                }

                if (player.flyingCount > 3) {
                    ++player.outliers;
                } else if (player.flyingCount == 0) {
                    return;
                }

                if (++player.clicks == 1000) {
                    if (player.outliers <= 7 && ((vl += 1.4) >= 4.0)) {
                        if (shouldModifyPackets() && this.flagAndAlert(player.outliers + "")) {
                            event.setCancelled(true);
                        }
                    } else {
                        vl -= 0.8;
                    }

                    this.violations = vl;
                    player.outliers = 0;
                    player.clicks = 0;
                }
            }

            this.player.flyingCount = 0;
        }

        if(WrapperPlayClientPlayerFlying.isFlying(packet)) {
            ++this.player.flyingCount;
        } else if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);

            if(digging.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                this.player.release = true;
            }
        }

    }
}
