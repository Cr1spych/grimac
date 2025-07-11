package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.data.RotationData;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Location;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerVehicleMove;

public class PacketServerTeleport extends PacketListenerAbstract {

    public PacketServerTeleport() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook teleport = new WrapperPlayServerPlayerPositionAndLook(event);

            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());

            Vector3d pos = new Vector3d(teleport.getX(), teleport.getY(), teleport.getZ());

            if (player == null) return;

            // This is the first packet sent to the client which we need to track
            if (player.getSetbackTeleportUtil().getRequiredSetBack() == null) {
                // Player teleport event gets called AFTER player join event
                player.x = teleport.getX();
                player.y = teleport.getY();
                player.z = teleport.getZ();
                player.xRot = teleport.getYaw();
                player.yRot = teleport.getPitch();

                player.lastX = teleport.getX();
                player.lastY = teleport.getY();
                player.lastZ = teleport.getZ();
                player.lastXRot = teleport.getYaw();
                player.lastYRot = teleport.getPitch();

                player.pollData();
            }

            // Convert relative teleports to normal teleports
            // We have to do this because 1.8 players on 1.9+ get teleports changed by ViaVersion
            // Additionally, velocity is kept after relative teleports making predictions difficult
            // The added complexity isn't worth a feature that I have never seen used
            //
            // If you do actually need this make an issue on GitHub with an explanation for why
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                if (teleport.isRelativeFlag(RelativeFlag.X)) {
                    pos = pos.add(new Vector3d(player.x, 0, 0));
                    teleport.setRelative(RelativeFlag.X, false);
                }

                if (teleport.isRelativeFlag(RelativeFlag.Y)) {
                    pos = pos.add(new Vector3d(0, player.y, 0));
                    teleport.setRelative(RelativeFlag.Y, false);
                }

                if (teleport.isRelativeFlag(RelativeFlag.Z)) {
                    pos = pos.add(new Vector3d(0, 0, player.z));
                    teleport.setRelative(RelativeFlag.Z, false);
                }

                teleport.setX(pos.getX());
                teleport.setY(pos.getY());
                teleport.setZ(pos.getZ());
            }

            player.sendTransaction();
            final int lastTransactionSent = player.lastTransactionSent.get();
            event.getTasksAfterSend().add(player::sendTransaction);

            if (teleport.isDismountVehicle()) {
                // Remove player from vehicle
                event.getTasksAfterSend().add(() -> player.compensatedEntities.self.eject());
            }

            // For some reason teleports on 1.7 servers are offset by 1.62?
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_8))
                pos = pos.withY(pos.getY() - 1.62);

            Location target = new Location(null, pos.getX(), pos.getY(), pos.getZ());
            player.getSetbackTeleportUtil().addSentTeleport(target, teleport.getDeltaMovement(), lastTransactionSent, teleport.getRelativeFlags(), true, teleport.getTeleportId());
        }

        if (event.getPacketType() == PacketType.Play.Server.PLAYER_ROTATION) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayServerPlayerRotation packet = new WrapperPlayServerPlayerRotation(event);

            // I don't want to deal with this, so we'll prevent it
            if (!Float.isFinite(packet.getPitch())) {
                packet.setPitch(0);
                event.markForReEncode(true);
            }
            if (!Float.isFinite(packet.getYaw())) {
                packet.setYaw(0);
                event.markForReEncode(true);
            }

            player.sendTransaction();
            player.pendingRotations.add(new RotationData(packet.getYaw(), GrimMath.clamp(packet.getPitch() % 360F, -90F, 90F), player.getLastTransactionSent()));
            event.getTasksAfterSend().add(player::sendTransaction);
        }

        if (event.getPacketType() == PacketType.Play.Server.VEHICLE_MOVE) {
            WrapperPlayServerVehicleMove vehicleMove = new WrapperPlayServerVehicleMove(event);

            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            player.sendTransaction();
            int lastTransactionSent = player.lastTransactionSent.get();
            Vector3d finalPos = vehicleMove.getPosition();

            event.getTasksAfterSend().add(player::sendTransaction);
            player.vehicleData.vehicleTeleports.add(new Pair<>(lastTransactionSent, finalPos));
        }
    }
}
