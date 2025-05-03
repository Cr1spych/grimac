package ac.grim.grimac.checks.impl.aim;

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
    private float buffer;

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
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.0005f);
        }

        if (buffer > 1) {
            flagAndAlert();
            buffer = 1;
            deltaXRots.clear();
            deltaYRots.clear();
        }

        if (deltaXRots.size() > 60) deltaXRots.remove(0);
        if (deltaYRots.size() > 60) deltaYRots.remove(0);
    }
}
