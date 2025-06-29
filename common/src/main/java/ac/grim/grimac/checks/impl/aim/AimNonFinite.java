package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

public class AimNonFinite extends Check implements RotationCheck {
    public AimNonFinite(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        if (isNonFinite(update.getDeltaYawRot()) || isNonFinite(update.getDeltaPitchRot())) {
            flagAndAlert();
        }
    }

    private boolean isNonFinite(float a) {
        return Float.isNaN(a) || Float.isInfinite(a);
    }
}
