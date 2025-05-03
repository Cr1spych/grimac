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

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();

        if (rotationUpdate.isCinematic() || player.inVehicle()) return;

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() > 30 && deltaYRots.size() > 30) {
            float kurtosisX = Math.abs(AimUtils.getKurtosis(deltaXRots));
            float kurtosisY = Math.abs(AimUtils.getKurtosis(deltaYRots));

            if ((kurtosisX < 1.5 && kurtosisX != 0.0) && (kurtosisY < 1.5 && kurtosisY != 0.0)) {
                flagAndAlert();
            }

            deltaXRots.clear();
            deltaYRots.clear();
        }
    }
}
