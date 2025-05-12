package ac.grim.grimac.checks.type;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.utils.anticheat.update.PositionUpdate;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;

public interface MultiCheck extends AbstractCheck {

    default void process(final RotationUpdate rotationUpdate) {}

    default void onPacketReceive(final PacketReceiveEvent event) {}
    default void onPacketSend(final PacketSendEvent event) {}

    default void onPositionUpdate(final PositionUpdate positionUpdate) {}
}
