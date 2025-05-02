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

import java.util.Deque;
import java.util.LinkedList;

public class AutoClickerH extends Check implements PacketCheck {
    private final Deque<Integer> recentCounts = new LinkedList<>();
    private int flyingCount;
    private boolean release;

    public AutoClickerH(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if(packet == PacketType.Play.Client.ANIMATION && !player.isDigging && !player.isPlacing && (System.currentTimeMillis() - player.getLastMovePacketTimestamp()) < 110L) {
            if (this.flyingCount < 10) {
                if (this.release) {
                    this.release = false;
                    this.flyingCount = 0;
                    return;
                }
                this.recentCounts.add(this.flyingCount);
                if (this.recentCounts.size() == 100) {
                    double average = 0.0;
                    for (int i : this.recentCounts) {
                        average += i;
                    }
                    average /= this.recentCounts.size();
                    double stdDev = 0.0;
                    for (int j : this.recentCounts) {
                        stdDev += Math.pow(j - average, 2.0);
                    }
                    stdDev /= this.recentCounts.size();
                    stdDev = Math.sqrt(stdDev);
                    if (stdDev < 0.45) {
                        if ((vl += 1.4) >= 4.0) {
                            this.flagAndAlert(String.format("STD %.2f. VL %.2f.", stdDev, vl));
                        }
                    } else {
                        vl -= 0.8;
                    }
                    this.violations = vl;
                    this.recentCounts.clear();
                }
            }
            this.flyingCount = 0;
        }

        if(WrapperPlayClientPlayerFlying.isFlying(packet)) {
            ++this.flyingCount;
        }

        if(packet == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);

            if(digging.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                this.release = true;
            }
        }
    }
}
