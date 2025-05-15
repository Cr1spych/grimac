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

@CheckData(name = "AimD")
public class AimD extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimD(GrimPlayer player) {
        super(player);
    }

    private double buffer, maxBuffer;
    private boolean alternatingX, alternatingY;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (deltaXRot < 0.35 || deltaYRot < 0.35 || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() >= 25) {
            alternatingX = AimUtils.hasAlternatingPattern(deltaXRots);
        }

        if (deltaYRots.size() >= 25) {
            alternatingY = AimUtils.hasAlternatingPattern(deltaYRots);
        }

        if (alternatingX || alternatingY) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.02f);
        }
        if (buffer > maxBuffer) {
            fail();
            buffer = 0;
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
        this.maxBuffer = configManager.getDoubleElse("AimD.buffer", 2.0);
    }
}
