package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimA")
public class AimA extends Check implements RotationCheck {

    private double buffer, maxBuffer;

    private float lastDeltaXRot, lastDeltaYRot, xRotExceeding, yRotExceeding;

    public AimA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        if (player.inVehicle()) {
            return;
        }

        if (deltaXRot > 35) xRotExceeding++; else xRotExceeding = 0;
        if (deltaYRot > 30) yRotExceeding++; else yRotExceeding = 0;

        boolean hasExceeding = xRotExceeding > 3 || yRotExceeding > 3;

        if (((deltaXRot > 50 && lastDeltaXRot < 5.1) || (deltaYRot > 45 && lastDeltaYRot < 5.1)) && !hasExceeding && player.actionManager.hasAttackedSince(90)) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.03f);
        }

        if (buffer > maxBuffer) {
            fail();
            buffer = 0;
        }

        lastDeltaXRot = deltaXRot;
        lastDeltaYRot = deltaYRot;
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("AimA.buffer", 1.0);
    }
}
