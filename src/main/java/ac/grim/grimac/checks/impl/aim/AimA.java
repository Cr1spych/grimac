package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimA")
public class AimA extends Check implements RotationCheck {

    public AimA(GrimPlayer player) {
        super(player);
    }

    private float lastDeltaXRot, lastDeltaYRot, xRotExceeding, yRotExceeding;
    private float buffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        if (deltaXRot > 35) xRotExceeding++; else xRotExceeding = 0;
        if (deltaYRot > 30) yRotExceeding++; else yRotExceeding = 0;

        boolean hasExceeding = xRotExceeding > 2 || yRotExceeding > 2;

        if (((deltaXRot > 50 && lastDeltaXRot < 3.5) || (deltaYRot > 45 && lastDeltaYRot < 3.5)) && !hasExceeding && player.actionManager.hasAttackedSince(80)) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.04f);
        }
        if (buffer > 1) {
            flagAndAlert();
            buffer = 0.5f;
        }

        lastDeltaXRot = deltaXRot;
        lastDeltaYRot = deltaYRot;
    }
}
