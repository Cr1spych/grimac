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

public class AutoClickerI extends Check implements PacketCheck {
    private Deque<Integer> recentCounts = new LinkedList<>();
    private int flyingCount;
    private float lastAnimationPacket;

    public AutoClickerI(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if(packet == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);

            if(digging.getAction() == DiggingAction.RELEASE_USE_ITEM) {
                if (this.flyingCount < 10 && this.lastAnimationPacket + 2000L > System.currentTimeMillis()) {
                    this.recentCounts.add(this.flyingCount);
                    if (this.recentCounts.size() == 100) {
                        double average = 0.0;
                        for (double flyingCount : this.recentCounts) {
                            average += flyingCount;
                        }
                        average /= this.recentCounts.size();
                        double stdDev = 0.0;
                        for (long l : this.recentCounts) {
                            stdDev += Math.pow(l - average, 2.0);
                        }
                        stdDev /= this.recentCounts.size();
                        stdDev = Math.sqrt(stdDev);
                        if (stdDev < 0.2) {
                            if ((vl += 1.4) >= 4.0) {
                                this.flagAndAlert(String.format("STD %.2f. VL %.2f.", stdDev, vl));
                            }
                        }
                        else {
                            vl -= 0.8;
                        }
                        this.violations = vl;
                        this.recentCounts.clear();
                    }
                }

                this.flyingCount = 0;
            }
        } else if (WrapperPlayClientPlayerFlying.isFlying(packet)) {
            ++this.flyingCount;
        }
    }
}
