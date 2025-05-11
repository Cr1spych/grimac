package ac.grim.grimac.utils.packet;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

public final class Packet {

    private Packet() {}

    public static boolean isAttack(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            return packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
        }
        return false;
    }

    public static boolean isInteract(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY;
    }

    public static boolean isUseItem(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.USE_ITEM;
    }

    public static boolean isRotation(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION;
    }

    public static boolean isPositionAndRotation(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION;
    }

    public static boolean isPosition(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION;
    }

    public static boolean isClickWindow(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW;
    }

    public static boolean isCloseWindow(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW;
    }

    public static boolean isBlockDigging(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING;
    }

    public static boolean isBlockBreak(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            return packet.getAction() == DiggingAction.FINISHED_DIGGING;
        }
        return false;
    }

    public static boolean isInput(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT;
    }

    public static boolean isAnimation(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.ANIMATION;
    }

    public static boolean isBlockPlace(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT;
    }
}
