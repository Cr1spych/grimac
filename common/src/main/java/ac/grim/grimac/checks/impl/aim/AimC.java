package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.api.config.ConfigManager;
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

    private double buffer, maxBuffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (player.inVehicle()) {
            return;
        }

        boolean isXRounded = deltaXRot == 1.0 || deltaXRot == 2.0 || deltaXRot == 3.0 || deltaXRot == 4.0 || deltaXRot == 5.0;
        boolean isYRounded = deltaYRot == 1.0 || deltaYRot == 2.0 || deltaYRot == 3.0 || deltaYRot == 4.0 || deltaYRot == 5.0;

        if (isYRounded || isXRounded) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.05f);
        }
        if (buffer > maxBuffer) {
            flagAndAlert();
            buffer = 0;
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("AimE.buffer", 2.0);
    }
}
