package ac.grim.grimac.utils.data;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.impl.aim.utils.millenium.Statistics;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RotationData extends Check implements RotationCheck {

    private final List<Float> deltaXRotsStDev = new ArrayList<>();
    private final List<Float> deltaYRotsStDev = new ArrayList<>();

    public RotationData(GrimPlayer player) {
        super(player);
    }

    private float deltaXRot, deltaYRot;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        deltaXRot = rotationUpdate.getDeltaXRotABS();
        deltaYRot = rotationUpdate.getDeltaYRotABS();

        deltaXRotsStDev.add(deltaXRot);
        deltaYRotsStDev.add(deltaYRot);
    }

    public float getXRotStDev(int listSize) {
        if (deltaXRotsStDev.size() > listSize) {
            deltaXRotsStDev.remove(0);
        }
        return (float) Statistics.getStandardDeviation(deltaXRotsStDev);
    }

    public float getYRotStDev(int listSize) {
        if (deltaYRotsStDev.size() > listSize) {
            deltaYRotsStDev.remove(0);
        }
        return (float) Statistics.getStandardDeviation(deltaYRotsStDev);
    }
}
