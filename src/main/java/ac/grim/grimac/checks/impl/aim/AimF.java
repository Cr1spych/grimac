package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.AimUtils;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimF")
public class AimF extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimF(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        boolean validSize = deltaXRots.size() >= 10 && deltaYRots.size() >= 10;
        boolean hasAllEqual = AimUtils.hasAllEqual(deltaXRots) || AimUtils.hasAllEqual(deltaYRots);

        if (hasAllEqual && validSize) {
            flagAndAlert();
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() > 10) deltaXRots.remove(0);
        if (deltaYRots.size() > 10) deltaYRots.remove(0);
    }
}
