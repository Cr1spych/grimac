package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimC")
public class AimC extends Check implements RotationCheck {
    public AimC(GrimPlayer player) {
        super(player);
    }

    private float buffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        if (rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        if ((deltaXRot < 10.0E-5 && deltaXRot != 0.0) || (deltaYRot < 10.0E-5 && deltaYRot != 0)) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.2f);
        }
        if (buffer > 1) {
            flagAndAlert();
            buffer = 0;
        }
    }
}
