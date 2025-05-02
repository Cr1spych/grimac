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
public class AimC extends Check implements RotationCheck {

    List<Float> deltaXRots = new ArrayList<>();
    List<Float> deltaYRots = new ArrayList<>();

    public AimC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (deltaXRot < 0.45 || deltaYRot < 0.45 || rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        boolean hasDuplicatedXRots = AimUtils.hasDuplicates(deltaXRots, 25) && deltaXRots.size() > 50;
        boolean hasDuplicatedYRots = AimUtils.hasDuplicates(deltaYRots, 25) && deltaYRots.size() > 50;

        if (hasDuplicatedXRots || hasDuplicatedYRots) {
            flagAndAlert();
            deltaYRots.clear();
            deltaXRots.clear();
        }
    }
}
