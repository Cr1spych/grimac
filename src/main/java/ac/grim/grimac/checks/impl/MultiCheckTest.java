package ac.grim.grimac.checks.impl;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.MultiCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PositionUpdate;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;

@CheckData(name = "Test")
public class MultiCheckTest extends Check implements MultiCheck {
    public MultiCheckTest(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {

    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

    }

    @Override
    public void onPacketSend(PacketSendEvent event) {

    }

    @Override
    public void onPositionUpdate(PositionUpdate positionUpdate) {

    }
}
