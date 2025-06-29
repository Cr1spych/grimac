package ac.grim.grimac.utils.anticheat.update;

import ac.grim.grimac.checks.impl.aim.processor.AimProcessor;
import ac.grim.grimac.utils.data.HeadRotation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class RotationUpdate {
    private HeadRotation from, to;
    private AimProcessor processor;
    private float deltaPitchRot, deltaYawRot;
    private boolean isCinematic;
    private double sensitivityX, sensitivityY;

    public RotationUpdate(HeadRotation from, HeadRotation to, float deltaYawRot, float deltaPitchRot) {
        this.from = from;
        this.to = to;
        this.deltaYawRot = deltaYawRot;
        this.deltaPitchRot = deltaPitchRot;
    }

    public float getDeltaYawRotABS() {
        return Math.abs(deltaYawRot);
    }

    public float getDeltaPitchRotABS() {
        return Math.abs(deltaPitchRot);
    }
}
