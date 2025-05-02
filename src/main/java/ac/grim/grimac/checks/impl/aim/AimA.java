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

    private float lastDeltaXRot, lastDeltaYRot, buffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        if ((deltaXRot > 55 && lastDeltaXRot < 1) || (deltaYRot > 55 && lastDeltaYRot < 1.0)) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.05f);
        }
        if (buffer > 2) {
            flagAndAlert();
            buffer = 0.5f;
        }

        lastDeltaXRot = deltaXRot;
        lastDeltaYRot = deltaYRot;
    }
}
