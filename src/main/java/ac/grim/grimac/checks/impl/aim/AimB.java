package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.millenium.Statistics;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimB")
public class AimB extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimB(GrimPlayer player) {
        super(player);
    }

    private float xStDev, yStDev;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRot();
        float deltaYRot = rotationUpdate.getDeltaYRot();
        float pitch = Math.abs(rotationUpdate.getTo().getPitch());
        if (player.inVehicle() || rotationUpdate.isCinematic()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() >= 10) {
            xStDev = (float) Statistics.getStandardDeviation(deltaXRots);
        }

        if (deltaYRots.size() >= 10) {
            yStDev = (float) Statistics.getStandardDeviation(deltaYRots);
        }

        if (((xStDev > 3.6f && yStDev <= 0.0f) || (yStDev > 3.6f && xStDev <= 0.0f) && pitch < 85)) {
            fail();
        }

        if (deltaXRots.size() > 10) {
            deltaXRots.remove(0);
        }
        if (deltaYRots.size() > 10) {
            deltaYRots.remove(0);
        }
    }
}
