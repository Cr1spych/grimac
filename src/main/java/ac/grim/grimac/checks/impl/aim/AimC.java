package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.AimUtils;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimC")
public class AimC extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimC(GrimPlayer player) {
        super(player);
    }

    private double buffer, maxBuffer;
    private boolean duplicatedX, duplicatedY;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (deltaXRot < 0.45 || deltaYRot < 0.45 || player.inVehicle() || player.getVerticalSensitivity() > 0.68 || player.getHorizontalSensitivity() > 0.68) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() >= 50) {
            duplicatedX = AimUtils.hasDuplicates(deltaXRots, 30);
        }

        if (deltaYRots.size() >= 50) {
            duplicatedY = AimUtils.hasDuplicates(deltaYRots, 30);
        }

        if (duplicatedX || duplicatedY) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.0005f);
        }

        if (buffer > maxBuffer) {
            buffer = 0;
            fail();
        }

        if (deltaXRots.size() > 50) {
            deltaXRots.remove(0);
        }
        if (deltaYRots.size() > 50) {
            deltaYRots.remove(0);
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("AimC.buffer", 1.0);
    }
}
