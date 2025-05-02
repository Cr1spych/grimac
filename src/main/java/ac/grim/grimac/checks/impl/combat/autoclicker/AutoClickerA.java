package ac.grim.grimac.checks.impl.combat.autoclicker;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "AutoClicker A", configName = "Autoclicker")
public class AutoClickerA extends Check implements PacketCheck {

    public AutoClickerA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        PacketTypeCommon packet = event.getPacketType();

        if (packet == PacketType.Play.Client.ANIMATION) {
            if (!player.isDigging() && !player.isPlacing() && (System.currentTimeMillis() - player.getLastMovePacketTimestamp()) < 110L) {
                player.swings++;
            }
        }

        if(WrapperPlayClientPlayerFlying.isFlying(packet) && ++player.movementPackets == 20) {
            if(player.isPlacing()) player.isPlacing = false;

            if (player.swings > 20 && this.flagAndAlert(player.swings + "")) {
                if (shouldModifyPackets()){
                    event.setCancelled(true);
                }
            }

            player.setLastCps(player.swings);

            player.setMovementPackets(0);
            player.setSwings(0);
        }

        if (packet == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);

            switch (digging.getAction()) {
                case START_DIGGING -> player.isDigging = true;
                case FINISHED_DIGGING, CANCELLED_DIGGING -> player.isDigging = false;
            }
        } else if (packet== PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            player.isPlacing = true;
        }


        // Update last move packet timestamp for other checks
        if (packet == PacketType.Play.Client.PLAYER_POSITION
                || packet == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                || packet == PacketType.Play.Client.PLAYER_ROTATION
                || packet == PacketType.Play.Client.PLAYER_FLYING) {
            player.lastMovePacketTimestamp = System.currentTimeMillis();
        }
    }
}
