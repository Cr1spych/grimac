package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimB")
public class AimB extends Check implements RotationCheck {
    public AimB(GrimPlayer player) {
        super(player);
    }

    private float buffer, lastDeltaXRot, lastDeltaYRot, repeats;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (deltaYRot < 0.35 || deltaXRot < 0.35 || rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        boolean repeatingXRot = lastDeltaXRot == deltaXRot;
        boolean repeatingYRot = lastDeltaYRot == deltaYRot;

        if (repeatingXRot || repeatingYRot) {
            repeats++;
        } else {
            repeats = 0;
        }
        if (repeats > 3) {
            if (++buffer > 8) {
                flagAndAlert();
                buffer = 7;
            } else {
                buffer = Math.max(0, buffer - 0.2f);
            }
        }

        lastDeltaXRot = deltaXRot;
        lastDeltaYRot = deltaYRot;
    }
}
