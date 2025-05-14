package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.PatternUtils;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimF", experimental = true)
public class AimF extends Check implements RotationCheck {

    private final List<Float> deltaXRots = new ArrayList<>();
    private final List<Float> deltaYRots = new ArrayList<>();

    public AimF(GrimPlayer player) {
        super(player);
    }

    private boolean matchesY1, matchesX1, matchesX2, matchesY2, matchesX3, matchesY3;
    private float flags;

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float deltaYRot = rotationUpdate.getDeltaYRotABS();
        if (deltaXRot < 0.35 || deltaYRot < 0.35 || player.inVehicle() || rotationUpdate.isCinematic()) {
            return;
        }

        deltaXRots.add(deltaXRot);
        deltaYRots.add(deltaYRot);

        if (deltaXRots.size() >= 30) {
            matchesX1 = PatternUtils.matchesPatternStructure(deltaXRots, 0, 0, 1, 1, 0, 0);
            matchesX2 = PatternUtils.matchesPatternStructure(deltaXRots, 0, 1, 0, 0, 1, 0);
            matchesX3 = PatternUtils.matchesPatternStructure(deltaXRots, 1, 1, 1, 0, 0, 0);
        }

        if (deltaYRots.size() >= 30) {
            matchesY1 = PatternUtils.matchesPatternStructure(deltaYRots, 0, 0, 1, 1, 0, 0);
            matchesY2 = PatternUtils.matchesPatternStructure(deltaYRots, 0, 1, 0, 0, 1, 0);
            matchesY3 = PatternUtils.matchesPatternStructure(deltaYRots, 1, 1, 1, 0, 0, 0);
        }

        if ((matchesX1 || matchesY1) || (matchesX2 || matchesY2)) {
            flags++;
        }

        if (flags > 2) {
            flagAndAlert();
            flags = 0;
        }

        if (deltaXRots.size() > 30) {
            deltaXRots.remove(0);
        }
        if (deltaYRots.size() > 30) {
            deltaYRots.remove(0);
        }
    }
}
