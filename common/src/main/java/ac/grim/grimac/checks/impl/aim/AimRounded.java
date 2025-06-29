package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimRounded")
public class AimRounded extends Check implements RotationCheck {
    public AimRounded(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        float deltaYaw = update.getDeltaYawRotABS();
        float deltaPitch = update.getDeltaPitchRotABS();

        if ((deltaYaw == Math.round(deltaYaw) && deltaYaw != 0) || (deltaPitch == Math.round(deltaPitch)) && deltaPitch != 0) {
            flagAndAlert();
        }
    }
}
