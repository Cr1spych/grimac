package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.millenium.Statistics;
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

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() > 10) deltaXRots.remove(0);
        if (deltaYRots.size() > 10) deltaYRots.remove(0);

        boolean validSize = deltaXRots.size() >= 10 && deltaYRots.size() >= 10;

        if (validSize) {
            float xStDev = (float) Statistics.getStandardDeviation(deltaXRots);
            float yStDev = (float) Statistics.getStandardDeviation(deltaYRots);

            if ((xStDev > 3.6f && yStDev <= 0.0f) || (yStDev > 3.6f && xStDev <= 0.0f)) {
                flagAndAlert();
                deltaXRots.clear();
                deltaYRots.clear();
            }
        }
    }
}
