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
        if (deltaXRot < 0.35 || deltaYRot < 0.35 || rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        boolean validSize = deltaXRots.size() >= 15 && deltaYRots.size() >= 15;

        if (validSize) {
            boolean hasAlternatingPattern = AimUtils.hasAlternatingPattern(deltaXRots) || AimUtils.hasAlternatingPattern(deltaYRots);

            if (hasAlternatingPattern) {
                buffer++;
            } else {
                buffer = Math.max(0, buffer - 0.02f);
            }
            if (buffer > 2) {
                flagAndAlert();
                buffer = 1;
            }
        }

        if (deltaXRots.size() > 15) deltaXRots.remove(0);
        if (deltaYRots.size() > 15) deltaYRots.remove(0);
    }
}
