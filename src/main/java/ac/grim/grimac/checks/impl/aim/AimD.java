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

    List<Float> deltaXRots = new ArrayList<>();
    List<Float> deltaYRots = new ArrayList<>();

    public AimD(GrimPlayer player) {
        super(player);
    }

    private float xRotBuffer, yRotBuffer;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        float kurtosisYRot = Math.abs(AimUtils.getKurtosis(deltaYRots));
        float kurtosisXRot = Math.abs(AimUtils.getKurtosis(deltaXRots));

        if (deltaXRots.size() > 30) {
            if (kurtosisXRot < 1.5) {
                flagAndAlert();
                deltaXRots.clear();
            }
        }

        if (deltaYRots.size() > 30) {
            if (kurtosisYRot < 1.5) {
                flagAndAlert();
                deltaYRots.clear();
            }
        }
    }
}
