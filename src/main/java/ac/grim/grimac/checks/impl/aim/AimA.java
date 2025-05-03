package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.AimUtils;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimA")
public class AimA extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimA(GrimPlayer player) {
        super(player);
    }

    private float lastDeltaXRot, lastDeltaYRot, buffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        boolean validSize = deltaXRots.size() >= 3 && deltaYRots.size() >= 3;
        boolean hasExceeding = AimUtils.hasTooManyExceeding(deltaXRots, 45, 2) && AimUtils.hasTooManyExceeding(deltaYRots, 40, 3) && validSize;

        if (((deltaXRot > 45 && lastDeltaXRot < 2.0) || (deltaYRot > 40 && lastDeltaYRot < 2.0)) && !hasExceeding) {
            buffer++;
        } else {
            buffer = Math.max(0, buffer - 0.02f);
        }
        if (buffer > 1) {
            flagAndAlert();
            buffer = 0.5f;
        }

        lastDeltaXRot = deltaXRot;
        lastDeltaYRot = deltaYRot;

        if (deltaXRots.size() > 3) deltaXRots.remove(0);
        if (deltaYRots.size() > 3) deltaYRots.remove(0);
    }
}
