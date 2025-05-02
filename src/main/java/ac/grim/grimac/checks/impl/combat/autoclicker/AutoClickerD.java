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

@CheckData(name = "AutoClicker D", configName = "Autoclicker")
public class AutoClickerD extends Check implements PacketCheck {

    public AutoClickerD(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();
        double vl = this.violations;

        switch (player.stage) {
            case 0 -> {
                if (packet == PacketType.Play.Client.ANIMATION) {
                    ++player.stage;
                }
            }
            case 1 -> {
                if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
                    WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
                    if(digging.getAction() == DiggingAction.START_DIGGING) {
                        ++player.stage;
                    } else {
                        player.stage = 0;
                    }
                }
            }
            case 2 -> {
                if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
                    WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
                    if(digging.getAction() == DiggingAction.CANCELLED_DIGGING) {
                        if (++vl >= 5) {
                            try {
                                if (player.movementPackets > 10 && (shouldModifyPackets() && flagAndAlert(this.player.movementPackets + "."))) {
                                    event.setCancelled(true);
                                }
                            } finally {
                                player.movementPackets = 0;
                                vl = 0;
                            }
                        }
                        player.stage = 0;
                    }
                } else if (packet == PacketType.Play.Client.ANIMATION) {
                    ++player.stage;
                } else {
                    player.movementPackets = 0;
                    vl = 0;
                    player.stage = 0;
                }
            }

            case 3 -> {
                if (WrapperPlayClientPlayerFlying.isFlying(packet)) {
                    ++player.stage;
                }

                else {
                    this.player.movementPackets = 0;
                    vl = 0;
                    this.player.stage = 0;
                }
            }
            case 4 -> {
                if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
                    WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
                    if(digging.getAction() == DiggingAction.CANCELLED_DIGGING) {
                        ++player.movementPackets;
                    }
                } else {
                    this.player.movementPackets = 0;
                    vl = 0;
                }
                player.stage = 0;
            }
        }

        this.violations = vl;
    }
}
