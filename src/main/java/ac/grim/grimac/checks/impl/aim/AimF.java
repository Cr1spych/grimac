package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
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

        float xStDev = player.rotationData.getXRotStDev(10);
        float yStDev = player.rotationData.getYRotStDev(10);

        if ((xStDev > 3.6f && yStDev <= 0.0f) || (yStDev > 3.6f && xStDev <= 0.0f)) {
            flagAndAlert();
        }
    }
}
