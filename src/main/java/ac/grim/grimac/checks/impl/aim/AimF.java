package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.millenium.Statistics;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimF")
public class AimF extends Check implements RotationCheck {
    public AimF(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        float deltaXRot = rotationUpdate.getDeltaXRotABS();
        float xRotGCD = Statistics.getGCD(deltaXRot);

        debugUtil.debugTo(player, "XRotGCD=" + xRotGCD, true);
        debugUtil.debugTo(player, "DeltaXRot=" + deltaXRot, true);
    }
}
