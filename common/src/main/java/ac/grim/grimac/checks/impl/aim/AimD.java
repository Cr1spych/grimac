package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimD")
public class AimD extends Check implements RotationCheck {
    public AimD(GrimPlayer player) {
        super(player);
    }

    private boolean ignoreCinematicMode;

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (player.inVehicle() || rotationUpdate.isCinematic() && ignoreCinematicMode) {
            return;
        }
        if (rotationUpdate.getDeltaXRotABS() < 1.0E-6 && rotationUpdate.getDeltaYRotABS() < 1.0E-6) {
            flagAndAlert();
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        ignoreCinematicMode = configManager.getBooleanElse("AimD.ignoreCinematicMode", false);
    }
}
