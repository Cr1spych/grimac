package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.checks.impl.elytra.ElytraA;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

public class PacketEntityAction extends PacketListenerAbstract {

    public PacketEntityAction() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());

            if (player == null) return;

            switch (action.getAction()) {
                case START_SPRINTING:
                    player.isSprinting = true;
                    break;
                case STOP_SPRINTING:
                    player.isSprinting = false;
                    break;
                case START_SNEAKING:
                    player.isSneaking = true;
                    break;
                case STOP_SNEAKING:
                    player.isSneaking = false;
                    break;
                case START_FLYING_WITH_ELYTRA:
                    if (player.onGround || player.lastOnGround) {
                        player.getSetbackTeleportUtil().executeForceResync();

                        if (player.platformPlayer != null) {
                            // Client ignores sneaking, use it to resync
                            player.platformPlayer.setSneaking(!player.platformPlayer.isSneaking());
                        }

                        event.setCancelled(true);
                        player.onPacketCancel();
                        break;
                    }
                    // Starting fall flying is server sided on 1.14 and below
                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) return;
                    player.checkManager.getPostPredictionCheck(ElytraA.class).onStartGliding(event);

                    // This shouldn't be needed with latency compensated inventories
                    // TODO: Remove this?
                    if (player.canGlide()) {
                        player.isGliding = true;
                        player.pointThreeEstimator.updatePlayerGliding();
                    } else {
                        // A client is flying with a ghost elytra, resync
                        player.getSetbackTeleportUtil().executeForceResync();
                        if (player.platformPlayer != null) {
                            // Client ignores sneaking, use it to resync
                            player.platformPlayer.setSneaking(!player.platformPlayer.isSneaking());
                        }
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                    break;
                case START_JUMPING_WITH_HORSE:
                    int jumpBoost = action.getJumpBoost();
                    if (jumpBoost < 0) jumpBoost = 0;
                    if (jumpBoost >= 90) {
                        player.vehicleData.nextHorseJump = 1;
                    } else {
                        player.vehicleData.nextHorseJump = 0.4F + 0.4F * jumpBoost / 90.0F;
                    }
                    break;
            }
        }
    }
}
