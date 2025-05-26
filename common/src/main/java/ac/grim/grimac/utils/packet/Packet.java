package ac.grim.grimac.utils.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public final class Packet {

    private Packet() {}

    public static boolean isCAttack(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            return packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
        }
        return false;
    }

    public static boolean isCInteract(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY;
    }

    public static boolean isCUseItem(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.USE_ITEM;
    }

    public static boolean isCRotation(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION;
    }

    public static boolean isCPositionAndRotation(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION;
    }

    public static boolean isCPosition(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION;
    }

    public static boolean isCClickWindow(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW;
    }

    public static boolean isCCloseWindow(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW;
    }

    public static boolean isCBlockDigging(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING;
    }

    public static boolean isCBlockBreak(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            return packet.getAction() == DiggingAction.FINISHED_DIGGING;
        }
        return false;
    }

    public static boolean isCInput(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT;
    }

    public static boolean isCAnimation(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.ANIMATION;
    }

    public static boolean isCBlockPlace(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT;
    }

    public static boolean isCStartSprinting(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            return packet.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING;
        }
        return false;
    }

    public static boolean isCStopSprinting(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            return packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING;
        }
        return false;
    }

    public static boolean isCStartSneaking(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            return packet.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING;
        }
        return false;
    }

    public static boolean isCStopSneaking(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            return packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING;
        }
        return false;
    }

    public static boolean isCFlying(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING;
    }

    public static boolean isCVehicleMove(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE;
    }

    public static boolean isSMetadata(PacketSendEvent event) {
        return event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA;
    }
}
