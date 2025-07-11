package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "DuplicateRotPlace", experimental = true)
public class DuplicateRotPlace extends BlockPlaceCheck {

    private float deltaX, deltaY;
    private float lastPlacedDeltaX;
    private double lastPlacedDeltaDotsX;
    private double deltaDotsX;
    private boolean rotated = false;

    public DuplicateRotPlace(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        deltaX = rotationUpdate.getDeltaYawRotABS();
        deltaY = rotationUpdate.getDeltaPitchRotABS();
        deltaDotsX = rotationUpdate.getProcessor().deltaDotsX;
        rotated = true;
    }

    @Override
    public void onPostFlyingBlockPlace(BlockPlace place) {
        if (rotated && !player.inVehicle()) {
            if (deltaX > 2) {
                float xDiff = Math.abs(deltaX - lastPlacedDeltaX);
                double xDiffDots = Math.abs(deltaDotsX - lastPlacedDeltaDotsX);

                if (xDiff < 0.0001) {
                    flagAndAlert("x=" + xDiff + " xdots=" + xDiffDots + " y=" + deltaY);
                } else {
                    reward();
                }
            } else {
                reward();
            }
            this.lastPlacedDeltaX = deltaX;
            this.lastPlacedDeltaDotsX = deltaDotsX;
            rotated = false;
        }
    }
}
