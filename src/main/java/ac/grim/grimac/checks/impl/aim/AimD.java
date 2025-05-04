package ac.grim.grimac.checks.impl.aim;

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

    private float buffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        boolean validSize = deltaXRots.size() >= 10 && deltaYRots.size() >= 10;
        boolean hasAlternatingPattern = AimUtils.hasAlternatingPattern(deltaXRots) || AimUtils.hasAlternatingPattern(deltaYRots);

        if (hasAlternatingPattern && validSize) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.03f);
        }
        if (buffer > 2) {
            flagAndAlert();
            buffer = 1;
        }

        if (deltaXRots.size() > 10) deltaXRots.remove(0);
        if (deltaYRots.size() > 10) deltaYRots.remove(0);
    }
}
