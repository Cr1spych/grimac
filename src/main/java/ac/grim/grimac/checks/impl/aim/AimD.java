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

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (deltaXRot < 0.35 || deltaYRot < 0.35 || rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() > 5) {
            float averageXRot = AimUtils.getAverage(deltaXRots);
            debugUtil.debugTo(player, "AverageX: " + averageXRot, true);
            deltaXRots.clear();
        }

        if (deltaYRots.size() > 5) {
            float averageYRot = AimUtils.getAverage(deltaYRots);
            debugUtil.debugTo(player, "AverageY: " + averageYRot, true);
            deltaYRots.clear();
        }
    }
}
