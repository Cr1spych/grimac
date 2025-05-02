package ac.grim.grimac.checks.impl.combat.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AutoClicker F", configName = "Autoclicker")
public class AutoClickerF extends Check implements PacketCheck {

    private final Deque<Integer> recentCounts = new ArrayDeque<>();
    private Vector3i lastBlock;
    private int flyingCount;

    public AutoClickerF(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);

            switch (digging.getAction()) {
                case START_DIGGING -> {
                    if (this.lastBlock != null && this.lastBlock.equals(digging.getBlockPosition())) {
                        this.recentCounts.addLast(this.flyingCount);
                        if (this.recentCounts.size() == 20) {
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
                            if (stdDev < 0.45 && ++vl >= 3.0) {
                                this.flagAndAlert(String.format("STD %.2f. VL %.1f.", stdDev, vl));
                            } else {
                                vl -= 0.5;
                            }
                            this.recentCounts.clear();
                        }
                        this.violations = vl;
                    }
                    this.flyingCount = 0;
                }
                case CANCELLED_DIGGING -> this.lastBlock = digging.getBlockPosition();
            }
        } else if(WrapperPlayClientPlayerFlying.isFlying(packet)) {
            ++this.flyingCount;
        }
    }
}
