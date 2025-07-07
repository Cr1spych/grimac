package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimSnap")
public class AimSnap extends Check implements RotationCheck {
    public AimSnap(GrimPlayer player) {
        super(player);
    }

    private float lastDeltaYaw, lastDeltaPitch;

    @Override
    public void process(RotationUpdate update) {
        float deltaYaw = update.getDeltaYawRotABS();
        float deltaPitch = update.getDeltaPitchRotABS();

        // wtf??? 🤢
        if (deltaYaw > 50 && lastDeltaYaw < 5) {
            flagAndAlert();
        }

        // wtf??? 🤢
        if (deltaPitch > 50 && lastDeltaPitch < 5) {
            flagAndAlert();
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }
}
