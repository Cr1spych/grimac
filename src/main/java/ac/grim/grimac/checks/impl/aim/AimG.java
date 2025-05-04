package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.AimUtils;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

// TEST CHECK
@CheckData(name = "AimG")
public class AimG extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimG(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        boolean validSize = deltaXRots.size() >= 12 && deltaYRots.size() >= 12;
        boolean hasRepeatingPattern = AimUtils.hasRepeatingPattern(deltaXRots, 6) || AimUtils.hasRepeatingPattern(deltaYRots, 6);

        if (hasRepeatingPattern && validSize) {
            flagAndAlert();
        }

        if (deltaXRots.size() > 12) deltaXRots.remove(0);
        if (deltaYRots.size() > 12) deltaYRots.remove(0);
    }
}
