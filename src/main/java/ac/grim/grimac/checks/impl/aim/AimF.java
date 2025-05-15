package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimG")
public class AimF extends Check implements RotationCheck {
    public AimF(GrimPlayer player) {
        super(player);
    }
    private float buffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (player.inVehicle() || rotationUpdate.isCinematic()) {
            return;
        }

        if (deltaXRot < 1.0E-6 && deltaYRot < 1.0E-6) {
            buffer++;
        } else {
            buffer = 0;
        }
        if (buffer > 4) {
            fail();
            buffer = 0;
        }
    }
}
